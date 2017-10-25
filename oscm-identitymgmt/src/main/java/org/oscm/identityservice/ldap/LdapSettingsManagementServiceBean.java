/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.PlatformSetting;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * Bean implementation of the user management related functionality.
 * 
 * @author groch
 * 
 */

@Local
@Stateless
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class LdapSettingsManagementServiceBean implements
        LdapSettingsManagementServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LdapSettingsManagementServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @Resource
    SessionContext sessionCtx;

    public void setPlatformSettings(Properties organizationProperties)
            throws ValidationException {
        

        // first remove all existing settings
        Query query = ds.createNamedQuery("PlatformSetting.removeAll");
        query.executeUpdate();

        if (organizationProperties != null) {
            for (Object e : organizationProperties.keySet()) {
                String key = (String) e;
                try {
                    if (!SettingType.contains(key)) {
                        logger.logWarn(
                                Log4jLogger.SYSTEM_LOG,
                                LogMessageIdentifier.WARN_IGNORE_ILLEGAL_PLATFORM_SETTING,
                                key);
                    } else {
                        SettingType settingType = SettingType.valueOf(key);
                        String value = organizationProperties.getProperty(key);
                        if ("".equals(value)) {
                            ValidationException vf = new ValidationException(
                                    ReasonEnum.LDAP_INVALID_PLATFORM_PROPERTY,
                                    null, new Object[] { key });
                            logger.logError(
                                    Log4jLogger.SYSTEM_LOG,
                                    vf,
                                    LogMessageIdentifier.ERROR_INVALID_LDAP_PLATFORM_PROPERTY,
                                    key);
                            sessionCtx.setRollbackOnly();
                            throw vf;
                        }

                        PlatformSetting setting = new PlatformSetting();
                        setting.setSettingType(settingType);
                        setting.setSettingValue(value);
                        ds.persist(setting);
                    }
                } catch (NonUniqueBusinessKeyException ex) {
                    // must not happen because passed properties have unique
                    // keys anyway
                    SaaSSystemException sse = new SaaSSystemException(ex);
                    logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                            LogMessageIdentifier.ERROR_DUPLICATE_LDAP_PROPERTY);
                    throw sse;
                }

            }
        }
        
    }

    public Properties getPlatformSettings() {
        
        Properties result = new Properties();
        Query query = ds.createNamedQuery("PlatformSetting.getAll");
        List<PlatformSetting> list = ParameterizedTypes.list(
                query.getResultList(), PlatformSetting.class);
        for (PlatformSetting setting : list) {
            result.put(String.valueOf(setting.getSettingType()),
                    setting.getSettingValue());
        }
        
        return result;
    }

    public void setOrganizationSettings(String orgId,
            Properties organizationProperties) throws ObjectNotFoundException {
        

        Organization organization = getOrganization(orgId);
        // first remove all existing settings
        Query query = ds
                .createNamedQuery("OrganizationSetting.removeAllForOrganization");
        query.setParameter("organization", organization);
        query.executeUpdate();

        if (organizationProperties != null) {
            List<OrganizationSetting> settings = new ArrayList<OrganizationSetting>();
            for (Object e : organizationProperties.keySet()) {
                String key = (String) e;
                if (!SettingType.contains(key)) {
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IGNORE_ILLEGAL_ORGANIZATION_SETTING,
                            key);
                } else {
                    OrganizationSetting setting = createOrganizationSetting(
                            organization, key,
                            organizationProperties.getProperty(key));
                    settings.add(setting);
                }
            }
            // now assign the settings to the organization
            // (may be empty if no properties provided)
            organization.setOrganizationSettings(settings);
        }
        
    }

    public Properties getOrganizationSettings(String orgId)
            throws ObjectNotFoundException {
        

        Organization organization = getOrganization(orgId);

        Properties result = new Properties();
        for (OrganizationSetting setting : organization
                .getOrganizationSettings()) {
            result.put(String.valueOf(setting.getSettingType()),
                    setting.getSettingValue());
        }

        
        return result;
    }

    public void resetOrganizationSettings(String orgId)
            throws ObjectNotFoundException {
        

        Organization organization = getOrganization(orgId);
        // first remove all existing settings
        Query query = ds
                .createNamedQuery("OrganizationSetting.removeAllForOrganization");
        query.setParameter("organization", organization);
        query.executeUpdate();

        Properties platformProps = getPlatformSettings();

        if (platformProps != null) {
            List<OrganizationSetting> settings = new ArrayList<OrganizationSetting>();
            for (Object e : platformProps.keySet()) {
                String key = (String) e;
                OrganizationSetting setting = createOrganizationSetting(
                        organization, key, "");
                settings.add(setting);
            }
            // now assign the settings to the organization
            // (may be empty if no properties provided)
            organization.setOrganizationSettings(settings);
        }

        
    }

    public Properties getOrganizationSettingsResolved(String orgId)
            throws ObjectNotFoundException {
        

        Properties orgProps = getOrganizationSettings(orgId);
        Properties resolvedProps = resolveSettings(orgProps);

        
        return resolvedProps;
    }

    public Properties getSettingsResolved(Properties organizationProperties) {
        

        ArgumentValidator.notNull("organizationProperties",
                organizationProperties);

        Properties settings = new Properties();
        if (organizationProperties != null) {
            for (Object e : organizationProperties.keySet()) {
                String key = (String) e;
                if (!SettingType.contains(key)) {
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_IGNORE_ILLEGAL_PLATFORM_SETTING,
                            key);
                } else {
                    settings.put(key, organizationProperties.getProperty(key));
                }
            }
        }

        Properties resolvedProps = resolveSettings(settings);

        
        return resolvedProps;
    }

    private Properties resolveSettings(Properties orgProps) {
        Properties platformProps = getPlatformSettings();
        Properties resolvedProps = new Properties();

        if (orgProps != null) {
            for (Object propKey : orgProps.keySet()) {
                final Object orgValue = orgProps.get(propKey);
                if ("".equals(orgValue)) {
                    final Object platFormValue = platformProps.get(propKey);
                    if (platFormValue != null) {
                        resolvedProps.put(propKey, platFormValue);
                    }
                } else {
                    resolvedProps.put(propKey, orgValue);
                }
            }
        }
        return resolvedProps;
    }

    public String getDefaultValueForSetting(SettingType setting) {
        

        ArgumentValidator.notNull("setting", setting);
        // ensureOrganizationAccessPriviliges(organization, true);

        PlatformSetting pfSetting = new PlatformSetting();
        pfSetting.setSettingType(setting);
        try {
            pfSetting = (PlatformSetting) ds
                    .getReferenceByBusinessKey(pfSetting);
        } catch (ObjectNotFoundException ex) {
            if (setting.getDefaultValue() == null) {
                IllegalArgumentException iae = new IllegalArgumentException();
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        iae,
                        LogMessageIdentifier.ERROR_NO_DEFAULT_DEFINED_FOR_LDAP_PROPERTY,
                        setting.name());
                throw iae;
            } else {
                return setting.getDefaultValue();
            }
        }

        // return "" to bind to platform property (at this point we know it
        // exists)

        
        return "";

    }

    public Set<SettingType> getMappedAttributes() {
        

        Set<SettingType> result = new HashSet<SettingType>();
        Properties resolvedProps;
        try {
            if (ds.getCurrentUser().getOrganization()
                    .equals(getPlatformOperatorReference())) {
                resolvedProps = getPlatformSettings();
            } else {
                resolvedProps = getOrganizationSettingsResolved(ds
                        .getCurrentUser().getOrganization().getOrganizationId());
            }
        } catch (ObjectNotFoundException e) {
            // must not happen because the currently logged-in user should have
            // a valid organization
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNKNOWN_ORGANIZATION, ds
                            .getCurrentUser().getOrganization()
                            .getOrganizationId());
            throw sse;
        }

        if (resolvedProps != null) {
            for (Object propKey : resolvedProps.keySet()) {
                String key = (String) propKey;
                SettingType settingType = SettingType.valueOf(key);
                if (SettingType.LDAP_ATTRIBUTES.contains(settingType)) {
                    result.add(settingType);
                }
            }
        }

        
        return result;
    }

    /**
     * @param organization
     *            the organization
     * @param key
     *            the property key
     */
    private OrganizationSetting createOrganizationSetting(
            Organization organization, String key, String value) {
        try {
            SettingType settingType = SettingType.valueOf(key);

            OrganizationSetting setting = new OrganizationSetting();
            setting.setOrganization(organization);
            setting.setSettingType(settingType);
            setting.setSettingValue(value);
            ds.persist(setting);
            return setting;
        } catch (NonUniqueBusinessKeyException ex) {
            // must not happen because passed properties have unique
            // keys anyway
            SaaSSystemException sse = new SaaSSystemException(ex);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_DUPLICATE_LDAP_PROPERTY);
            throw sse;
        }
    }

    private Organization getOrganization(String orgId)
            throws ObjectNotFoundException {
        ArgumentValidator.notEmptyString("orgId", orgId);
        Organization organization = new Organization();
        organization.setOrganizationId(orgId);
        organization = (Organization) ds
                .getReferenceByBusinessKey(organization);
        return organization;
    }

    /**
     * Reads the platform operator organization and returns it. In case it
     * cannot be found, a system exception will be logged and thrown.
     * 
     * @return The platform operator organization.
     */
    private Organization getPlatformOperatorReference() {

        Organization platformOperator = new Organization();
        platformOperator
                .setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                        .name());
        try {
            platformOperator = (Organization) ds
                    .getReferenceByBusinessKey(platformOperator);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "Platform operator organization could not be found!", e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_PLATFORM_OPERATOR_ORGANIZATION_NOT_FOUND);
            throw sse;
        }
        return platformOperator;
    }

    public Set<Organization> getLdapManagedOrganizations() {
        
        Query query = ds
                .createNamedQuery("Organization.getLdapManagedOrganizations");
        List<Organization> orgList = ParameterizedTypes.list(
                query.getResultList(), Organization.class);
        final Set<Organization> result = new HashSet<Organization>(orgList);
        
        return result;
    }

    public void clearPlatformSettings() {
        
        Query query = ds.createNamedQuery("PlatformSetting.removeAll");
        query.executeUpdate();
        
    }
}
