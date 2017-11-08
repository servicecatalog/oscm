/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.setup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.oscm.converter.ResourceLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import com.sun.org.apache.xerces.internal.impl.Constants;

/**
 * Class to import products from their xml representation. Localized resources
 * are not considered.
 * 
 * @author Mike J&auml;ger
 * 
 * @deprecated Use official API instead.
 */
@Deprecated
public class ProductImportParser extends DefaultHandler {

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_PRICE = "price";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_ISCHARGEABLE = "chargeable";
    private static final String ELEMENT_MARKETING_DESC = "MarketingDescription";
    private static final String ELEMENT_MARKETING_NAME = "MarketingName";
    private static final String ELEMENT_PRICE_DESC = "PriceDescription";
    private static final String ELEMENT_SHORT_PRICE_DESC = "ShortPriceDescription";
    private static final String ATTRIBUTE_PRICE_PER_PERIOD = "pricePerPeriod";
    private static final String ATTRIBUTE_PERIOD = "period";
    private static final String ATTRIBUTE_ONE_TIME_FEE = "oneTimeFee";
    private static final String ATTRIBUTE_PRICE_PER_USER = "pricePerUser";
    private static final String ATTRIBUTE_USER_ASSIGNMENT_BASE_PERIOD = "userAssignmentBasePeriod";
    private static final String ATTRIBUTE_ORGANIZATION_ID = "orgId";
    private static final String ATTRIBUTE_SOURCE_ID = "sourceId";
    private static final String ATTRIBUTE_TARGET_ID = "targetId";
    private static final String ATTRIBUTE_LOCALE = "locale";
    private static final String ELEMENT_LOCALIZED_RESOURCE = getShortName(LocalizedResource.class);
    private static final String ELEMENT_PARAMETER = getShortName(Parameter.class);
    private static final String ELEMENT_PRODUCT = getShortName(Product.class);
    private static final String ELEMENT_PRICE_MODEL = getShortName(PriceModel.class);
    private static final String ELEMENT_PRICED_EVENT = getShortName(PricedEvent.class);
    private static final String ELEMENT_PRODUCT_REFERENCE = getShortName(ProductReference.class);
    private static final String ELEMENT_TECHNICAL_PRODUCT = getShortName(TechnicalProduct.class);

    private TechnicalProduct techProduct = null;
    private String productId = null;
    private Product product = null;
    private StringBuffer text = null;
    private DataService dm;
    private Organization org;

    /**
     * Constructor
     * 
     * @param dm
     *            the datamanager bean
     * @param org
     *            the supplier
     */
    public ProductImportParser(DataService dm, Organization org) {
        this.dm = dm;
        this.org = org;
    }

    public String getSchemaName() {
        return "Products.xsd";
    }

