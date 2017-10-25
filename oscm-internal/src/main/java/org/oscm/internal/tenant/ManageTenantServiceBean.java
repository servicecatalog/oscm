/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.internal.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.id.IdGenerator;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

@Stateless
@RolesAllowed("PLATFORM_OPERATOR")
@Remote(ManageTenantService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ManageTenantServiceBean implements ManageTenantService {
    private static final Log4jLogger logger = LoggerFactory
        .getLogger(ManageTenantServiceBean.class);

    @EJB
    TenantService tenantService;

    @Override
    public List<POTenant> getAllTenants() {
        List<POTenant> poTenants = new ArrayList<>();
        for (VOTenant voTenant : tenantService.getTenants()) {
            poTenants.add(new POTenant(voTenant));
        }
        return poTenants;
    }

    @Override
    public POTenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException {
        return new POTenant(tenantService.getTenantByTenantId(tenantId));
    }

    @Override
    public String addTenant(POTenant poTenant) throws NonUniqueBusinessKeyException {
        String suggestedId = IdGenerator.generateArtificialIdentifier();
        poTenant.setTenantId(suggestedId);
        tenantService.addTenant(poTenant.toVOTenanat());
        return suggestedId;
    }

    @Override
    public void updateTenant(POTenant poTenant)
        throws ConcurrentModificationException, ObjectNotFoundException, NonUniqueBusinessKeyException {
        tenantService.updateTenant(poTenant.toVOTenanat());
    }

    @Override
    public void removeTenant(POTenant poTenant) throws ObjectNotFoundException, TenantDeletionConstraintException {
        tenantService.removeTenant(poTenant.toVOTenanat());
    }

    @Override
    public void setTenantSettings(Properties properties, String tenantId)
        throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        VOTenant voTenant = tenantService.getTenantByTenantId(tenantId);
        List<VOTenantSetting> tenantSettings = new ArrayList<>();
        if (properties == null) {
            return;
        }
        for (Object propertyKey : properties.keySet()) {
            String key = (String) propertyKey;
            if (!IdpSettingType.contains(key)) {
                logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_IGNORE_ILLEGAL_PLATFORM_SETTING,
                    key);
                continue;
            }
            String value = properties.getProperty(key);
            VOTenantSetting voTenantSetting = new VOTenantSetting();
            voTenantSetting.setName(IdpSettingType.valueOf(key));
            voTenantSetting.setValue(value);
            voTenantSetting.setVoTenant(voTenant);
            tenantSettings.add(voTenantSetting);
        }
        tenantService.addTenantSettings(tenantSettings, voTenant);
    }

    @Override
    public Properties getTenantSettings(long tenantKey) {
        Properties properties = new Properties();
        for (VOTenantSetting voTenantSetting : tenantService.getSettingsForTenant(tenantKey)) {
            properties.put(voTenantSetting.getName().name(), voTenantSetting.getValue());
        }
        return properties;
    }

    @Override
    public void removeTenantSettings(long tenantKey) throws ObjectNotFoundException {
        tenantService.removeTenantSettings(tenantKey);
    }
    
    @Override
    public List<POTenant> getTenantsByIdPattern(String tenantIdPattern) {
        List<POTenant> poTenants = new ArrayList<>();
        for (VOTenant voTenant : tenantService.getTenantsByIdPattern(tenantIdPattern)) {
            poTenants.add(new POTenant(voTenant));
        }
        return poTenants;
    }

    @Override
    public void validateOrgUsersUniqnessInTenant(String orgId, long tenantKey) throws ValidationException {
        boolean duplicatedUserIdExists = tenantService
                .doOrgUsersExistInTenant(orgId, tenantKey);
        
        if(duplicatedUserIdExists){
            throw new ValidationException(ReasonEnum.USER_ID_DUPLICATED, null, null);
        }
    }
}
