/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 20 6, 2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.webtest;

public class PortalHtmlElements {

    public static final String PORTAL_TITLE = "Service Catalog Manager";
    public static final String PORTAL_INPUT_USERID = "userId";
    public static final String PORTAL_INPUT_PASSWORD = "password";
    public static final String PORTAL_BUTTON_LOGIN = "loginButton";
    public static final String PORTAL_LINK_LOGOUT = "formLogout:logout";
    public static final String PORTAL_DIV_LOGIN_FAILED = "login_warning";

    public static final String PORTAL_SPAN_ERRORS = "errorMessages:";
    public static final String PORTAL_ERRORCLASS = "rf-msgs-sum";
    public static final String PORTAL_SPAN_INFOS = "infoMessages:";
    public static final String PORTAL_INFOCLASS = "rf-msgs-sum";

    public static final String PORTAL_DIV_SHOWMESSAGE = "mainTmplMessagesPanel";
    public static final String MARKETPLACE_SPAN_SHOWMESSAGE = "globalMessagesHolder";

    public static final String PORTAL_PASSWORD_INPUT_CURRENT = "passwordform:currentPassword";
    public static final String PORTAL_PASSWORD_INPUT_CHANGE = "passwordform:password";
    public static final String PORTAL_PASSWORD_INPUT_REPEAT = "passwordform:password2";
    public static final String PORTAL_PASSWORD_BUTTON_SAVE = "passwordform:changeButtonLink";

    public static final String CREATE_MARKETPLACE_INPUT_NAME = "createMarketplaceForm:marketplaceName";
    public static final String CREATE_MARKETPLACE_INPUT_ORG_ID = "createMarketplaceForm:organizationIdInput";
    public static final String CREATE_MARKETPLACE__BUTTON_SAVE = "createMarketplaceForm:saveButtonLink";

    public static final String DELETE_MARKETPLACE_DROPDOWN_IDLIST = "marketplaceSelectForm:selectMarketplaceId";
    public static final String DELETE_MARKETPLACE_BUTTON_DELETE = "deleteMarketplaceForm:deleteButton";
    public static final String DELETE_MARKETPLACE_BUTTON_CONFIRM = "confirmForm:okButton";

    public static final String CREATE_ORGANIZATION_INPUT_ADMINEMAIL = "editForm:administratorEmail";
    public static final String CREATE_ORGANIZATION_INPUT_DESIRED_USERID = "editForm:administratorUserId";
    public static final String CREATE_ORGANIZATION_DROPDOWN_LANGUAGE = "editForm:administratorLocale";

    public static final String CREATE_ORGANIZATION_CHECKBOX_TPROVIDER = "editForm:checkboxRoleTechnologyProvider";
    public static final String CREATE_ORGANIZATION_FORM_UPLOADIMAGE = "editForm:image";
    public static final String CREATE_ORGANIZATION_CHECKBOX_SUPPLIER = "editForm:checkboxRoleSupplier";
    public static final String CREATE_ORGANIZATION_INPUT_REVENUESHARE = "editForm:operatorRevenueShare";

    public static final String CREATE_ORGANIZATION_INPUT_ORGNAME = "editForm:organizationName";
    public static final String CREATE_ORGANIZATION_INPUT_ORGEMAIL = "editForm:organizationEmail";
    public static final String CREATE_ORGANIZATION_DROPDOWN_ORGLOCALE = "editForm:organizationLocale";
    public static final String CREATE_ORGANIZATION_INPUT_ORGPHONE = "editForm:organizationPhone";
    public static final String CREATE_ORGANIZATION_INPUT_ORGURL = "editForm:organizationUrl";
    public static final String CREATE_ORGANIZATION_INPUT_ORGADDRESS = "editForm:organizationAddress";
    public static final String CREATE_ORGANIZATION_DROPDOWN_ORGCOUNTRY = "editForm:organizationCountry";
    public static final String CREATE_ORGANIZATION_BUTTON_SAVE = "editForm:saveButtonLink";

    public static final String IMPORT_TECHSERVICE_UPLOAD_INPUT = "importForm:file";
    public static final String IMPORT_TECHSERVICE_UPLOAD_BUTTON = "importForm:importButtonLink";