    @Override
    public void startElement(String uri, String name, String qName,
            Attributes atts) {

        // abort if the organization is no supplier
        if (!org.hasRole(OrganizationRoleType.SUPPLIER)) {
            addError(qName,
                    "Organization is not authorized to create a marketing product");
            return;
        }

        if (ELEMENT_TECHNICAL_PRODUCT.equals(qName)) {
            String values[] = getMandatoryValues(atts,
                    ATTRIBUTE_ORGANIZATION_ID, ATTRIBUTE_ID);
            if (values == null) {
                return;
            }
            techProduct = getTechnicalProduct(org, values[0], values[1]);

        } else if (ELEMENT_PRODUCT.equals(qName)) {
            productId = getMandatoryValue(atts, ATTRIBUTE_ID);
            if (isBlank(productId) || techProduct == null) {
                return;
            }

        } else if (ELEMENT_LOCALIZED_RESOURCE.equals(qName)) {
            String locale = getMandatoryValue(atts, ATTRIBUTE_LOCALE);
            if (isBlank(locale) || isBlank(productId)) {
                return;
            }

        } else if (ELEMENT_MARKETING_NAME.equals(qName)
                || ELEMENT_MARKETING_DESC.equals(qName)
                || ELEMENT_PRICE_DESC.equals(qName)
                || ELEMENT_SHORT_PRICE_DESC.equals(qName)) {
            text = new StringBuffer();

        } else if (ELEMENT_PRICE_MODEL.equals(qName)) {
            product = initProduct(techProduct, org, productId, qName, atts);

        } else if (ELEMENT_PRICED_EVENT.equals(qName)) {
            processPricedEvent(techProduct, product, qName, atts);

        } else if (ELEMENT_PARAMETER.equals(qName)) {
            processParameter(techProduct, product, qName, atts);

        } else if (ELEMENT_PRODUCT_REFERENCE.equals(qName)) {
            String srcId = getMandatoryValue(atts, ATTRIBUTE_SOURCE_ID);
            String trgId = getMandatoryValue(atts, ATTRIBUTE_TARGET_ID);
            if (isBlank(srcId) || isBlank(trgId)) {
                return;
            }
            Product srcProduct = new Product();
            srcProduct.setVendor(org);
            srcProduct.setProductId(srcId);
            srcProduct = (Product) dm.find(srcProduct);
            if (srcProduct == null) {
                addError(qName, ELEMENT_PRODUCT + " '" + srcId + "' not found!");
                return;
            }

            Product trgProduct = new Product();
            trgProduct.setVendor(org);
            trgProduct.setProductId(trgId);
            trgProduct = (Product) dm.find(trgProduct);
            if (trgProduct == null) {
                addError(qName, ELEMENT_PRODUCT + " '" + trgId + "' not found!");
                return;
            }

            try {
                ProductReference productReference = new ProductReference(
                        srcProduct, trgProduct);
                dm.persist(productReference);
            } catch (NonUniqueBusinessKeyException e) {
                addError(qName, "Already exists " + ATTRIBUTE_SOURCE_ID + "='"
                        + srcId + "' " + ATTRIBUTE_TARGET_ID + "='" + trgId
                        + "'!");
            }

        } else {
            addError(qName, "Unknown element ignored!");

        }

    }

