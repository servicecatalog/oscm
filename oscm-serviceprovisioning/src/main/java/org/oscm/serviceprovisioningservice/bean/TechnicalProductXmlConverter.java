/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.01.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.TechnicalProductTag;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * This class provides methods for converting technical product domain objects
 * to the xml structure that can be imported.
 * 
 * @author weiser
 * 
 */
public class TechnicalProductXmlConverter {

    private static String TECHNICAL_SERVICE_XSD = "oscm.serviceprovisioning/1.9/TechnicalService.xsd";

    /**
     * Converts the given technical products to a dom document.
     * 
     * @param techProds
     *            the technical products to convert
     * @param localizer
     *            the localizer used to read localized values
     * @param dm
     *            the data service used to read other values
     * @return the dom document
     */
    public Document technicalProductToXmlDocument(
            List<TechnicalProduct> techProds, LocalizerServiceLocal localizer,
            DataService dm) {
        return technicalProductToXmlDocumentInt(techProds, localizer)
                .getDomDocument();
    }

    XmlDocument technicalProductToXmlDocumentInt(
            List<TechnicalProduct> techProds, LocalizerServiceLocal localizer) {
        XmlDocument doc = new XmlDocument(
                TECHNICAL_SERVICE_XSD,
                "tns",
                TECHNICAL_SERVICE_XSD
                        + " ../../oscm-serviceprovisioning/javares/TechnicalServices.xsd",
                "TechnicalServices");

        for (TechnicalProduct technicalProduct : techProds) {
            insertTechnicalProduct(doc, localizer, technicalProduct);
        }

        return doc;
    }

    /**
     * Converts the given technical products to a xml string.
     * 
     * @param techProds
     *            the technical products to convert
     * @param localizer
     *            the localizer used to read localized values
     * @param dm
     *            the data service used to read other values
     * @return the xml
     */
    public byte[] technicalProductToXml(List<TechnicalProduct> techProds,
            LocalizerServiceLocal localizer, DataService dm) {
        XmlDocument xml = technicalProductToXmlDocumentInt(techProds, localizer);
        return xml.docToXml();
    }

    /**
     * Creates a node for each technical product including its
     * <ul>
     * <li>attributes</li>
     * <li>localized values</li>
     * <li>parameters</li>
     * <li>events</li>
     * </ul>
     * and appends it to the given parent node.
     * 
     * @param xmldoc
     *            the dom document used to create nodes
     * @param root
     *            the root node
     * @param localizer
     *            the localizer
     * @param technicalProduct
     *            the technical product
     */
    void insertTechnicalProduct(XmlDocument doc,
            LocalizerServiceLocal localizer, TechnicalProduct technicalProduct) {
        Document xmldoc = doc.getDomDocument();
        Element tp = xmldoc.createElement("TechnicalService");

        // workaround to fix the name space of the element
        tp = (Element) doc.getDomDocument().renameNode(tp,
                TECHNICAL_SERVICE_XSD, tp.getNodeName());
        tp.setPrefix("tns");

        doc.getRootNode().appendChild(tp);
        tp.setAttribute("id", technicalProduct.getTechnicalProductId());
        tp.setAttribute("build", technicalProduct.getTechnicalProductBuildId());
        tp.setAttribute("provisioningType", technicalProduct
                .getProvisioningType().name());
        tp.setAttribute("provisioningUrl",
                technicalProduct.getProvisioningURL());
        tp.setAttribute("provisioningVersion",
                technicalProduct.getProvisioningVersion());
        tp.setAttribute("provisioningUsername",
                technicalProduct.getProvisioningUsername());
        tp.setAttribute("provisioningPassword",
                technicalProduct.getProvisioningPassword());
        tp.setAttribute("accessType", technicalProduct.getAccessType().name());
        tp.setAttribute("baseUrl", technicalProduct.getBaseURL());
        tp.setAttribute("loginPath", technicalProduct.getLoginPath());
        tp.setAttribute("onlyOneSubscriptionPerUser",
                String.valueOf(technicalProduct.isOnlyOneSubscriptionAllowed()));
        tp.setAttribute("allowingOnBehalfActing",
                String.valueOf(technicalProduct.isAllowingOnBehalfActing()));
        tp.setAttribute("billingIdentifier",
                String.valueOf(technicalProduct.getBillingIdentifier()));

        appendLocalizedValues(xmldoc, localizer, tp,
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                technicalProduct.getKey(), "AccessInfo");
        appendLocalizedValues(xmldoc, localizer, tp,
                LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC,
                technicalProduct.getKey(), "LocalizedDescription");
        appendLocalizedValues(xmldoc, localizer, tp,
                LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                technicalProduct.getKey(), "LocalizedLicense");
        appendLocalizedTags(xmldoc, tp, technicalProduct);
        appendParameters(xmldoc, tp, localizer, technicalProduct);
        appendEvents(xmldoc, tp, localizer, technicalProduct);
        appendRoles(xmldoc, tp, localizer, technicalProduct);
        appendOperations(xmldoc, tp, localizer, technicalProduct);
    }

