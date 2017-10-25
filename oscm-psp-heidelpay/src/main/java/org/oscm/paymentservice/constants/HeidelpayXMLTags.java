/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.constants;

/**
 * Interface providing all XML element and attribute names required for
 * interaction with the heideplay XML integrator.
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface HeidelpayXMLTags {

    public static final String XML_ELEMENT_TRANSACTIONID = "TransactionID";
    public static final String XML_ELEMENT_IDENTIFICATION = "Identification";
    public static final String REQUEST_COMPLIANCE_LEVEL = "1.0";
    public static final String XML_ATTRIBUTE_REGISTRATION = "registration";
    public static final String XML_ELEMENT_ACCOUNT = "Account";
    public static final String XML_ELEMENT_USAGE = "Usage";
    public static final String XML_ELEMENT_CURRENCY = "Currency";
    public static final String XML_ELEMENT_AMOUNT = "Amount";
    public static final String XML_ELEMENT_REPRESENTATION = "Presentation";
    public static final String XML_ATTRIBUTE_CODE = "code";
    public static final String XML_ELEMENT_PAYMENT = "Payment";
    public static final String XML_ATTRIBUTE_PASSWORD = "pwd";
    public static final String XML_ATTRIBUTE_LOGIN = "login";
    public static final String XML_ELEMENT_USER = "User";
    public static final String XML_ATTRIBUTE_CHANNEL = "channel";
    public static final String XML_ATTRIBUTE_RESPONSE = "response";
    public static final String XML_ATTRIBUTE_MODE = "mode";
    public static final String XML_ELEMENT_TRANSACTION = "Transaction";
    public static final String XML_ATTRIBUTE_SENDER = "sender";
    public static final String XML_ELEMENT_SECURITY = "Security";
    public static final String XML_ELEMENT_HEADER = "Header";
    public static final String XML_ATTRIBUTE_VERSION = "version";
    public static final String XML_ELEMENT_REQUEST = "Request";
    public static final String XML_ATTRIBUTE_NAME = "name";
    public static final String XML_ELEMENT_REFERENCEID = "ReferenceID";

    // --------------------------------------------------------
    // analysis related tags

    public static final String XML_ELEMENT_ANALYSIS = "Analysis";
    public static final String XML_ANALYSIS_EMAIL = "EMAIL";
    public static final String XML_ANALYSIS_ADDRESS_COMPLETE = "ADDRESS_COMPLETE";
    public static final String XML_ANALYSIS_CURRENCY = "CURRENCY";
    public static final String XML_ANALYSIS_AMOUNT_TOTAL = "AMOUNT_TOTAL";
    public static final String XML_ANALYSIS_AMOUNT_VAT = "AMOUNT_VAT";
    public static final String XML_ANALYSIS_AMOUNT_NET = "AMOUNT_NET";
    public static final String XML_ANALYSIS_PERCENT_VAT = "PERCENT_VAT";
    public static final String XML_ANALYSIS_CRITERION = "Criterion";
    public static final String XML_ANALYSIS_AMOUNT_NET_DISCOUNT = "AMOUNT_NET_DISCOUNT";
    public static final String XML_ANALYSIS_AMOUNT_TOTAL_DISCOUNT = "AMOUNT_TOTAL_DISCOUNT";

    public static final String XML_ANALYSIS_POSITION_POSITIONNAME = "POS_ZZ.POSITION";
    public static final String XML_ANALYSIS_POSITION_QUANTITY = "POS_ZZ.QUANTITY";
    public static final String XML_ANALYSIS_POSITION_UNIT = "POS_ZZ.UNIT";
    public static final String XML_ANALYSIS_POSITION_AMOUNT_UNIT = "POS_ZZ.AMOUNT_UNIT";
    public static final String XML_ANALYSIS_POSITION_AMOUNT = "POS_ZZ.AMOUNT";
    public static final String XML_ANALYSIS_POSITION_TEXT = "POS_ZZ.TEXT";
    public static final String XML_ANALYSIS_NUMBER_PLACEHOLDER = "ZZ";
}