    @Override
    public void endElement(String uri, String name, String qName) {

        if (ELEMENT_TECHNICAL_PRODUCT.equals(qName)) {
            techProduct = null;

        } else if (ELEMENT_PRODUCT.equals(qName)) {
            if (product == null) {
                return;
            }

            // delete all ProductReference rows with this product as source
            // product
            Query query = dm
                    .createNamedQuery("ProductReference.deleteBySourceProduct");
            query.setParameter("sourceProduct", product);
            query.executeUpdate();

            product = null;

        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (text != null) {
            text.append(ch, start, length);
        }
    }

    /**
     * Parse the given XML string an create/update the corresponding entities
     * 
     * @param xml
     *            the XML string
     * @return the parse return code
     * @throws Exception
     */
    public int parse(byte[] xml) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SchemaFactory sf = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try (InputStream inputStream = ResourceLoader.getResourceAsStream(
                getClass(), getSchemaName())) {
            Schema schema = sf.newSchema(new StreamSource(inputStream));
            spf.setSchema(schema);
        }
        SAXParser saxParser = spf.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();
        reader.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true);
        reader.setContentHandler(this);
        reader.parse(new InputSource(new ByteArrayInputStream(xml)));
        return 0;
    }

    /**
     * Read the product from the database or create a new one and initialize the
     * price model and parameter set.
     * 
     * @param techProduct
     *            the technical product for which the product is defined.
     * @param supplier
     *            The supplier the product belongs to.
     * @param productId
     *            the product identifier.
     * @param qName
     *            the qualified name (with prefix) of the current element.
     * @param atts
     *            the attributes attached to the current element.
     * @return The product.
     */
    private Product initProduct(TechnicalProduct techProduct,
            Organization supplier, String productId, String qName,
            Attributes atts) {
        String type = getMandatoryValue(atts, ATTRIBUTE_ISCHARGEABLE);
        String period = atts.getValue(ATTRIBUTE_PERIOD);
        String pricePerPeriod = atts.getValue(ATTRIBUTE_PRICE_PER_PERIOD);
        String userAssignmentBasePeriod = atts
                .getValue(ATTRIBUTE_USER_ASSIGNMENT_BASE_PERIOD);
        String pricePerUser = atts.getValue(ATTRIBUTE_PRICE_PER_USER);
        String oneTimeFee = atts.getValue(ATTRIBUTE_ONE_TIME_FEE);

        if (isBlank(type) || techProduct == null || isBlank(productId)) {
            return null;
        }
        if (isBlank(period) && isBlank(pricePerPeriod)) {
            period = PricingPeriod.MONTH.toString();
            pricePerPeriod = "0";
        }
        if (isBlank(userAssignmentBasePeriod) && isBlank(pricePerUser)) {
            userAssignmentBasePeriod = PricingPeriod.MONTH.toString();
            pricePerUser = "0";
        }
        if (isBlank(oneTimeFee)) {
            oneTimeFee = "0";
        }

        // find the product
        Product product = new Product();
        product.setVendor(supplier);
        product.setProductId(productId);
        product = (Product) dm.find(product);
        PriceModel priceModel = null;
        if (product != null) {
            priceModel = product.getPriceModel();
        }

        // remove the existing price model and create a new one
        PriceModel newPriceModel = new PriceModel();
        if (priceModel != null) {
            // remove the old price model, this will also remove the priced
            // events
            dm.remove(priceModel);
        }
        priceModel = newPriceModel;

        if (Boolean.parseBoolean(type)) {
            priceModel.setType(PriceModelType.PRO_RATA);
        }

        // update the price model attributes (the priced events will be added
        // later when they are processed from the XML parser)
        if (priceModel.isChargeable()) {
            PriceModel priceModelChargeable = priceModel;

            priceModelChargeable.setType(PriceModelType.PRO_RATA);
            priceModelChargeable.setPeriod(PricingPeriod.valueOf(period));
            priceModelChargeable.setPricePerPeriod(new BigDecimal(
                    pricePerPeriod));
            priceModelChargeable.setPricePerUserAssignment(new BigDecimal(
                    pricePerUser));
            priceModelChargeable.setOneTimeFee(new BigDecimal(oneTimeFee));
            SupportedCurrency sc = new SupportedCurrency();
            sc.setCurrency(Currency.getInstance("EUR"));
            sc = (SupportedCurrency) dm.find(sc);
            if (sc == null) {
                addError(qName, "currency not supported");
            } else {
                priceModelChargeable.setCurrency(sc);
            }
        }

        // create or update the product
        if (product == null) {
            product = createProduct(techProduct, supplier, productId,
                    priceModel);
        } else {
            product.setTechnicalProduct(techProduct);
            product.setPriceModel(priceModel);
            // remove the existing parameter
            ParameterSet parameterSet = product.getParameterSet();
            if (parameterSet != null) {
                List<Parameter> list = parameterSet.getParameters();
                if (list != null) {
                    for (Parameter parameter : list) {
                        dm.remove(parameter);
                    }
                }
            }
        }
        return product;
    }

    /**
     * Process a priced event element (get the corresponding event and insert a
     * new priced event).
     * 
     * @param techProduct
     *            the technical product which defines the events.
     * @param product
     *            the product with the price model for the priced event.
     * @param qName
     *            the qualified name (with prefix) of the current element.
     * @param atts
     *            the attributes attached to the current element.
     */
    private void processPricedEvent(TechnicalProduct techProduct,
            Product product, String qName, Attributes atts) {
        String type = getMandatoryValue(atts, ATTRIBUTE_TYPE);
        String id = atts.getValue(ATTRIBUTE_ID);
        String price = getMandatoryValue(atts, ATTRIBUTE_PRICE);
        if (isBlank(type) || isBlank(id) || isBlank(price)
                || techProduct == null || product == null) {
            return;
        }

        // find the event
        Event event = null;
        if (type.equals(EventType.PLATFORM_EVENT.toString())
                && id.equals(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE)) {
            event = getPlatformEvent(id);
        } else if (type.equals(EventType.PLATFORM_EVENT.toString())
                && id.equals(PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE)) {
            event = getPlatformEvent(id);
        } else if (type.equals(EventType.SERVICE_EVENT.toString())) {
            List<Event> events = techProduct.getEvents();
            event = findEvent(events, id);
        } else {
            addError(qName, "Unknown event type '" + type + "'");
        }

        // add the priced event
        if (event != null) {
            PricedEvent pricedEvent = new PricedEvent();
            pricedEvent.setPriceModel(product.getPriceModel());
            pricedEvent.setEvent(event);
            pricedEvent.setEventPrice(new BigDecimal(price));
            persist(pricedEvent);
        } else {
            addError(qName, "Unknown event type:'" + type + "' id:'" + id + "'");
        }
    }

    /**
     * Process a parameter element (get the corresponding parameter definition
     * and insert a new parameter).
     * 
     * @param techProduct
     *            the technical product which defines the events.
     * @param product
     *            the product with the price model for the priced event.
     * @param qName
     *            the qualified name (with prefix) of the current element.
     * @param atts
     *            the attributes attached to the current element.
     */
    private void processParameter(TechnicalProduct techProduct,
            Product product, String qName, Attributes atts) {
        String type = getMandatoryValue(atts, ATTRIBUTE_TYPE);
        String id = atts.getValue(ATTRIBUTE_ID);
        String value = getMandatoryValue(atts, ATTRIBUTE_VALUE);
        if (isBlank(type) || isBlank(id) || isBlank(value)
                || techProduct == null || product == null) {
            return;
        }

        // find the parameter definition
        ParameterDefinition paramDef = null;
        if (type.equals(ParameterType.PLATFORM_PARAMETER.toString())
                && id.equals(PlatformParameterIdentifiers.CONCURRENT_USER)) {
            paramDef = getPlatformParamDef(id);
        } else if (type.equals(ParameterType.PLATFORM_PARAMETER.toString())
                && id.equals(PlatformParameterIdentifiers.NAMED_USER)) {
            paramDef = getPlatformParamDef(id);
        } else if (type.equals(ParameterType.PLATFORM_PARAMETER.toString())
                && id.equals(PlatformParameterIdentifiers.PERIOD)) {
            paramDef = getPlatformParamDef(id);
        } else if (type.equals(ParameterType.SERVICE_PARAMETER.toString())) {
            List<ParameterDefinition> paramDefs = techProduct
                    .getParameterDefinitions();
            paramDef = findParam(paramDefs, id);
        } else {
            addError(qName, "Unknown parameter definition type '" + type + "'");
        }

        // add the parameter
        if (paramDef != null) {
            ParameterSet parameterSet = product.getParameterSet();
            if (parameterSet == null) {
                parameterSet = new ParameterSet();
                persist(parameterSet);
                product.setParameterSet(parameterSet);
            }
            Parameter parameter = new Parameter();
            parameter.setParameterSet(parameterSet);
            parameter.setParameterDefinition(paramDef);
            parameter.setValue(value);
            persist(parameter);
        } else {
            addError(qName, "Unknown parameter definition type:'" + type
                    + "' id:'" + id + "'");
        }
    }

    /**
     * Read the technical product from the database.
     * 
     * @param supplier
     *            the supplier who want to access the technical product
     * @param techProviderId
     *            the technology provider of the technical product
     * @param id
     *            the technical product identifier
     * @param version
     *            the technical product version
     * @return the technical product.
     * 
     */
    private TechnicalProduct getTechnicalProduct(Organization supplier,
            String techProviderId, String id) {
        Organization techProvider = null;
        for (Organization organization : supplier.getTechnologyProviders()) {
            if (organization.getOrganizationId().equals(techProviderId)) {
                techProvider = organization;
            }
        }
        if (techProvider == null) {
            throw new SaaSSystemException(
                    "Your organization isn't a supplier for the"
                            + " technology provider '" + techProviderId + "'!",
                    null);
        }

        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setOrganization(techProvider);
        techProduct.setTechnicalProductId(id);
        try {
            techProduct = (TechnicalProduct) dm
                    .getReferenceByBusinessKey(techProduct);
        } catch (ObjectNotFoundException e) {
            throw new SaaSSystemException("Technical product missing!", e);
        }
        return techProduct;
    }

    /**
     * Create a new product
     * 
     * @param technicalProduct
     *            the technical product for which the market product is created
     * @param supplier
     *            the owner of the created product.
     * @param productId
     *            the identifier of the created product.
     * @param priceModel
     *            the price model of the created product.
     * @return the product domain object.
     */
    private Product createProduct(TechnicalProduct technicalProduct,
            Organization supplier, String productId, PriceModel priceModel) {
        Product product = new Product();
        product.setVendor(supplier);
        product.setProductId(productId);
        product.setPriceModel(priceModel);
        product.setProvisioningDate(System.currentTimeMillis());
        product.setTechnicalProduct(technicalProduct);
        product.setStatus(ServiceStatus.INACTIVE);
        product.setType(ServiceType.TEMPLATE);
        product.setAutoAssignUserEnabled(Boolean.FALSE);
        // no parameter set defined, so set empty one as initial parameter set
        ParameterSet emptyPS = new ParameterSet();
        product.setParameterSet(emptyPS);
        persist(product);
        return product;
    }

    /**
     * Return true if the given string is null or only contains whitespaces.
     * 
     * @param str
     *            the string to check
     * @return true if the given string is null or only contains whitespaces.
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Get a mandatory attribute value. If the attribute value is blank an error
     * message is added to the error string buffer
     * 
     * @param atts
     *            the attributes of the element
     * @param attName
     *            the attribute name
     * 
     * @return the attribute value or null if the value is blank.
     */
    private String getMandatoryValue(Attributes atts, String attName) {
        String value = atts.getValue(attName);
        if (isBlank(value)) {
            throw new SaaSSystemException("Missing attribute '" + attName
                    + "'.");
        }
        return value;
    }

    /**
     * Returns the short name of the given class.
     * 
     * @param clazz
     *            The class to retrieve the name for
     * @return The short class name.
     */
    private static String getShortName(Class<?> clazz) {
        String name = clazz.getName();
        int idx = name.lastIndexOf(".");
        if (idx >= 0) {
            return name.substring(idx + 1);
        }
        return name;
    }

    /**
     * Throws a system exception indicating the qname and message.
     * 
     * @param qName
     *            The name of the attribute causing the conflict.
     * @param message
     *            The message to be set.
     */
    private void addError(String qName, String message) {
        throw new SaaSSystemException(message + " caused by attribute '"
                + qName + "'.");
    }

    /**
     * Tries to persist the domain object. If this fails, a system exception is
     * thrown.
     * 
     * @param object
     *            The object to be stored.
     */
    private void persist(DomainObject<?> object) {
        try {
            dm.persist(object);
        } catch (NonUniqueBusinessKeyException e) {
            throw new SaaSSystemException(e);
        }
    }

    /**
     * Read a platform parameter definition from the database
     * 
     * @param id
     *            the event identifier
     * @return the event domain object.
     * 
     */
    private ParameterDefinition getPlatformParamDef(String id) {
        Query query = dm
                .createNamedQuery("ParameterDefinition.getPlatformParameterDefinition");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        query.setParameter("parameterId", id);

        ParameterDefinition paramDef = null;
        try {
            paramDef = (ParameterDefinition) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new SaaSSystemException(
                    "Non unique platform parameter definition '" + id + "'!", e);
        }
        return paramDef;
    }

    /**
     * Read a platform event from the database
     * 
     * @param id
     *            the event identifier
     * @return the event domain object.
     * 
     */
    private Event getPlatformEvent(String id) {
        Query query = dm.createNamedQuery("Event.getPlatformEvent");
        query.setParameter("eventType", EventType.PLATFORM_EVENT);
        query.setParameter("eventIdentifier", id);

        Event event = null;
        try {
            event = (Event) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new SaaSSystemException("Non unique platform event '" + id
                    + "'!", e);
        }
        return event;
    }

    /**
     * Retrieves the mandatory attribute values for one element.
     * 
     * @param atts
     *            The attributes to parse..
     * @param attNames
     *            The names of the attributes to be retrieved.
     * 
     * @return The determined attribute values.
     */
    private String[] getMandatoryValues(Attributes atts, String... attNames) {
        List<String> values = new ArrayList<String>();
        for (String attributeName : attNames) {
            values.add(getMandatoryValue(atts, attributeName));
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Retrieves the product parameter with the given id.
     * 
     * @param paramDefs
     *            The parameters to parse.
     * @param id
     *            The id the target parameter should have.
     * @return The parameter with the given id. If none is found,
     *         <code>null</code> is returned.
     */
    private ParameterDefinition findParam(List<ParameterDefinition> paramDefs,
            String id) {
        for (ParameterDefinition param : paramDefs) {
            if (param.getParameterType() == ParameterType.SERVICE_PARAMETER
                    && param.getParameterId().equals(id)) {
                return param;
            }
        }
        return null;
    }

    /**
     * Retrieves the product event with the given identifier.
     * 
     * @param events
     *            The events to parse.
     * @param id
     *            The id the target event should have.
     * @return The event with the given id. If none is found, <code>null</code>
     *         is returned.
     */
    private Event findEvent(List<Event> events, String id) {
        for (Event evt : events) {
            if (evt.getEventType() == EventType.SERVICE_EVENT
                    && evt.getEventIdentifier().equals(id)) {
                return evt;
            }
        }
        return null;
    }
}
