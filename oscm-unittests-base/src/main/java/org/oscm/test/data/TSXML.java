/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                           
 *                                                                              
 *  Creation Date: 25.08.2010                                                      
 *                                                                              
 *  Completion Time: 25.08.2010                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Utility class providing methods for technical product xml creation.
 * 
 * @author weiser
 * 
 */
public class TSXML {

    private static final String VERSION = "1.9";
    private static final String TNS = "tns:";

    private static final String TARGET_NAMESPACE = "xmlns:tns=\"oscm.serviceprovisioning/"
            + VERSION + "/TechnicalService.xsd\"";
    private static final String PARAM_TEMPLATE = "<ParameterDefinition id=\"%s\" valueType=\"%s\" minValue=\"%s\" maxValue=\"%s\" default=\"%s\" configurable=\"%s\" mandatory=\"%s\">";
    private static final String ACCESS_INFO = "<AccessInfo locale=\"en\">AccessInfo</AccessInfo>";
    private static final String LOCALIZED_DESCR = "<LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>";
    private static final String LOCALIZED_LICENSE = "<LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>";
    private static final String TECHNICAL_SERVICES_START = "<" + TNS
            + "TechnicalServices " + TARGET_NAMESPACE + ">";
    private static final String TECHNICAL_SERVICES_END = "</" + TNS
            + "TechnicalServices>";
    private static final String TECHNICAL_SERVICE_START = " <"
            + TNS
            + "TechnicalService id=\"tp1\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
            + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
            + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
            + " provisioningUsername=\"user\" provisioningPassword=\"pwd\" billingIdentifier=\""
            + BillingAdapterIdentifier.NATIVE_BILLING.toString() + "\">";
    private static final String TECHNICAL_SERVICE_END = "</" + TNS
            + "TechnicalService>";

    private static final String XML_FIRST_PART = TECHNICAL_SERVICES_START
            + TECHNICAL_SERVICE_START + ACCESS_INFO + LOCALIZED_DESCR
            + LOCALIZED_LICENSE;

    private static final String XML_LAST_PART = TECHNICAL_SERVICE_END
            + TECHNICAL_SERVICES_END;

    private TSXML() {
    }

    public static String getStartXML() {
        return "<" + TNS + "TechnicalServices " + TARGET_NAMESPACE + ">";
    }

    public static String getEndXML() {
        return "</" + TNS + "TechnicalServices>";
    }

    public static String getStartXMLTechnicalService(String technicalServiceId) {
        return " <"
                + TNS
                + "TechnicalService id=\""
                + technicalServiceId
                + "\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
                + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
                + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\">"
                + " <AccessInfo locale=\"en\">AccessInfo</AccessInfo>"
                + " <LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
                + " <LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>";
    }

    public static String getEndXMLTechnicalService() {
        return "</" + TNS + "TechnicalService>";
    }