    public static final String UPDATE_TECHSERVICE_PARAM_TABLE = "editForm:parameterTable";
    public static final String UPDATE_TECHSERVICE_DROPDOWN_SERVICENAME = "techServiceForm:techServiceId";
    public static final String UPDATE_TECHSERVICE_BUTTONLINK_SAVE = "editForm:saveButtonLink";

    public static final String DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME = "editForm:techServiceId";
    public static final String DEFINE_MARKETSERVICE_INPUT_SERVICENAME = "editForm:serviceName";
    public static final String DEFINE_MARKETSERVICE_INPUT_SERVICEID = "editForm:serviceId";
    public static final String DEFINE_MARKETSERVICE_PARAM_TABLE = "editForm:parameterTable";
    public static final String DEFINE_MARKETSERVICE_BUTTONLINK_SAVE = "editForm:saveButtonLink";
    public static final String DEFINE_MARKETSERVICE_BUTTON_SAVE = "editForm:saveButton";

    public static final String DEFINE_PRICEMODEL_DROPDOWN_SERVICENAME = "serviceSelectForm:serviceSelection";
    public static final String DEFINE_PRICEMODEL_BUTTON_SAVE = "editForm:saveButton";
    public static final String DEFINE_PRICEMODEL_CHECKBOX_FREE_OF_CHARGE = "editForm:isfree:0";
    public static final String DEFINE_PRICEMODEL_CHECKBOX_PRO_RATE = "editForm:isfree:1";
    public static final String DEFINE_PRICEMODEL_CHECKBOX_PER_UNITE = "editForm:isfree:2";

    public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME = "editForm:selectedServiceId";
    public static final String DEFINE_PUBLISH_OPTION_DROPDOWN_MARKETPLACE = "editForm:marketplace";
    public static final String DEFINE_PUBLISH_OPTION_BUTTON_SAVE = "editForm:saveButton";

    public static final String DEACTIVATION_SERVICE_FORM = "serviceDeActivationForm";
    public static final String DEACTIVATION_SERVICE_TABLE = "serviceDeActivationForm:activationTable";
    public static final String DEACTIVATION_SERVICE_BUTTON_SAVE = "serviceDeActivationForm:deActivateButtonLink";

    public static final String REGISTER_CUSTOMER_INPUT_EMAIL = "editForm:email";
    public static final String REGISTER_CUSTOMER_INPUT_USERID = "editForm:userId";
    public static final String REGISTER_CUSTOMER_DROPDOWN_COUNTRY = "editForm:country";
    public static final String REGISTER_CUSTOMER_DROPDOWN_MARKETPLACE = "editForm:marketplace";
    public static final String REGISTER_CUSTOMER_BUTTONLINK_SAVE = "editForm:saveButtonLink";

    public static final String MARKETPLACE_PASSWORD_INPUT_CURRENT = "passwordForm:currentPassword";
    public static final String MARKETPLACE_PASSWORD_INPUT_CHANGE = "passwordForm:password";
    public static final String MARKETPLACE_PASSWORD_INPUT_REPEAT = "passwordForm:password2";
    public static final String MARKETPLACE_PASSWORD_BUTTONLINK_SAVE = "passwordForm:changeButtonLink";
    public static final String MARKETPLACE_SPAN_WELCOME = "formLogout:welcome";
    public static final String GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE = "gotoMarketplaceForm:marketplace";
    public static final String GOTO_MARKETPLACE_BUTTONLINK_GOTO = "goForm:gotoBtnLink";
    public static final String MARKETPLACE_INPUT_USERID = "loginForm:loginUserId";
    public static final String MARKETPLACE_INPUT_PASSWORD = "loginForm:loginPassword";
    public static final String MARKETPLACE_BUTTON_LOGIN = "loginForm:loginButtonLink";
    public static final String MARKETPLACE_LINK_LOGOUT = "formLogout:logout";
    public static final String MARKETPLACE_LINKTEXT_LOGIN = "Login";

    public static final String MARKETPLACE_LINK_SERVICE_NAME = "//*[@title='%s']/../../..//*[@class='serviceShowDetailsButton']/a";
    public static final String MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW = "buyme";
    public static final String MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME = "configurationForm:subscriptionIdText";
    public static final String MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER = "configurationForm:purchaseOrderNumberText";
    public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT = "configurationForm:nextLink";
    public static final String MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE = "confirmForm:agreeCheckbox";
    public static final String MARKETPLACE_SUBSCRIPTION_BUTTONLINK_CONFIRM = "confirmForm:confirmLink";

}