    /**
     * Appends the parameter nodes to the technical product node including its
     * localized values.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param parent
     *            the parent node
     * @param localizer
     *            the localizer
     * @param technicalProduct
     *            the technical product
     */
    void appendParameters(Document xmldoc, Element parent,
            LocalizerServiceLocal localizer, TechnicalProduct technicalProduct) {
        List<ParameterDefinition> definitions = technicalProduct
                .getParameterDefinitions();
        for (ParameterDefinition def : definitions) {
            Element defNode = xmldoc.createElement("ParameterDefinition");
            defNode.setAttribute("id", def.getParameterId());
            defNode.setAttribute("valueType", def.getValueType().name());
            defNode.setAttribute("mandatory", String.valueOf(def.isMandatory()));
            defNode.setAttribute("configurable",
                    String.valueOf(def.isConfigurable()));
            Long minimumValue = def.getMinimumValue();
            if (minimumValue != null) {
                defNode.setAttribute("minValue",
                        String.valueOf(minimumValue.longValue()));
            }
            Long maximumValue = def.getMaximumValue();
            if (maximumValue != null) {
                defNode.setAttribute("maxValue",
                        String.valueOf(maximumValue.longValue()));
            }
            String defaultValue = def.getDefaultValue();
            if (defaultValue != null) {
                defNode.setAttribute("default", defaultValue);
            }
            ParameterModificationType modificationType = def
                    .getModificationType();
            if (modificationType == ParameterModificationType.ONE_TIME) {
                defNode.setAttribute("modificationType",
                        modificationType.name());
            }
            parent.appendChild(defNode);
            appendOptions(xmldoc, localizer, def, defNode);
            appendLocalizedValues(xmldoc, localizer, defNode,
                    LocalizedObjectTypes.PARAMETER_DEF_DESC, def.getKey(),
                    "LocalizedDescription");
        }
    }

    /**
     * Appends the existing options to a parameter definition.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param localizer
     *            the localizer
     * @param def
     *            the parameter definition domain object
     * @param defNode
     *            the parameter definition node
     */
    void appendOptions(Document xmldoc, LocalizerServiceLocal localizer,
            ParameterDefinition def, Element defNode) {
        List<ParameterOption> optionList = def.getOptionList();
        if (optionList == null || optionList.isEmpty()) {
            return;
        }
        Element optsNode = xmldoc.createElement("Options");
        defNode.appendChild(optsNode);
        for (ParameterOption option : optionList) {
            Element optNode = xmldoc.createElement("Option");
            optNode.setAttribute("id", option.getOptionId());
            optsNode.appendChild(optNode);
            appendLocalizedValues(xmldoc, localizer, optNode,
                    LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC,
                    option.getKey(), "LocalizedOption");
        }
    }

    /**
     * Appends the event nodes to the technical product node including its
     * localized values.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param parent
     *            the parent node
     * @param localizer
     *            the localizer
     * @param technicalProduct
     *            the technical product
     */
    void appendEvents(Document xmldoc, Element parent,
            LocalizerServiceLocal localizer, TechnicalProduct technicalProduct) {
        List<Event> events = technicalProduct.getEvents();
        for (Event event : events) {
            Element defNode = xmldoc.createElement("Event");
            defNode.setAttribute("id", event.getEventIdentifier());
            parent.appendChild(defNode);
            appendLocalizedValues(xmldoc, localizer, defNode,
                    LocalizedObjectTypes.EVENT_DESC, event.getKey(),
                    "LocalizedDescription");
        }
    }