    public static String createTSXMLWithOperations(String[] operationIds,
            String[] actionUrls, String[] locales) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyOperationsAndOperationParams(operationIds,
                actionUrls, locales, new String[0][0]));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLWithOperationsAndOperationParams(
            String[] operationIds, String[] actionUrls, String[] locales,
            String[][] parameterAtts) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyOperationsAndOperationParams(operationIds,
                actionUrls, locales, parameterAtts));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    static String createTSXMLOnlyOperationsAndOperationParams(
            String[] operationIds, String[] actionUrls, String[] locales,
            String[][] parameterAtts) {
        StringBuffer xml = new StringBuffer();
        for (int index = 0; index < operationIds.length; index++) {
            String id = operationIds[index];
            String actionUrl = actionUrls[index];
            xml.append("<Operation id=\"" + id + "\" actionURL=\"" + actionUrl
                    + "\">");
            for (String locale : locales) {
                xml.append("<LocalizedName locale=\"" + locale + "\">name_"
                        + locale + "</LocalizedName>");
            }
            for (String locale : locales) {
                xml.append("<LocalizedDescription locale=\"" + locale
                        + "\">description_" + locale
                        + "</LocalizedDescription>");
            }
            if (parameterAtts.length > 0) {
                String[] param = parameterAtts[index];
                for (String value : param) {
                    String[] split = value.split(":");
                    xml.append("<OperationParameter id=\"" + split[0]
                            + "\" type=\"" + split[2] + "\" mandatory=\""
                            + split[1] + "\">");
                    for (String locale : locales) {
                        xml.append("<LocalizedName locale=\"" + locale
                                + "\">parametername_" + locale
                                + "</LocalizedName>");
                    }
                    xml.append("</OperationParameter>");
                }
            }
            xml.append("</Operation>");
        }
        return xml.toString();
    }

    public static String createTSXMLWithRoles(String[] roleIds, String[] locales) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyRoles(roleIds, locales));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLOnlyRoles(String[] roleIds, String[] locales) {
        StringBuffer xml = new StringBuffer();
        for (String id : roleIds) {
            xml.append("<Role id=\"" + id + "\">");
            for (String locale : locales) {
                xml.append("<LocalizedName locale=\"" + locale + "\">name_"
                        + locale + "</LocalizedName>");
            }
            for (String locale : locales) {
                xml.append("<LocalizedDescription locale=\"" + locale
                        + "\">description_" + locale
                        + "</LocalizedDescription>");
            }
            xml.append("</Role>");
        }
        return xml.toString();
    }

    public static String createTSXMLWithEvents(String[] eventIds,
            String[] locales) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyEvents(eventIds, locales));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLOnlyEvents(String[] eventIds,
            String[] locales) {
        StringBuffer xml = new StringBuffer();
        for (String id : eventIds) {
            xml.append("<Event id=\"" + id + "\">");
            for (String locale : locales) {
                xml.append("<LocalizedDescription locale=\"" + locale
                        + "\">description_" + locale
                        + "</LocalizedDescription>");
            }
            xml.append("</Event>");
        }
        return xml.toString();
    }

    public static String createTSXMLWithSubscriptionRestriction(
            String booleanValue) {
        String xml = "<"
                + TNS
                + "TechnicalServices "
                + TARGET_NAMESPACE
                + ">"
                + " <"
                + TNS
                + "TechnicalService id=\"tp1\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
                + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
                + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\" onlyOneSubscriptionPerUser=\""
                + booleanValue
                + "\">"
                + " <AccessInfo locale=\"en\">AccessInfo</AccessInfo>"
                + " <LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
                + " <LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
                + "</" + TNS + "TechnicalService></" + TNS
                + "TechnicalServices>";
        return xml;
    }

    public static String createTSXMLWithAllowingOnBehalfActing(
            String booleanValue) {
        String xml = "<"
                + TNS
                + "TechnicalServices "
                + TARGET_NAMESPACE
                + ">"
                + " <"
                + TNS
                + "TechnicalService id=\"tp1\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
                + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
                + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\" allowingOnBehalfActing=\""
                + booleanValue
                + "\">"
                + " <AccessInfo locale=\"en\">AccessInfo</AccessInfo>"
                + " <LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
                + " <LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
                + "</" + TNS + "TechnicalService></" + TNS
                + "TechnicalServices>";
        return xml;
    }

    public static String createTSXMLWithParameters(String[] parameterIds,
            ParameterValueType[] types, String[] min, String[] max,
            String[] defaultValue, boolean[] configurable, boolean[] mandatory,
            String[] optionIds, String[] locales) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyParameters(parameterIds, types, min, max,
                defaultValue, configurable, mandatory, optionIds, locales));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLOnlyParameters(String[] parameterIds,
            ParameterValueType[] types, String[] min, String[] max,
            String[] defaultValue, boolean[] configurable, boolean[] mandatory,
            String[] optionIds, String[] locales) {
        StringBuffer xml = new StringBuffer();
        for (int i = 0; i < parameterIds.length; i++) {
            xml.append(String.format(PARAM_TEMPLATE, parameterIds[i],
                    types[i].name(), min[i], max[i], defaultValue[i],
                    String.valueOf(configurable[i]),
                    String.valueOf(mandatory[i])));
            if (ParameterValueType.ENUMERATION == types[i]) {
                xml.append("<Options>");
                for (String optId : optionIds) {
                    xml.append("<Option id=\"" + optId + "\">");
                    for (String locale : locales) {
                        xml.append("<LocalizedOption locale=\"" + locale
                                + "\">Description_" + locale
                                + "</LocalizedOption>");
                    }
                    xml.append("</Option>");
                }
                xml.append("</Options>");
            }
            for (String locale : locales) {
                xml.append("<LocalizedDescription locale=\"" + locale
                        + "\">Description_" + locale
                        + "</LocalizedDescription>");
            }
            xml.append("</ParameterDefinition>");
        }
        return xml.toString();
    }

    public static String createTSXMLWithEventAndParameter(String paramId,
            String paramValueType, String min, String max, String defaultValue,
            String configurable, String mandatory, String eventId,
            String[] locale) {
        String xml = "<"
                + TNS
                + "TechnicalServices "
                + TARGET_NAMESPACE
                + ">"
                + " <"
                + TNS
                + "TechnicalService id=\"ssh\" build=\"1000\" provisioningType=\"SYNCHRONOUS\" provisioningUrl=\"http://estadmue:8089/example-dev/services/ProvisioningService?wsdl\" provisioningVersion=\"1.0\" accessType=\"DIRECT\" baseUrl=\"\" loginPath=\"loginpath\">"
                + " <AccessInfo locale=\"en\">AccessInfo</AccessInfo>"
                + " <LocalizedDescription locale=\"en\">LocalizedDescription</LocalizedDescription>"
                + " <LocalizedLicense locale=\"en\">LocalizedLicense</LocalizedLicense>"
                + " <ParameterDefinition id=\"%s\" valueType=\"%s\" minValue=\"%s\" maxValue=\"%s\" default=\"%s\" configurable=\"%s\" mandatory=\"%s\">";
        if (ParameterValueType.ENUMERATION.name().equals(paramValueType)) {
            xml += "<Options><Option id=\"1\">" + "<LocalizedOption  locale=\""
                    + locale[2] + "\">Minimum Storage</LocalizedOption>"
                    + "</Option><Option id=\"2\">"
                    + "<LocalizedOption  locale=\"" + locale[2]
                    + "\">Optimum storage</LocalizedOption>"
                    + "</Option></Options>";
        }
        xml += " <LocalizedDescription locale=\"" + locale[0]
                + "\">LocalizedDescription_en</LocalizedDescription>"
                + " </ParameterDefinition><Event id=\"%s\">"
                + " <LocalizedDescription locale=\"" + locale[1]
                + "\">LocalizedDescription_en</LocalizedDescription>"
                + " </Event></" + TNS + "TechnicalService></" + TNS
                + "TechnicalServices>";
        return String.format(xml, paramId, paramValueType, min, max,
                defaultValue, configurable, mandatory, eventId);
    }

    public static String createTSXML(String id, String build, String provType,
            String provUrl, String provVersion, String accessType,
            String baseUrl, String loginPath, String[] locale) {

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<"
                + TNS
                + "TechnicalServices "
                + TARGET_NAMESPACE
                + ">"
                + " <"
                + TNS
                + "TechnicalService id=\"%s\" build=\"%s\" provisioningType=\"%s\""
                + " provisioningUrl=\"%s\" provisioningVersion=\"%s\" accessType=\"%s\" baseUrl=\"%s\""
                + " loginPath=\"%s\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\">"

                + " <AccessInfo locale=\"" + locale[0]
                + "\">AccessInfo</AccessInfo>"

                + " <LocalizedDescription locale=\"" + locale[1]
                + "\">LocalizedDescription</LocalizedDescription>"

                + " <LocalizedLicense locale=\"" + locale[2]
                + "\">LocalizedLicense</LocalizedLicense>"

                + " </" + TNS + "TechnicalService> </" + TNS
                + "TechnicalServices>";
        return String.format(xml, id, build, provType, provUrl, provVersion,
                accessType, baseUrl, loginPath);
    }

    public static String createTSXMLWithTags(String[] tagValues,
            String[] locales) {
        StringBuffer xml = new StringBuffer(XML_FIRST_PART);
        xml.append(createTSXMLOnlyTags(tagValues, locales));
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLOnlyTags(String[] tagValues,
            String[] locales) {
        StringBuffer xml = new StringBuffer();
        for (int i = 0; i < tagValues.length; i++) {
            xml.append("<LocalizedTag locale=\"" + locales[i] + "\">"
                    + tagValues[i] + "</LocalizedTag>");
        }
        return xml.toString();
    }

    public static String createTSXMLWithBillingIdentifier(
            String billingIdentifier) {
        StringBuffer xml = new StringBuffer(TECHNICAL_SERVICES_START);
        xml.append("<"
                + TNS
                + "TechnicalService id=\"tp1\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
                + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
                + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\" billingIdentifier=\""
                + billingIdentifier + "\">");
        xml.append(ACCESS_INFO + LOCALIZED_DESCR + LOCALIZED_LICENSE);
        xml.append(XML_LAST_PART);
        return xml.toString();
    }

    public static String createTSXMLWithOutBillingIdentifier() {
        StringBuffer xml = new StringBuffer(TECHNICAL_SERVICES_START);
        xml.append("<"
                + TNS
                + "TechnicalService id=\"tp1\" build=\"1\" provisioningType=\"SYNCHRONOUS\""
                + " provisioningUrl=\"http://someurl\" provisioningVersion=\"1.0\" accessType=\"LOGIN\" baseUrl=\"http://someurl\""
                + " loginPath=\"loginpath\" provisioningTimeout=\"50000\""
                + " provisioningUsername=\"user\" provisioningPassword=\"pwd\""
                + " >");
        xml.append(ACCESS_INFO + LOCALIZED_DESCR + LOCALIZED_LICENSE);
        xml.append(XML_LAST_PART);
        return xml.toString();
    }
}
