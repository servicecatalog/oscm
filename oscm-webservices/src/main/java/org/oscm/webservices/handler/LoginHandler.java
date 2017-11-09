/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices.handler;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.UserIdNotFoundException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;

//import com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin;
//import com.sun.xml.ws.security.opt.impl.incoming.SAMLAssertion;
//import com.sun.xml.wss.XWSSecurityException;
//import com.sun.xml.wss.impl.MessageConstants;
//import com.sun.xml.wss.saml.util.SAMLUtil;

public class LoginHandler implements SOAPHandler<SOAPMessageContext> {

    private static final String ORGANIZATION_ID_HEADER_PARAM = "organizationId";

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        if (isInboundMessage(context).booleanValue()) {

            String userKey = null;
            try {
                userKey = getUserKeyFromContext(context);
            } catch (UserIdNotFoundException | NamingException | SQLException | NotExistentTenantException exception) {
                logException(
                        exception,
                        LogMessageIdentifier.ERROR_GET_USER_FROM_SAML_RESPONSE_FAILED);
                return false;
            }

            try {
                login(userKey);
                addPrincipal(context, userKey);
            } catch (Exception exception) {
                logException(exception,
                        LogMessageIdentifier.ERROR_USER_LOGIN_FAILED);
                return false;
            }
        }
        return true;
    }

    private void logException(Exception exception,
            LogMessageIdentifier identifier) {
        Log4jLogger logger = LoggerFactory.getLogger(LoginHandler.class);
        logger.logError(Log4jLogger.SYSTEM_LOG, exception, identifier,
                exception.getClass().getName());
    }

//    private void logDebugSamlAssertion(SAMLAssertion samlAssertion) {
//        Log4jLogger logger = LoggerFactory.getLogger(LoginHandler.class);
//        String samlAssertionString = null;
//        try {
//            Element samlAssertionElement = SAMLUtil
//                    .createSAMLAssertion(samlAssertion.getSamlReader());
//            samlAssertionString = XMLConverter.convertToString(
//                    samlAssertionElement, false);
//            logger.logDebug(samlAssertionString);
//        } catch (XWSSecurityException | XMLStreamException
//                | TransformerException exception) {
//            logger.logDebug("SAML Assertion conversion failed: "
//                    + exception.getMessage());
//        }
//    }

    protected void addPrincipal(SOAPMessageContext context, String userKey) {
        Subject sub = (Subject) context.get("javax.security.auth.Subject");
        sub.getPrincipals().add(new BasicUserPrincipal(userKey));
    }

    protected void login(String userKey) throws Exception {
        final SecurityService securityService = SystemInstance.get()
                .getComponent(SecurityService.class);
        final Object token;
        try {
            securityService.disassociate();

            token = securityService.login(
                    userKey,
                    "WS" + System.currentTimeMillis());
            if (AbstractSecurityService.class.isInstance(securityService)
                    && AbstractSecurityService.class.cast(securityService)
                    .currentState() == null) {
                securityService.associate(token);
            }
        } catch (final LoginException e) {
            throw new SecurityException("cannot log user "
                    + userKey, e);
        }
        ejbLogin(userKey, "WS" + System.currentTimeMillis());
    }

    private void ejbLogin(String key, String password) throws LoginException {
        final SecurityService securityService = SystemInstance.get()
                .getComponent(SecurityService.class);
        final Object token;
        securityService.disassociate();

        token = securityService.login(key, password);
        if (AbstractSecurityService.class.isInstance(securityService)
                && AbstractSecurityService.class.cast(securityService)
                .currentState() == null) {
            securityService.associate(token);
        }
    }

    protected Boolean isInboundMessage(SOAPMessageContext context) {
        Boolean outBoundProperty = (Boolean) context
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return !outBoundProperty.booleanValue();
    }

    protected String getUserKeyFromContext(SOAPMessageContext context)
            throws UserIdNotFoundException, NamingException, SQLException, NotExistentTenantException {
        
        String userId = getUserIdFromContext(context);
        String tenantId = getTenantIdFromContext(context);
        String orgId = getOrganizationIdFromContext(context);
        
        return getUserKey(userId, orgId, tenantId);
    }
    
    private String getUserKey(String userId, String orgId, String tenantId)
            throws NamingException, SQLException {
         
        Context context = new InitialContext();
        DataSource ds = (DataSource) context.lookup("BSSDS");
        
        AbstractKeyQuery keyQuery = null;
        
        if (StringUtils.isNotEmpty(tenantId)) {

            VOConfigurationSetting setting = getConfigService(context)
                    .getVOConfigurationSetting(
                            ConfigurationKey.SSO_DEFAULT_TENANT_ID,
                            Configuration.GLOBAL_CONTEXT);
            String defaultTenantId = setting.getValue();

            if (tenantId.equals(defaultTenantId)) {
                keyQuery = new UserKeyQuery(ds, userId);
            } else {
                keyQuery = new UserKeyForTenantQuery(ds, userId,
                        tenantId);
            }

        } else if (StringUtils.isNotEmpty(orgId)) {
            keyQuery = new UserKeyForOrganizationQuery(ds, userId,
                    orgId);
        } 

        keyQuery.execute();
        long userKey = keyQuery.getKey();
        
        if (userKey == 0) {
            throw new SQLException(
                    "User not found [user id: " + userId + ", tenant id: "
                            + tenantId + ", orgaznization id: " + orgId + " ]");
        }
        
        return String.valueOf(userKey);
    }

    protected String getUserIdFromContext(SOAPMessageContext context)
            throws UserIdNotFoundException {
        
//        String userId;
//
//        SAMLAssertion samlAssertion = getSamlAssertion(context);
//        SAMLResponseExtractor samlResponseExtractor = new SAMLResponseExtractor();
//        userId = samlResponseExtractor.getUserId(samlAssertion);
//        return userId;
        return "";
    }
    
    protected String getTenantIdFromContext(SOAPMessageContext context) throws NotExistentTenantException {
        
        String tenantId;
        
//        SAMLAssertion samlAssertion = getSamlAssertion(context);
//        SAMLResponseExtractor samlResponseExtractor = new SAMLResponseExtractor();
//        tenantId = samlResponseExtractor.getTenantId(samlAssertion);
//
//        if(StringUtils.isEmpty(tenantId)){
//            throw new NotExistentTenantException(Reason.MISSING_TEANT_ID_IN_SAML);
//        }
        
//        return tenantId;
        return "";
    }
    
    protected String getOrganizationIdFromContext(SOAPMessageContext context){
        
        String orgId = null;
        
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) context
                .get(MessageContext.HTTP_REQUEST_HEADERS);
        
        List<String> orgIdParams = headers.get(ORGANIZATION_ID_HEADER_PARAM);
        
        if(orgIdParams!=null){
            orgId = orgIdParams.get(0);
        }
        
        return orgId;
    }
    
//    private SAMLAssertion getSamlAssertion(SOAPMessageContext context){
//
//        SAMLAssertion samlAssertion = (SAMLAssertion) context
//                .get(MessageConstants.INCOMING_SAML_ASSERTION);
//
//        logDebugSamlAssertion(samlAssertion);
//
//        return samlAssertion;
//    }

    protected ConfigurationService getConfigService(Context context) throws NamingException {
        return (ConfigurationService) context
                    .lookup(ConfigurationService.class.getName());
    }
    
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