    /**
     * Appends the operation nodes to the technical product node including its
     * localized values.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param parent
     *            the parent node
     * @param localizer
     *            the localizer
     * @param technicalProduct
     *            the technical product
     */
    void appendOperations(Document xmldoc, Element parent,
            LocalizerServiceLocal localizer, TechnicalProduct technicalProduct) {
        List<TechnicalProductOperation> ops = technicalProduct
                .getTechnicalProductOperations();
        for (TechnicalProductOperation op : ops) {
            Element defNode = xmldoc.createElement("Operation");
            defNode.setAttribute("id", op.getOperationId());
            defNode.setAttribute("actionURL", op.getActionUrl());
            parent.appendChild(defNode);
            appendLocalizedValues(xmldoc, localizer, defNode,
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME,
                    op.getKey(), "LocalizedName");
            appendLocalizedValues(
                    xmldoc,
                    localizer,
                    defNode,
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION,
                    op.getKey(), "LocalizedDescription");
            appendOperationParameters(xmldoc, defNode, localizer, op);
        }
    }

    /**
     * Appends the operation parameter nodes to the technical service operation
     * node including its localized values.
     * 
     * @param xmldoc
     *            the DOM document used to create elements
     * @param parent
     *            the parent technical service operation node
     * @param localizer
     *            the localizer
     * @param technicalProductOperation
     *            the technical service operation to get the parameters from
     */
    void appendOperationParameters(Document xmldoc, Element parent,
            LocalizerServiceLocal localizer,
            TechnicalProductOperation technicalProductOperation) {
        List<OperationParameter> parameters = technicalProductOperation
                .getParameters();
        for (OperationParameter op : parameters) {
            Element opNode = xmldoc.createElement("OperationParameter");
            opNode.setAttribute("id", op.getId());
            opNode.setAttribute("type", op.getType().name());
            opNode.setAttribute("mandatory", String.valueOf(op.isMandatory()));
            parent.appendChild(opNode);
            appendLocalizedValues(
                    xmldoc,
                    localizer,
                    opNode,
                    LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME,
                    op.getKey(), "LocalizedName");
        }
    }

    /**
     * Appends the role nodes to the technical product node including its
     * localized values.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param parent
     *            the parent node
     * @param localizer
     *            the localizer
     * @param technicalProduct
     *            the technical product
     */
    void appendRoles(Document xmldoc, Element parent,
            LocalizerServiceLocal localizer, TechnicalProduct technicalProduct) {
        List<RoleDefinition> roleDefs = technicalProduct.getRoleDefinitions();
        for (RoleDefinition role : roleDefs) {
            Element defNode = xmldoc.createElement("Role");
            defNode.setAttribute("id", role.getRoleId());
            parent.appendChild(defNode);
            appendLocalizedValues(xmldoc, localizer, defNode,
                    LocalizedObjectTypes.ROLE_DEF_NAME, role.getKey(),
                    "LocalizedName");
            appendLocalizedValues(xmldoc, localizer, defNode,
                    LocalizedObjectTypes.ROLE_DEF_DESC, role.getKey(),
                    "LocalizedDescription");
        }
    }

    /**
     * Created the nodes for the localized values of a certain type and appends
     * them to the given parent node.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param localizer
     *            the localizer
     * @param parent
     *            the parent node
     * @param localizedType
     *            the type of the value that is localized
     *            {@link LocalizedObjectTypes}
     * @param key
     *            the object key
     * @param nodeName
     *            the name of the nodes to create
     */
    void appendLocalizedValues(Document xmldoc,
            LocalizerServiceLocal localizer, Element parent,
            LocalizedObjectTypes localizedType, long key, String nodeName) {
        List<VOLocalizedText> localizedValues = localizer.getLocalizedValues(
                key, localizedType);
        for (VOLocalizedText text : localizedValues) {
            Element element = xmldoc.createElement(nodeName);
            element.setTextContent(text.getText());
            element.setAttribute("locale", text.getLocale());
            parent.appendChild(element);
        }
    }

    /**
     * Appends the parameter nodes to the technical product node including its
     * localized values.
     * 
     * @param xmldoc
     *            the dom document used to create elements
     * @param parent
     *            the parent node
     * @param tagHandler
     *            the tag handler
     * @param technicalProduct
     *            the technical product
     */
    void appendLocalizedTags(Document xmldoc, Element parent,
            TechnicalProduct technicalProduct) {
        List<TechnicalProductTag> tags = technicalProduct.getTags();
        for (TechnicalProductTag tag : tags) {
            Element element = xmldoc.createElement("LocalizedTag");
            element.setAttribute("locale", tag.getTag().getLocale());
            element.setTextContent(tag.getTag().getValue());
            parent.appendChild(element);
        }
    }
}
