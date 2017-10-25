/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Bean implementation of the user management related functionality.
 * 
 * @author groch
 * 
 */

@Remote(UserManagementService.class)
@Stateless
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class UserManagementServiceBean implements UserManagementService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserManagementServiceBean.class);

    private static final String MASKED_CREDENTIALS = "********";

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB(beanInterface = LdapSettingsManagementServiceLocal.class)
    LdapSettingsManagementServiceLocal ldapSettingsMgmt;

    @EJB(beanInterface = LdapAccessServiceLocal.class)
    LdapAccessServiceLocal ldapAccess;

    @RolesAllowed("PLATFORM_OPERATOR")
    public void setPlatformSettings(Properties organizationProperties)
            throws ValidationException {
        
        ldapSettingsMgmt.setPlatformSettings(organizationProperties);
        
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "SERVICE_MANAGER", "BROKER_MANAGER",
            "RESELLER_MANAGER", "PLATFORM_OPERATOR" })
    public Set<POLdapSetting> getPlatformSettings() {
        
        Properties props = ldapSettingsMgmt.getPlatformSettings();
        Properties onlyKeyProps = new Properties();
        for (Object key : props.keySet()) {
            onlyKeyProps.put(key, "");
        }
        Set<POLdapSetting> result = mergeSettings(onlyKeyProps, props);
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void setOrganizationSettings(String orgId,
            Properties organizationProperties) throws ObjectNotFoundException {
        
        ldapSettingsMgmt.setOrganizationSettings(orgId, organizationProperties);
        
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public void setOrganizationSettings(Properties organizationProperties) {
        
        String orgId = getOrganizationId();
        try {
            ldapSettingsMgmt.setOrganizationSettings(orgId,
                    organizationProperties);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, orgId);
            throw sse;
        }
        
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public Properties getOrganizationSettings(String orgId)
            throws ObjectNotFoundException {
        
        Properties result = ldapSettingsMgmt.getOrganizationSettings(orgId);
        if (result.containsKey(SettingType.LDAP_CREDENTIALS.name())) {
            result.setProperty(SettingType.LDAP_CREDENTIALS.name(),
                    MASKED_CREDENTIALS);
        }
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void resetOrganizationSettings(String orgId)
            throws ObjectNotFoundException {
        
        ldapSettingsMgmt.resetOrganizationSettings(orgId);
        
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public void resetOrganizationSettings() {
        
        String orgId = getOrganizationId();
        try {
            ldapSettingsMgmt.resetOrganizationSettings(orgId);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, orgId);
            throw sse;
        }
        
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public Set<POLdapSetting> getOrganizationSettingsResolved(String orgId)
            throws ObjectNotFoundException {
        
        Properties orgProps = ldapSettingsMgmt.getOrganizationSettings(orgId);
        Properties platformProps = ldapSettingsMgmt.getPlatformSettings();
        Set<POLdapSetting> result = mergeSettings(orgProps, platformProps);
        
        return result;
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public Set<POLdapSetting> getOrganizationSettingsResolved() {
        
        String orgId = getOrganizationId();
        Properties orgProps;
        try {
            orgProps = ldapSettingsMgmt.getOrganizationSettings(orgId);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, orgId);
            throw sse;
        }
        Properties platformProps = ldapSettingsMgmt.getPlatformSettings();
        Set<POLdapSetting> result = mergeSettings(orgProps, platformProps);
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public boolean canConnect(String orgId) throws ObjectNotFoundException,
            ValidationException {
        
        Properties props = (orgId == null || orgId.length() == 0) ? ldapSettingsMgmt
                .getPlatformSettings() : ldapSettingsMgmt
                .getOrganizationSettingsResolved(orgId);
        boolean result = canConnect(props);
        
        return result;
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public boolean canConnect() throws ValidationException {
        
        String orgId = getOrganizationId();
        Properties props;
        try {
            props = ldapSettingsMgmt.getOrganizationSettingsResolved(orgId);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, orgId);
            throw sse;
        }
        boolean result = canConnect(props);
        
        return result;
    }

    /**
     * Verifies that a connection to an LDAP system can be established using the
     * provided connection properties.
     * 
     * @param props
     *            The properties to verify.
     * @return <code>true</code> in case a connection can be established,
     *         <code>false</code> otherwise.
     * @throws ValidationException
     *             thrown in case the properties are empty.
     */
    boolean canConnect(Properties props) throws ValidationException {
        // extract different types of properties + check if all mandatory
        // settings are available
        LdapConnector connector = createLdapConnector(props);
        connector.ensureAllMandatoryLdapPropertiesPresent();
        boolean result = connector.canConnect();
        return result;
    }

    LdapConnector createLdapConnector(Properties props) {
        return new LdapConnector(ldapAccess, props);
    }

    public Set<SettingType> getMappedAttributes() {
        
        Set<SettingType> result = ldapSettingsMgmt.getMappedAttributes();
        
        return result;
    }

    public boolean isPlatformOperator() {
        return ds.getCurrentUser().hasRole(UserRoleType.PLATFORM_OPERATOR);
    }

    String getOrganizationId() {
        return ds.getCurrentUser().getOrganization().getOrganizationId();
    }

    Set<POLdapSetting> mergeSettings(Properties organizationSettings,
            Properties platformSettings) {
        
        HashSet<POLdapSetting> result = new HashSet<POLdapSetting>();
        Set<Object> keySet = organizationSettings.keySet();
        for (Object key : keySet) {
            String keyString = (String) key;
            String orgValue = organizationSettings.getProperty(keyString);
            String platformValue = platformSettings.getProperty(keyString);
            POLdapSetting settingToAdd = null;
            if ("".equals(orgValue)) {
                settingToAdd = new POLdapSetting(keyString, platformValue, true);
            } else {
                settingToAdd = new POLdapSetting(keyString, orgValue, false);
            }
            if (SettingType.LDAP_CREDENTIALS.name().equals(keyString)) {
                settingToAdd.setSettingValue(MASKED_CREDENTIALS);
            }
            result.add(settingToAdd);
        }
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public Set<POLdapOrganization> getLdapManagedOrganizations() {
        
        HashSet<POLdapOrganization> result = new HashSet<POLdapOrganization>();
        Set<Organization> ldapOrgs = ldapSettingsMgmt
                .getLdapManagedOrganizations();
        for (Organization organization : ldapOrgs) {
            result.add(new POLdapOrganization(organization.getKey(),
                    organization.getVersion(), organization.getName(),
                    organization.getOrganizationId()));
        }
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public boolean isOrganizationLDAPManaged(String organizationIdentifier)
            throws ObjectNotFoundException {
        
        Organization template = new Organization();
        template.setOrganizationId(organizationIdentifier);
        Organization resultOrg = (Organization) ds
                .getReferenceByBusinessKey(template);
        
        return resultOrg.isRemoteLdapActive();
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public boolean isOrganizationLDAPManaged() {
        
        String orgId = getOrganizationId();
        boolean result;
        try {
            result = isOrganizationLDAPManaged(orgId);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, orgId);
            throw sse;
        }
        
        return result;
    }

    @RolesAllowed("PLATFORM_OPERATOR")
    public void clearPlatformSettings() {
        
        ldapSettingsMgmt.clearPlatformSettings();
        
    }

}
