/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.sun.org.apache.xerces.internal.impl.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ResourceLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.BillingAdapterNotFoundException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TechnicalServiceActiveException;
import org.oscm.internal.types.exception.TechnicalServiceMultiSubscriptions;
import org.oscm.internal.types.exception.UnchangeableAllowingOnBehalfActingException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.serviceprovisioningservice.assembler.TagAssembler;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.validator.BLValidator;

public class TechnicalProductImportParser extends ImportParserBase {

    private static final String PROTOCOLL_HTTP = "http://";
    private static final String PROTOCOLL_HTTPS = "https://";

    public static final int RC_OK = 0;
    public static final int RC_WARNING = 90;
    public static final int RC_ERROR = 100;
    public static final int RC_FATAL = 200;

    private static final String ELEMENT_TECHNICAL_SERVICES = "TechnicalServices";
    private static final String ELEMENT_TECHNICAL_SERVICE = "TechnicalService";
    private static final String ELEMENT_LOCALIZED_DESCRIPTION = "LocalizedDescription";
    private static final String ELEMENT_LOCALIZED_LICENSE = "LocalizedLicense";
    private static final String ELEMENT_LOCALIZED_TAG = "LocalizedTag";
    private static final String ELEMENT_EVENT = getShortName(Event.class);
    private static final String ELEMENT_PARAMETER_DEFINITION = getShortName(
            ParameterDefinition.class);
    private static final String ELEMENT_ACCESS_INFO = "AccessInfo";
    private static final String ELEMENT_OPTION = "Option";
    private static final String ELEMENT_OPTIONS = "Options";
    private static final String ELEMENT_LOCALIZED_OPTION = "LocalizedOption";
    private static final String ELEMENT_ROLE = "Role";
    private static final String ELEMENT_LOCALIZED_NAME = "LocalizedName";
    private static final String ELEMENT_OPERATION = "Operation";
    private static final String ELEMENT_OPERATION_PARAMETER = "OperationParameter";

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_ACCESS_TYPE = "accessType";
    private static final String ATTRIBUTE_BASE_URL = "baseUrl";
    private static final String ATTRIBUTE_BILLING_IDENTIFIER = "billingIdentifier";
    private static final String ATTRIBUTE_BUILD = "build";
    private static final String ATTRIBUTE_LOCALE = "locale";
    private static final String ATTRIBUTE_LOGIN_PATH = "loginPath";
    private static final String ATTRIBUTE_PROVISIONING_TYPE = "provisioningType";
    private static final String ATTRIBUTE_PROVISIONING_URL = "provisioningUrl";
    private static final String ATTRIBUTE_PROVISIONING_VERSION = "provisioningVersion";
    private static final String ATTRIBUTE_PROVISIONING_TIMEOUT = "provisioningTimeout";
    private static final String ATTRIBUTE_PROVISIONING_USERNAME = "provisioningUsername";
    private static final String ATTRIBUTE_PROVISIONING_PASSWORD = "provisioningPassword";
    private static final String ATTRIBUTE_VALUE_TYPE = "valueType";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_ACTIONURL = "actionURL";
    private static final String ATTRIBUTE_SUBSCRIPTION_RESTRICTION = "onlyOneSubscriptionPerUser";
    private static final String ATTRIBUTE_ONBEHALF_ACTING = "allowingOnBehalfActing";
    private static final String ATTRIBUTE_MANDATORY = "mandatory";
    private static final String DEFAULT_LOCALE = "en";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TechnicalProductImportParser.class);

    private LocalizerServiceLocal localizer;
    private TenantProvisioningServiceBean tenantProvisioning;
    private Organization org;
    private StringBuffer text = null;
    private SaaSApplicationException appException = null;
    private StringBuffer buffer;
    private int rc;
    private TechnicalProduct techProduct = null;
    private Event event = null;
    private List<Event> obsoleteEvents = null;
    private ParameterDefinition parameterDef = null;
    private List<ParameterDefinition> obsoleteParameterDefs = null;
    private RoleDefinition role = null;
    private List<RoleDefinition> obsoleteRoles = null;
    private String locale = null;
    private SessionServiceLocal pm;
    private TechnicalProductOperation operation = null;
    private OperationParameter operationParameter = null;
    private List<TechnicalProductOperation> obsoleteOperations = null;
    private List<OperationParameter> obsoleteOperationParameters = null;
    private TagServiceLocal tagService;
    private TechnicalProductParameterImportParser parameterParser;
    private TechnicalProductParameterOptionImportParser parOptionParser = null;

    /**
     * The list of tags which are defined for one technical product.
     */
    private final List<Tag> techProductTags = new ArrayList<Tag>();

    /**
     * The set of already processed operation ids in the scope of a technical
     * product. Used for uniqueness validation.
     */
    private final Set<String> operationIds = new HashSet<String>();

    /**
     * The set of already processed operation parameter ids in the scope of a
     * technical product operation. Used for uniqueness validation.
     */
    private final Set<String> operationParameterIds = new HashSet<String>();

    /**
     * The set of already processed role ids in the scope of a technical
     * product. Used for uniqueness validation.
     */
    private final Set<String> roleIds = new HashSet<String>();

    /**
     * The set of already processed event ids in the scope of a technical
     * product. Used for uniqueness validation.
     */
    private final Set<String> eventIds = new HashSet<String>();

    /**
     * The set of already processed parameter ids in the scope of a technical
     * product. Used for uniqueness validation.
     */
    private final Set<String> parameterIds = new HashSet<String>();

    private final MarketingPermissionServiceLocal ms;

    private boolean isKafkaServerEnabled = false;

    /**
     * Constructor
     * 
     * @param dm
     *            the datamanager bean
     * @param localizer
     *            the localizer bean
     * @param org
     *            the organization (technology provider).
     * @param ms
     *            the marketing permission service
     * @param isKafkaServerEnabled
     *            boolean flag for kafka server
     */
    private TechnicalProductImportParser(DataService dm,
            LocalizerServiceLocal localizer, Organization org,
            TenantProvisioningServiceBean tenantProvisioning,
            MarketingPermissionServiceLocal ms, boolean isKafkaServerEnabled) {
        this.dm = dm;
        this.localizer = localizer;
        this.org = org;
        this.tenantProvisioning = tenantProvisioning;
        this.ms = ms;
        this.isKafkaServerEnabled = isKafkaServerEnabled;
        parameterParser = new TechnicalProductParameterImportParser();
        init();
    }

    /**
     * Constructor
     * 
     * @param dm
     *            the datamanager bean
     * @param localizer
     *            the localizer bean
     * @param org
     *            the organization (technology provider).
     * @param pm
     *            The product session service.
     * @param tenantProvisioning
     *            the tenant provisioning service
     * @param ms
     * @param confService
     */
    public TechnicalProductImportParser(DataService dm,
            LocalizerServiceLocal localizer, Organization org,
            SessionServiceLocal pm,
            TenantProvisioningServiceBean tenantProvisioning,
            TagServiceLocal tagService, MarketingPermissionServiceLocal ms,
            ConfigurationServiceLocal confService,
            boolean isKafkaServerEnabled) {
        this(dm, localizer, org, tenantProvisioning, ms, isKafkaServerEnabled);
        this.pm = pm;
        this.tagService = tagService;
    }

    private static String getShortName(Class<?> clazz) {
        String name = clazz.getName();
        int idx = name.lastIndexOf(".");
        if (idx >= 0) {
            return name.substring(idx + 1);
        }
        return name;
    }

    private class SimpleErrorHandler implements ErrorHandler {
        private String getText(SAXException e) {
            if (e instanceof SAXParseException) {
                return ((SAXParseException) e).getLineNumber() + ": "
                        + e.getLocalizedMessage();
            } else {
                return e.getMessage();
            }
        }

        @Override
        public void warning(SAXParseException e) {
            rc = RC_WARNING;
            String text = getText(e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_IMPORT_PARSER_WARNING);
            if (buffer.indexOf(text) == -1) {
                buffer.append(text).append("\n");
            }
        }

        @Override
        public void error(SAXParseException e) {
            rc = RC_ERROR;
            String text = getText(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IMPORT_PARSER_ERROR);
            if (buffer.indexOf(text) == -1) {
                buffer.append(text).append("\n");
            }
        }

        @Override
        public void fatalError(SAXParseException e) {
        }
    }

    private abstract class Finder<E> {

        public E find(List<E> list, String id) {
            if (list == null || id == null) {
                return null;
            }
            for (E e : list) {
                if (equals(e, id)) {
                    return e;
                }
            }
            return null;
        }

        abstract public boolean equals(E element, String id);
    }

    private final Finder<Event> prodEventFinder = new Finder<Event>() {
        @Override
        public boolean equals(Event event, String id) {
            return event.getEventType() == EventType.SERVICE_EVENT
                    && event.getEventIdentifier().equals(id);
        }
    };

    private final Finder<ParameterDefinition> prodParamDefFinder = new Finder<ParameterDefinition>() {
        @Override
        public boolean equals(ParameterDefinition paramDef, String id) {
            return paramDef
                    .getParameterType() == ParameterType.SERVICE_PARAMETER
                    && paramDef.getParameterId().equals(id);
        }
    };

    private final Finder<RoleDefinition> roleDefFinder = new Finder<RoleDefinition>() {
        @Override
        public boolean equals(RoleDefinition roleDef, String id) {
            return roleDef.getRoleId().equals(id);
        }
    };

    private final Finder<TechnicalProductOperation> operationFinder = new Finder<TechnicalProductOperation>() {
        @Override
        public boolean equals(TechnicalProductOperation operation, String id) {
            return operation.getOperationId().equals(id);
        }
    };

    private final Finder<OperationParameter> operationParameterFinder = new Finder<OperationParameter>() {
        @Override
        public boolean equals(OperationParameter op, String id) {
            return op.getId().equals(id);
        }
    };

    /**
     * @return the file name of the XML schema which is used to validate the
     *         XML.
     */
    public String getSchemaName() {
        return "TechnicalServices.xsd";
    }

    public boolean isXmlValid() {
        return rc == RC_OK || rc == RC_WARNING;
    }

    /**
     * Parse the given XML string an create/update the corresponding entities
     * 
     * @param xml
     *            the XML string
     * @return the parse return code
     * @throws OperationNotPermittedException
     *             Thrown in case the current organization is not permitted to
     *             perform the import.
     * @throws TechnicalServiceActiveException
     *             Thrown in case an imported technical product is still in use.
     * @throws UpdateConstraintException
     *             Thrown in case the update fails.
     * @throws TechnicalServiceMultiSubscriptions
     *             Thrown in case a technical product is defined to allow
     *             multiple subscription, but multiple subscriptions already
     *             exist.
     * @throws BillingAdapterNotFoundException
     *             Thrown in case imported technical service specifies billing
     *             identifier which is not registered in OSCM.
     */
    public int parse(byte[] xml) throws OperationNotPermittedException,
            TechnicalServiceActiveException, UpdateConstraintException,
            TechnicalServiceMultiSubscriptions,
            UnchangeableAllowingOnBehalfActingException,
            BillingAdapterNotFoundException {
        buffer = new StringBuffer();
        rc = RC_OK;
        SimpleErrorHandler simpleErrorHandler = new SimpleErrorHandler();

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spf.setNamespaceAware(true);

            SchemaFactory sf = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            final Schema schema;
            try (InputStream inputStream = ResourceLoader
                    .getResourceAsStream(getClass(), getSchemaName());) {
                schema = sf.newSchema(new StreamSource(inputStream));
            }
            spf.setSchema(schema);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader reader = saxParser.getXMLReader();
            reader.setFeature(Constants.XERCES_FEATURE_PREFIX
                    + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true);
            reader.setContentHandler(this);
            reader.setErrorHandler(simpleErrorHandler);
            reader.parse(new InputSource(new ByteArrayInputStream(xml)));
        } catch (SAXParseException e) {
            handleSaxError(simpleErrorHandler, e);
        } catch (SAXNotRecognizedException e) {
            handleSaxError(simpleErrorHandler, e);
        } catch (SAXNotSupportedException e) {
            handleSaxError(simpleErrorHandler, e);
        } catch (ParserConfigurationException e) {
            rc = RC_FATAL;
            String text = e.getMessage();
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IMPORT_PARSER_ERROR);
            buffer.append(text).append("\n");
        } catch (SaaSSystemException e) {
            throw e;
        } catch (Exception e) {
            if (appException instanceof OperationNotPermittedException) {
                throw (OperationNotPermittedException) appException;
            } else if (appException instanceof TechnicalServiceActiveException) {
                throw (TechnicalServiceActiveException) appException;
            } else if (appException instanceof UpdateConstraintException) {
                throw (UpdateConstraintException) appException;
            } else if (appException instanceof TechnicalServiceMultiSubscriptions) {
                throw (TechnicalServiceMultiSubscriptions) appException;
            } else if (appException instanceof UnchangeableAllowingOnBehalfActingException) {
                throw (UnchangeableAllowingOnBehalfActingException) appException;
            } else if (appException instanceof BillingAdapterNotFoundException) {
                throw (BillingAdapterNotFoundException) appException;
            }
            rc = RC_FATAL;
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IMPORT_PARSER_ERROR);
            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                buffer.append(e.getMessage()).append("\n");
            } else if (e.getCause() != null) {
                buffer.append(e.getCause().getMessage()).append("\n");
            }
        }

        return rc;
    }

    private void handleSaxError(SimpleErrorHandler simpleErrorHandler,
            SAXException e) {
        rc = RC_FATAL;
        String text = simpleErrorHandler.getText(e);
        logger.logError(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.ERROR_IMPORT_PARSER_ERROR);
        buffer.append(text).append("\n");
    }

    /**
     * Get the message for the previous parse call.
     * 
     * @return the message for the previous parse call.
     */
    public String getMessage() {
        return buffer.toString();

    }

    /*
     * (non-Javadoc) receive notification of character data
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (text != null) {
            text.append(ch, start, length);
        }
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
     * Return true if the given StringBuffer is null or contains only
     * whitespaces.
     *
     * @param sb
     *            the StringBuffer to check
     * @return true if the given StringBuffer is null or contains only
     *         whitespaces.
     */
    private boolean isBlank(StringBuffer sb) {
        return sb == null || sb.toString().trim().length() == 0;
    }

    private void checkMandatoryAccessInfo() {
        if (techProduct.getAccessType() == ServiceAccessType.DIRECT
                || techProduct.getAccessType() == ServiceAccessType.USER) {
            String accessInfo = localizer.getLocalizedTextFromDatabase("en",
                    techProduct.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
            if (isBlank(accessInfo)) {
                addError(ELEMENT_ACCESS_INFO,
                        ELEMENT_ACCESS_INFO + " is required if "
                                + ATTRIBUTE_ACCESS_TYPE + " is "
                                + techProduct.getAccessType().name());
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_ACCESSINFO_NEEDED,
                        techProduct.getAccessType().name());
            }
        }
    }

    void checkEventBasedProvisioning() {

        if (this.isKafkaServerEnabled) {
            if (techProduct.getAccessType() == ServiceAccessType.DIRECT
                    && techProduct.getProvisioningURL().isEmpty() && techProduct
                            .getProvisioningType() == ProvisioningType.SYNCHRONOUS) {
                addError(ATTRIBUTE_PROVISIONING_TYPE,
                        "Attribute " + ATTRIBUTE_PROVISIONING_TYPE + " must be "
                                + ProvisioningType.ASYNCHRONOUS.name()
                                + " in case attribute " + ATTRIBUTE_ACCESS_TYPE
                                + " is " + ServiceAccessType.DIRECT + " and "
                                + ATTRIBUTE_PROVISIONING_URL + " is empty");
                return;
            }
        }
    }

    boolean isEventBasedProvisioning(Attributes atts) {
        if (!this.isKafkaServerEnabled) {
            return false;
        }
        if (ServiceAccessType.DIRECT.name()
                .equals(atts.getValue(ATTRIBUTE_ACCESS_TYPE))
                && isBlank(atts.getValue(ATTRIBUTE_PROVISIONING_URL))
                && ProvisioningType.ASYNCHRONOUS.name()
                        .equals(atts.getValue(ATTRIBUTE_PROVISIONING_TYPE))) {
            return true;
        }
        return false;
    }

    /**
     * Get a mandatory attribute value. If the attribute value is blank an error
     * message is added to the error string buffer
     * 
     * @param qName
     *            the element name
     * @param atts
     *            the attributes of the element
     * @param attName
     *            the attribute name
     * @return the attribute value or null if the value is blank.
     */
    private String getMandatoryValue(String qName, Attributes atts,
            String attName) {
        String value = atts.getValue(attName);
        if (isBlank(value)) {
            addError(qName,
                    "Mandatory attribute '" + attName + "' is missing!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_MANDATORY_ATTRIBUTE_MISSING,
                    attName);
            return null;
        }
        return value;
    }

    private String getLocaleValue(Attributes atts, String attName) {
        String value = atts.getValue(attName);
        if (isBlank(value)) {
            return DEFAULT_LOCALE;
        }
        return value;
    }

    /**
     * Get the mandatory attribute values of an element. If one attribute value
     * is blank an error message is added to the error string buffer and null is
     * returned
     * 
     * @param qName
     *            the element name
     * @param atts
     *            the attributes of the element
     * @param attNames
     *            the attribute names
     * @return an array with the attribute values or null if the on attribute
     *         value is blank.
     */
    private String[] getMandatoryValues(String qName, Attributes atts,
            String... attNames) {
        boolean missing = false;
        String[] values = new String[attNames.length];
        for (int i = 0; i < attNames.length; i++) {
            values[i] = atts.getValue(attNames[i]);
            if (isBlank(values[i])) {
                addError(qName, "Mandatory attribute '" + attNames[i]
                        + "' is missing!");
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_MANDATORY_ATTRIBUTE_MISSING,
                        attNames[i]);
                missing = true;
            }
        }
        if (missing) {
            return null;
        }
        return values;
    }

    /**
     * Add an error message to the internal error buffer.
     * 
     * @param qName
     *            current element name
     * @param str
     *            the string to add
     */
    public void addError(String qName, String str) {
        if (str == null) {
            return;
        }
        if (qName != null) {
            str = qName + ": " + str;
        }
        rc = RC_ERROR;
        if (buffer.length() > 0) {
            buffer.append(";\n ");
        }
        buffer.append(str);
    }

    /**
     * Log some debug information
     * 
     * @param qName
     *            current element name
     * @param str
     *            the string to log
     */
    private void debug(String qName, String str) {
        logger.logDebug(str + ": " + qName);
    }

    /**
     * Store an application Exception, so that it can be evaluated in the parse
     * method.
     * 
     * @param appException
     *            the application exception to set
     */
    private void setAppException(SaaSApplicationException appException) {
        this.appException = appException;
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
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_NOT_UNIQUE_PLATFORM_EVENT, id);
            throw se;
        }
        return event;
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
        Query query = dm.createNamedQuery(
                "ParameterDefinition.getPlatformParameterDefinition");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        query.setParameter("parameterId", id);

        ParameterDefinition paramDef = null;
        try {
            paramDef = (ParameterDefinition) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_NOT_UNIQUE_PLATFORM_PARAMETER,
                    id);
            throw se;
        }
        return paramDef;
    }

    private String[] getOptionalValues(Attributes atts, String... attNames) {
        String[] values = new String[attNames.length];
        for (int i = 0; i < attNames.length; i++) {
            values[i] = atts.getValue(attNames[i]);
        }
        return values;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String name, String qName,
            Attributes atts) throws SAXException {

        debug(name, "startElement");

        if (ELEMENT_TECHNICAL_SERVICE.equals(name)) {
            clearMembers();

            String baseUrl = atts.getValue(ATTRIBUTE_BASE_URL);
            try {
                BLValidator.isUrl(ATTRIBUTE_BASE_URL, baseUrl, false);
            } catch (ValidationException e) {
                addError(name, ATTRIBUTE_BASE_URL + " \"" + baseUrl + "\""
                        + " is not valid url");
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
            }
            while (baseUrl != null && (baseUrl.endsWith("\\"))) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            String[] values = getMandatoryValues(name, atts, ATTRIBUTE_ID,
                    ATTRIBUTE_ACCESS_TYPE);
            if (values == null) {
                return;
            }

            ServiceAccessType accessType = ServiceAccessType.valueOf(values[1]);
            if (accessType != ServiceAccessType.DIRECT
                    && accessType != ServiceAccessType.USER) {
                if (isBlank(baseUrl)) {
                    addError(name, ATTRIBUTE_BASE_URL + " is required if "
                            + ATTRIBUTE_ACCESS_TYPE + " is not equal to "
                            + ServiceAccessType.DIRECT.name() + " nor "
                            + ServiceAccessType.USER.name());
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_BASE_URL_NEEDED,
                            ServiceAccessType.DIRECT.name(),
                            ServiceAccessType.USER.name());
                }
            }

            String provisioningType = atts
                    .getValue(ATTRIBUTE_PROVISIONING_TYPE);
            if (provisioningType == null
                    || provisioningType.trim().length() == 0) {
                provisioningType = ProvisioningType.SYNCHRONOUS.name();
            }
            String provisioningUrl = atts.getValue(ATTRIBUTE_PROVISIONING_URL);
            String provisioningVersion = atts
                    .getValue(ATTRIBUTE_PROVISIONING_VERSION);

            if (accessType != ServiceAccessType.EXTERNAL) {
                // if the provisioning URL is relative and the base URL is set
                // create an absolute URL with the help of the base URL
                if (provisioningUrl != null) {
                    String lower = provisioningUrl.toLowerCase();
                    if (baseUrl != null && !lower.startsWith(PROTOCOLL_HTTP)
                            && !lower.startsWith(PROTOCOLL_HTTPS)) {
                        provisioningUrl = removeEndingSlash(baseUrl)
                                + provisioningUrl;
                    }
                }
                if (isBlank(provisioningUrl)
                        && !isEventBasedProvisioning(atts)) {
                    addError(name,
                            "The attributes '" + ATTRIBUTE_PROVISIONING_URL
                                    + "', '"
                                    + "' is mandatory for the access type "
                                    + accessType);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_PROVISIONING_URL_NEEDED,
                            String.valueOf(accessType));
                    return;
                }
            } else if (provisioningUrl == null) {
                provisioningUrl = "";
            }

            Boolean subscriptionRestriction = Boolean
                    .valueOf(atts.getValue(ATTRIBUTE_SUBSCRIPTION_RESTRICTION));
            if (subscriptionRestriction == null) {
                subscriptionRestriction = Boolean.FALSE; // set it to the
                                                         // default
            }

            Boolean onBehalfActing = Boolean
                    .valueOf(atts.getValue(ATTRIBUTE_ONBEHALF_ACTING));
            if (onBehalfActing == null) {
                onBehalfActing = Boolean.FALSE; // default
            }

            String techProductId = values[0];
            String billingIdentifier = getValidatedBillingID(atts,
                    techProductId);

            techProduct = initTechnicalProduct(values[0],
                    ProvisioningType.valueOf(provisioningType), provisioningUrl,
                    provisioningVersion, accessType,
                    subscriptionRestriction.booleanValue(),
                    onBehalfActing.booleanValue(), billingIdentifier);

            /*
             * if the technical product already refers to events or parameter
             * definitions we must delete all events and parameter definitions
             * which are not mentioned in the imported XML anymore
             */
            obsoleteEvents = new ArrayList<Event>(techProduct.getEvents());
            obsoleteParameterDefs = new ArrayList<ParameterDefinition>(
                    techProduct.getParameterDefinitions());
            obsoleteRoles = new ArrayList<RoleDefinition>(
                    techProduct.getRoleDefinitions());
            obsoleteOperations = new ArrayList<TechnicalProductOperation>(
                    techProduct.getTechnicalProductOperations());

            String loginPath = atts.getValue(ATTRIBUTE_LOGIN_PATH);
            // remove leading backslashes
            while (loginPath != null && loginPath.startsWith("\\")) {
                loginPath = loginPath.substring(1);
            }
            // add a leading slash if it is missing (the login path will be
            // appended to the base URL)
            if (loginPath != null && !loginPath.startsWith("/")) {
                loginPath = "/" + loginPath;
            }
            if (accessType != ServiceAccessType.DIRECT
                    && accessType != ServiceAccessType.EXTERNAL
                    && accessType != ServiceAccessType.USER) {
                if (isBlank(loginPath)) {
                    addError(name,
                            ATTRIBUTE_LOGIN_PATH + " is required if "
                                    + ATTRIBUTE_ACCESS_TYPE + " is equal to "
                                    + accessType);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_LOGIN_PATH_NEEDED,
                            String.valueOf(accessType));
                }
            }

            String build = atts.getValue(ATTRIBUTE_BUILD);
            techProduct.setTechnicalProductBuildId(build);
            Long provisioningTimeout = null;
            if (atts.getValue(ATTRIBUTE_PROVISIONING_TIMEOUT) != null) {
                provisioningTimeout = Long
                        .valueOf(atts.getValue(ATTRIBUTE_PROVISIONING_TIMEOUT));
            }
            techProduct.setProvisioningTimeout(provisioningTimeout);
            String username = atts.getValue(ATTRIBUTE_PROVISIONING_USERNAME);
            techProduct.setProvisioningUsername(username);
            String password = atts.getValue(ATTRIBUTE_PROVISIONING_PASSWORD);
            techProduct.setProvisioningPassword(password);
            techProduct.setLoginPath(loginPath);
            techProduct.setBaseURL(baseUrl);
            techProduct.setBillingIdentifier(billingIdentifier);

        } else if (ELEMENT_LOCALIZED_DESCRIPTION.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);
            cleanBuffer();
        } else if (ELEMENT_LOCALIZED_NAME.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);
            cleanBuffer();
        } else if (ELEMENT_LOCALIZED_TAG.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);
            cleanBuffer();
        } else if (ELEMENT_ACCESS_INFO.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);
            cleanBuffer();
        } else if (ELEMENT_LOCALIZED_LICENSE.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);
            cleanBuffer();
        } else if (ELEMENT_EVENT.equals(name)) {
            String id = getMandatoryValue(name, atts, ATTRIBUTE_ID);
            if (id == null || techProduct == null) {
                return;
            }
            if (eventIds.contains(id)) {
                addError(name, "Duplicate event id: " + id);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_EVENT_ID,
                        id);
                return;
            }
            event = initProductEvent(techProduct, id);
        } else if (ELEMENT_ROLE.equals(name)) {
            String id = getMandatoryValue(name, atts, ATTRIBUTE_ID);
            if (id == null || techProduct == null) {
                return;
            }
            if (techProduct.getAccessType() == ServiceAccessType.DIRECT) {
                String message = "The definition of service roles is not allowed if %s is %s";
                addError(name, String.format(message, ATTRIBUTE_ACCESS_TYPE,
                        ServiceAccessType.DIRECT.name()));
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPROT_PARSER_ERROR_ROLES_NOT_ALLOWED,
                        ATTRIBUTE_ACCESS_TYPE, ServiceAccessType.DIRECT.name());
                return;
            }
            if (roleIds.contains(id)) {
                addError(name, "Duplicate role id: " + id);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_ROLE_ID,
                        id);
                return;
            }
            role = initRole(techProduct, id);
        } else if (ELEMENT_OPERATION.equals(name)) {
            if (techProduct == null) {
                return;
            }
            String id = getMandatoryValue(name, atts, ATTRIBUTE_ID);
            String actionUrl = getMandatoryValue(name, atts,
                    ATTRIBUTE_ACTIONURL);

            if (operationIds.contains(id)) {
                addError(name, "Duplicate operation id: " + id);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_OPERATION_ID,
                        id);
                return;
            }
            operation = initOperation(techProduct, id, actionUrl);
            obsoleteOperationParameters = new ArrayList<>(
                    operation.getParameters());
        } else if (ELEMENT_OPERATION_PARAMETER.equals(name)) {
            if (!readOperationParameter(atts, name)) {
                return;
            }
        } else if (ELEMENT_PARAMETER_DEFINITION.equals(name)) {
            String[] mandatoryValues = getMandatoryValues(name, atts,
                    ATTRIBUTE_ID, ATTRIBUTE_VALUE_TYPE);
            if (mandatoryValues == null || techProduct == null) {
                return;
            }

            // if parameter Id already exists
            if (parameterIds.contains(mandatoryValues[0])) {
                addError(name, "Duplicate parameter id: " + mandatoryValues[0]);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_PARAMETER_ID,
                        mandatoryValues[0]);
                return;
            }

            try {
                ParameterDefinition paramDef = prodParamDefFinder.find(
                        techProduct.getParameterDefinitions(),
                        mandatoryValues[0]);

                parameterDef = parameterParser.parseParameterDef(
                        mandatoryValues[0], mandatoryValues[1], atts,
                        techProduct, paramDef);
            } catch (UpdateConstraintException e) {
                setAppException(e);
                throw new SAXException(e);
            } catch (ImportException e) {
                addError(name, e.getDetails());
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }

            parameterIds.add(parameterDef.getParameterId());

            if (parameterParser.isCreateAction()) {
                // if parameter definition is new, persist this parameterDef
                persist(parameterDef);
            } else {
                // remove old Parameter definition from obsolete parameter
                // definition list by Id
                ParameterDefinition oldParamDef = prodParamDefFinder
                        .find(obsoleteParameterDefs, mandatoryValues[0]);
                if (obsoleteParameterDefs != null) {
                    obsoleteParameterDefs.remove(oldParamDef);
                }
            }
        } else if (ELEMENT_LOCALIZED_OPTION.equals(name)) {
            locale = getLocaleValue(atts, ATTRIBUTE_LOCALE);

            if (parOptionParser.hasProcessedLocale(locale)) {
                addError(name,
                        "Duplicate localized option. Option: "
                                + parOptionParser.getCurrentOptionID()
                                + " Locale: " + locale);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_OPTION_ID,
                        parOptionParser.getCurrentOptionID(), locale);
                return;
            }

            cleanBuffer();
        } else if (ELEMENT_TECHNICAL_SERVICES.equals(name)) {
            // avoid reporting an error for this element
        } else if (ELEMENT_OPTION.equals(name)) {
            String values[] = getMandatoryValues(name, atts, ATTRIBUTE_ID);
            String optionID = values[0];

            if (!parOptionParser.hasProcessed(optionID)) {
                parOptionParser.getOrCreateOption(optionID);
            } else {
                addError(name, "Duplicate option id: " + optionID);
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_OPTION_ID,
                        optionID);
                return;
            }
        } else if (ELEMENT_OPTIONS.equals(name)) {
            // setup parameter option parser if the parameter is an enumeration
            if (parameterDef.getValueType() == ParameterValueType.ENUMERATION) {
                parOptionParser = new TechnicalProductParameterOptionImportParser(
                        parameterDef, techProduct.getTechnicalProductId(), dm,
                        localizer);
            } else {
                addError(name,
                        "Option(s) defined for non-enumeration parameter");
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_IMPLAUSIBLE_OPTIONS);
            }
        } else {
            addError(name, "Unknown element ignored!");
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_UNKNOWN_ELEMENT);
        }
    }

    /**
     * Initializes a new or updates an existing operation.
     * 
     * @param tp
     *            the {@link TechnicalProduct} the operation belongs to
     * @param id
     *            the id of the operation
     * @param actionUrl
     *            the action URL of the operation
     * 
     * 
     * @return the created or updated {@link TechnicalProductOperation}
     */
    private TechnicalProductOperation initOperation(TechnicalProduct tp,
            String id, String actionUrl) {
        TechnicalProductOperation operation = operationFinder
                .find(tp.getTechnicalProductOperations(), id);
        if (operation == null) {
            operation = new TechnicalProductOperation();
            operation.setActionUrl(actionUrl);
            operation.setOperationId(id);
            operation.setTechnicalProduct(tp);

            persist(operation);
        } else {
            TechnicalProductOperation old = operationFinder
                    .find(obsoleteOperations, id);
            if (obsoleteOperations != null) {
                obsoleteOperations.remove(old);
            }
            cleanupObsoleteOperationLocalizedObject(old.getKey());
            operation.setActionUrl(actionUrl);

        }
        operationIds.add(operation.getOperationId());
        return operation;
    }

    boolean readOperationParameter(Attributes atts, String name) {
        if (operation == null) {
            return false;
        }
        String id = getMandatoryValue(name, atts, ATTRIBUTE_ID);
        OperationParameterType type = OperationParameterType
                .valueOf(getMandatoryValue(name, atts, ATTRIBUTE_TYPE));
        boolean mandatory = false;
        String[] values = getOptionalValues(atts, ATTRIBUTE_MANDATORY);
        if (values[0] != null) {
            mandatory = Boolean.parseBoolean(values[0]);
        }
        if (operationParameterIds.contains(id)) {
            addError(name, "Duplicate operation parameter id: " + id);
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_OPERATION_PARAMETER_ID,
                    id);
            return false;
        }
        operationParameter = initOperationParameter(operation, id, mandatory,
                type);
        return true;
    }

    OperationParameter initOperationParameter(TechnicalProductOperation tpo,
            String id, boolean mandatory, OperationParameterType type) {
        OperationParameter op = operationParameterFinder
                .find(operation.getParameters(), id);
        if (op == null) {
            op = new OperationParameter();
            op.setId(id);
            op.setMandatory(mandatory);
            op.setType(type);
            op.setTechnicalProductOperation(tpo);
            persist(op);
        } else {
            OperationParameter old = operationParameterFinder
                    .find(obsoleteOperationParameters, id);
            obsoleteOperationParameters.remove(old);
            cleanupObsoleteElementLocalizedObject(old.getKey(),
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME);
            op.setId(id);
            op.setMandatory(mandatory);
            op.setType(type);
        }
        operationParameterIds.add(op.getId());
        return op;
    }

    /**
     * Remove all obsolete localized resources from the database
     * 
     * @param elementKey
     * @param type
     *            the type of localized object
     */
    private void cleanupObsoleteElementLocalizedObject(long elementKey,
            LocalizedObjectTypes type) {
        List<VOLocalizedText> oldResource = localizer
                .getLocalizedValues(elementKey, type);
        if (oldResource != null && oldResource.size() > 0) {
            // Remove all obsolete localized resources from the database
            for (VOLocalizedText lt : oldResource) {
                localizer.removeLocalizedValue(elementKey, type,
                        lt.getLocale());
            }
        }
    }

    /**
     * Remove all localized objects of obsolete role from the database
     * 
     * @param elementKey
     */
    private void cleanupObsoleteRoleLocalizedObject(long elementKey) {
        cleanupObsoleteElementLocalizedObject(elementKey,
                LocalizedObjectTypes.ROLE_DEF_DESC);
        cleanupObsoleteElementLocalizedObject(elementKey,
                LocalizedObjectTypes.ROLE_DEF_NAME);
    }

    /**
     * Remove all localized objects of obsolete event from the database
     * 
     * @param elementKey
     */
    private void cleanupObsoleteEventLocalizedObject(long elementKey) {
        cleanupObsoleteElementLocalizedObject(elementKey,
                LocalizedObjectTypes.EVENT_DESC);
    }

    /**
     * Remove all localized objects of obsolete operation from the database
     * 
     * @param elementKey
     */
    private void cleanupObsoleteOperationLocalizedObject(long elementKey) {
        cleanupObsoleteElementLocalizedObject(elementKey,
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION);
        cleanupObsoleteElementLocalizedObject(elementKey,
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME);
    }

    /**
     * Update an existing or create a new role definition for the technical
     * product.
     * 
     * @param tp
     *            the technical product
     * @param id
     *            the role id
     * @return the created or updated role
     */
    private RoleDefinition initRole(TechnicalProduct tp, String id) {
        RoleDefinition role = roleDefFinder.find(tp.getRoleDefinitions(), id);
        if (role == null) {
            role = new RoleDefinition();
            role.setRoleId(id);
            role.setTechnicalProduct(tp);
            persist(role);
        } else {
            RoleDefinition oldRole = roleDefFinder.find(obsoleteRoles, id);
            if (obsoleteRoles != null) {
                obsoleteRoles.remove(oldRole);
            }
            cleanupObsoleteRoleLocalizedObject(oldRole.getKey());
        }
        roleIds.add(role.getRoleId());
        return role;
    }

    private void cleanBuffer() {
        if (text == null) {
            text = new StringBuffer();
        } else {
            text.setLength(0);
        }
    }

    @Override
    public void endElement(String uri, String name, String qName)
            throws SAXException {
        debug(name, "endElement");
        if (!isXmlValid()) {
            return;
        }
        if (isBlank(locale)) {
            locale = DEFAULT_LOCALE;
        }
        if (ELEMENT_TECHNICAL_SERVICE.equals(name)) {
            if (!obsoleteEvents.isEmpty() || !obsoleteParameterDefs.isEmpty()
                    || !obsoleteRoles.isEmpty()) {
                // if we have an obsolete event, parameter or role we must
                // delete all referenced (marketable) products
                TechnicalProductCleaner cleaner = new TechnicalProductCleaner(
                        dm, tenantProvisioning);
                try {
                    cleaner.cleanupTechnicalProduct(techProduct);
                } catch (DeletionConstraintException e) {
                    UpdateConstraintException uce = new UpdateConstraintException(
                            ClassEnum.TECHNICAL_SERVICE,
                            techProduct.getTechnicalProductId());
                    setAppException(uce);
                    throw new SAXException(uce);
                }
            }
            for (Event event : obsoleteEvents) {
                dm.remove(event);
            }
            for (ParameterDefinition paramDef : obsoleteParameterDefs) {
                dm.remove(paramDef);
            }
            for (RoleDefinition role : obsoleteRoles) {

                dm.remove(role);
            }
            for (TechnicalProductOperation op : obsoleteOperations) {
                dm.remove(op);
            }

            try {
                // Update tags
                tagService.updateTags(techProduct, null, techProductTags);
            } catch (ValidationException e) {
                setAppException(e);
                throw new SAXException(e);
            }
            checkMandatoryAccessInfo();
            checkEventBasedProvisioning();
            persist(techProduct);

            // if the organization is supplier and technology provider, it
            // must be able to use its own products, so register it as supplier
            // for itself.
            if (org.hasRole(OrganizationRoleType.SUPPLIER)
                    && org.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
                try {
                    ms.addMarketingPermission(org, techProduct.getKey(),
                            Collections.singletonList(org.getOrganizationId()));
                } catch (ObjectNotFoundException e) {
                    // should not happen here
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                            String.valueOf(techProduct.getKey()),
                            org.getOrganizationId());
                } catch (AddMarketingPermissionException e) {
                    // should not happen here
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                            String.valueOf(techProduct.getKey()),
                            org.getOrganizationId());
                }
            }
            clearMembers();
        } else if (ELEMENT_ACCESS_INFO.equals(name)) {

            String value = text.toString().trim();
            if ((techProduct.getAccessType() == ServiceAccessType.DIRECT
                    || techProduct.getAccessType() == ServiceAccessType.USER)
                    && isBlank(value)
                    && locale.equals(Locale.ENGLISH.getLanguage())) {
                addError(name,
                        ELEMENT_ACCESS_INFO + " is required if "
                                + ATTRIBUTE_ACCESS_TYPE + " is "
                                + techProduct.getAccessType().name());
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_ACCESSINFO_NEEDED,
                        techProduct.getAccessType().name());
            }
            localizer.storeLocalizedResource(locale, techProduct.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC, value);
        } else if (ELEMENT_LOCALIZED_DESCRIPTION.equals(name)) {
            if (isBlank(text)) {
                return;
            }
            long key;
            LocalizedObjectTypes type;
            if (event != null) {
                key = event.getKey();
                type = LocalizedObjectTypes.EVENT_DESC;
            } else if (parameterDef != null) {
                key = parameterDef.getKey();
                type = LocalizedObjectTypes.PARAMETER_DEF_DESC;
            } else if (role != null) {
                key = role.getKey();
                type = LocalizedObjectTypes.ROLE_DEF_DESC;
            } else if (operation != null) {
                key = operation.getKey();
                type = LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION;
            } else if (techProduct != null) {
                key = techProduct.getKey();
                type = LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC;
            } else {
                addError(name,
                        "Missing context element, ignoring description!");
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_MISSING_CONTEXT_ELEMENT,
                        "description");
                return;
            }
            localizer.storeLocalizedResource(locale, key, type,
                    text.toString().trim());
        } else if (ELEMENT_LOCALIZED_NAME.equals(name)) {
            long key;
            LocalizedObjectTypes type;
            if (role != null) {
                key = role.getKey();
                type = LocalizedObjectTypes.ROLE_DEF_NAME;
            } else if (operationParameter != null) {
                key = operationParameter.getKey();
                type = LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME;
            } else if (operation != null) {
                key = operation.getKey();
                type = LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME;
            } else {
                addError(name, "Missing context element, ignoring name!");
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_MISSING_CONTEXT_ELEMENT,
                        "name");
                return;
            }
            try {
                BLValidator.isName(name, text.toString(), false);
            } catch (ValidationException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }
            localizer.storeLocalizedResource(locale, key, type,
                    text.toString().trim());
        } else if (ELEMENT_LOCALIZED_LICENSE.equals(name)) {
            String newText = text.toString().trim();
            String oldText = localizer.getLocalizedTextFromDatabase(locale,
                    techProduct.getKey(),
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
            ProductLicenseValidator.validate(techProduct, oldText, newText);
            localizer.storeLocalizedResource(locale, techProduct.getKey(),
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC, newText);

        } else if (ELEMENT_LOCALIZED_TAG.equals(name)) {
            try {
                // Create tag entity
                Tag newTag = TagAssembler.toTag(locale, text.toString());

                // Check whether it's already defined
                if (isTagInList(techProductTags, newTag)) {
                    addError(name, "Duplicate tag " + newTag.getValue()
                            + " for locale " + locale);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_DUPULICATE_TAG,
                            newTag.getValue(), locale);
                    return;
                }

                // Add next tag to internal list
                techProductTags.add(newTag);
            } catch (ValidationException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }

        } else if (ELEMENT_EVENT.equals(name)) {
            event = null;
        } else if (ELEMENT_ROLE.equals(name)) {
            role = null;
        } else if (ELEMENT_OPERATION_PARAMETER.equals(name)) {
            operationParameter = null;
        } else if (ELEMENT_OPERATION.equals(name)) {
            // delete obsolete operation parameters
            for (OperationParameter op : obsoleteOperationParameters) {
                operation.getParameters().remove(op);
                dm.remove(op);
            }
            operation = null;
            // clear option parameter ids for next operation
            operationParameterIds.clear();
        } else if (ELEMENT_PARAMETER_DEFINITION.equals(name)) {
            parameterParser.setTempDefaultValueForEnumeration(null);
            parameterDef = null;
        } else if (ELEMENT_OPTION.equals(name)) {
            try {
                parOptionParser.finishOption();
            } catch (UpdateConstraintException e) {
                setAppException(e);
                throw new SAXException(e);
            }
        } else if (ELEMENT_LOCALIZED_OPTION.equals(name)) {
            parOptionParser.processLocalizedOption(locale,
                    text.toString().trim());
        } else if (ELEMENT_OPTIONS.equals(name)) {
            try {
                // Remove the obsolete options from the database
                parOptionParser.finishOptions();

                // Check if the default option is one of the processed options
                // and store it
                String defaultOptionID = parameterParser
                        .getTempDefaultValueForEnumeration();
                if (!parOptionParser.storeDefaultOption(defaultOptionID)) {
                    String message = String.format(
                            "Invalid default value '%s'. Default value must be the id of one of the options.",
                            defaultOptionID);
                    addError(name, message);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IMPORT_PARSER_ERROR_INVALID_DEFAULT_VALUE,
                            defaultOptionID);
                }

                parOptionParser = null;
            } catch (UpdateConstraintException e) {
                setAppException(e);
                throw new SAXException(e);
            }
        }
    }

    /**
     * Clears all member variables and sets the technical product null. Required
     * in order to handle a new technical service.
     */
    private void clearMembers() {
        techProduct = null;
        operationIds.clear();
        operationParameterIds.clear();
        roleIds.clear();
        eventIds.clear();
        parameterIds.clear();
        techProductTags.clear();
    }

    /**
     * Create the platform events and parameter definitions if they don't exist
     * (which should not be the case because we don't set a description).
     */
    private void init() {
        initPlatformEvent(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
        initPlatformEvent(PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE);
        initPlatformParameterDefinition(
                PlatformParameterIdentifiers.CONCURRENT_USER,
                ParameterValueType.LONG);
        initPlatformParameterDefinition(PlatformParameterIdentifiers.NAMED_USER,
                ParameterValueType.LONG);
        initPlatformParameterDefinition(PlatformParameterIdentifiers.PERIOD,
                ParameterValueType.DURATION);
    }

    /**
     * Create a new platform event with the given event id, if the event doesn't
     * already exist.
     * 
     * @param id
     *            the event identifier
     */
    private void initPlatformEvent(String id) {
        Event event = getPlatformEvent(id);
        if (event == null) {
            event = new Event();
            event.setEventType(EventType.PLATFORM_EVENT);
            event.setEventIdentifier(id);
            persist(event);
        }
    }

    /**
     * Create a new product event, if the event doesn't already exist.
     * 
     * @param techProd
     *            the technical product to which the event belongs
     * @param id
     *            the event identifier
     * @return The initialized product event.
     */
    private Event initProductEvent(TechnicalProduct techProd, String id) {
        Event event = prodEventFinder.find(techProd.getEvents(), id);
        if (event == null) {
            event = new Event();
            event.setEventType(EventType.SERVICE_EVENT);
            event.setEventIdentifier(id);
            event.setTechnicalProduct(techProd);
            persist(event);
        } else {
            Event oldEvent = prodEventFinder.find(obsoleteEvents, id);
            if (obsoleteEvents != null) {
                obsoleteEvents.remove(oldEvent);
            }
            cleanupObsoleteEventLocalizedObject(oldEvent.getKey());
        }
        eventIds.add(event.getEventIdentifier());
        return event;
    }

    /**
     * Create a new platform parameter definition with the given id, if the
     * parameter definition doesn't already exist.
     * 
     * @param id
     *            the parameter identifier
     * @param valueType
     *            the parameter value type
     */
    private void initPlatformParameterDefinition(String id,
            ParameterValueType valueType) {
        ParameterDefinition paramDef = getPlatformParamDef(id);
        if (paramDef == null) {
            paramDef = new ParameterDefinition();
            paramDef.setParameterType(ParameterType.PLATFORM_PARAMETER);
            paramDef.setParameterId(id);
            paramDef.setValueType(valueType);
            persist(paramDef);
        }
    }

    /**
     * Create a new technical product with the given product attributes if it
     * doesn't already exist. Otherwise return the updated found technical
     * product.
     * 
     * @param id
     *            the product identifier
     * @param provisioningType
     *            The provisioning type.
     * @param provisioningUrl
     *            The provisioning URL.
     * @param provisioningVersion
     *            The provisioning Version.
     * @param accessType
     *            The access type for the technical product.
     * @param onlyOneSubscriptionAllowed
     *            True if a customer can only 'subscribe' once to the technical
     *            service.
     * 
     * @return the technical product domain object.
     * @throws SAXException
     *             Thrown in case the given organization is no technology
     *             provider or the technical product is still in use.
     * 
     */
    private TechnicalProduct initTechnicalProduct(String id,
            ProvisioningType provisioningType, String provisioningUrl,
            String provisioningVersion, ServiceAccessType accessType,
            boolean onlyOneSubscriptionAllowed, boolean allowingOnBehalfActing,
            String billingIdentifier) throws SAXException {

        // first check if the organization is a technology provider. if it's
        // not, an OperationNotPermitted will be thrown.
        if (!org.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            OperationNotPermittedException e = new OperationNotPermittedException(
                    "Not authorized");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
            setAppException(e);
            throw new SAXException(e);
        }

        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setOrganization(org);
        techProduct.setTechnicalProductId(id);
        techProduct = (TechnicalProduct) dm.find(techProduct);
        if (techProduct == null) {
            techProduct = new TechnicalProduct();
            techProduct.setOrganization(org);
            techProduct.setTechnicalProductId(id);
            techProduct.setProvisioningType(provisioningType);
            techProduct.setProvisioningURL(provisioningUrl);
            techProduct.setProvisioningVersion(provisioningVersion);
            techProduct.setAccessType(accessType);
            techProduct
                    .setOnlyOneSubscriptionAllowed(onlyOneSubscriptionAllowed);
            techProduct.setAllowingOnBehalfActing(allowingOnBehalfActing);
            techProduct.setBillingIdentifier(billingIdentifier);
            persist(techProduct);
        } else {
            if (pm.hasTechnicalProductActiveSessions(techProduct.getKey())) {
                TechnicalServiceActiveException e = new TechnicalServiceActiveException(
                        new Object[] { techProduct.getTechnicalProductId() });
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_IMPORT_TECHNICAL_PRODUCT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }
            techProduct.setProvisioningType(provisioningType);
            techProduct.setProvisioningURL(provisioningUrl);
            techProduct.setProvisioningVersion(provisioningVersion);
            techProduct.setAccessType(accessType);
            if (onlyOneSubscriptionAllowed
                    && hasMultipleSubscriptions(techProduct)) {
                TechnicalServiceMultiSubscriptions e = new TechnicalServiceMultiSubscriptions(
                        new Object[] { techProduct.getTechnicalProductId() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_TECHNICAL_PRODUCT_IMPORT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }
            techProduct
                    .setOnlyOneSubscriptionAllowed(onlyOneSubscriptionAllowed);
            if (allowingOnBehalfActing != techProduct.isAllowingOnBehalfActing()
                    && isExistingSubscriptionForTechnicalProduct(techProduct)) {
                UnchangeableAllowingOnBehalfActingException e = new UnchangeableAllowingOnBehalfActingException(
                        new Object[] { techProduct.getTechnicalProductId() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_TECHNICAL_PRODUCT_IMPORT_FAILED);
                setAppException(e);
                throw new SAXException(e);
            }
            techProduct.setAllowingOnBehalfActing(allowingOnBehalfActing);
            techProduct.setBillingIdentifier(billingIdentifier);
        }
        return techProduct;
    }

    boolean hasMultipleSubscriptions(TechnicalProduct techProduct) {
        Query query = dm.createNamedQuery(
                "Subscription.organizationsWithMoreThanOneVisibleSubscription");
        query.setParameter("productKey", Long.valueOf(techProduct.getKey()));
        long result = ((Long) query.getSingleResult()).longValue();
        return result > 0;
    }

    boolean isExistingSubscriptionForTechnicalProduct(
            TechnicalProduct techProduct) {
        Query query = dm.createNamedQuery(
                "Subscription.numberOfVisibleSubscriptionsForTechnicalProduct");
        query.setParameter("productKey", Long.valueOf(techProduct.getKey()));
        long result = ((Long) query.getSingleResult()).longValue();
        return result > 0;
    }

    BillingAdapter getDefaultBillingAdapter() {
        Query query = dm.createNamedQuery("BillingAdapter.getDefaultAdapter");
        try {
            BillingAdapter defaultAdapter = (BillingAdapter) query
                    .getSingleResult();
            return defaultAdapter;
        } catch (NonUniqueResultException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "More than one default billing adapter were found", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_MULTIPLE_DEFAULT_BILLING_ADAPTER_FOUND);
            throw se;
        }
    }

    /**
     * Checks whether given tag is already within the given list of tags
     * 
     * @param list
     *            the list of all tags which should be checked
     * @param tag
     *            the tag to search for
     * @return the found element assignment or NULL if not found
     */
    private boolean isTagInList(final List<Tag> list, Tag tag) {
        Iterator<Tag> iter = list.iterator();
        while (iter.hasNext()) {
            Tag exTag = iter.next();
            if (exTag.getLocale().equals(tag.getLocale())
                    && exTag.getValue().equals(tag.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String removeEndingSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    String getValidatedBillingID(Attributes atts, String techProductId)
            throws SAXException {
        String billingID = atts.getValue(ATTRIBUTE_BILLING_IDENTIFIER);
        if (billingID != null) {
            billingID = billingID.trim();
        } else {
            billingID = "";
        }

        return getBillingAdapter(billingID, techProductId)
                .getBillingIdentifier();

    }

    BillingAdapter getBillingAdapter(String billingIdentifier,
            String techProductId) throws SAXException {
        BillingAdapter billingAdapter = new BillingAdapter();
        if (billingIdentifier.equals("")) {
            billingAdapter = getDefaultBillingAdapter();
        } else {
            billingAdapter.setBillingIdentifier(billingIdentifier);
            billingAdapter = (BillingAdapter) dm.find(billingAdapter);
        }
        if (billingAdapter == null) {
            BillingAdapterNotFoundException e = new BillingAdapterNotFoundException(
                    new Object[] { billingIdentifier });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECHNICAL_PRODUCT_IMPORT_FAILED,
                    techProductId);
            setAppException(e);
            throw new SAXException(e);
        }
        return billingAdapter;
    }

}
