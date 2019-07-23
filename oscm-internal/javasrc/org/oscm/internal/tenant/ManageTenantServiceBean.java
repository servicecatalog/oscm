/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
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
    public POTenant getTenantByTenantId(String tenantId)
            throws ObjectNotFoundException {
        return new POTenant(tenantService.getTenantByTenantId(tenantId));
    }

    @Override
    public String addTenant(POTenant poTenant)
            throws NonUniqueBusinessKeyException {
        String suggestedId = IdGenerator.generateArtificialIdentifier();
        poTenant.setTenantId(suggestedId);
        tenantService.addTenant(poTenant.toVOTenanat());
        return suggestedId;
    }

    @Override
    public void updateTenant(POTenant poTenant)
            throws ConcurrentModificationException, ObjectNotFoundException,
            NonUniqueBusinessKeyException {
        tenantService.updateTenant(poTenant.toVOTenanat());
    }

    @Override
    public void removeTenant(POTenant poTenant)
            throws ObjectNotFoundException, TenantDeletionConstraintException {
        tenantService.removeTenant(poTenant.toVOTenanat());
    }

    @Override
    public List<POTenant> getTenantsByIdPattern(String tenantIdPattern) {
        List<POTenant> poTenants = new ArrayList<>();
        for (VOTenant voTenant : tenantService
                .getTenantsByIdPattern(tenantIdPattern)) {
            poTenants.add(new POTenant(voTenant));
        }
        return poTenants;
    }

    @Override
    public void validateOrgUsersUniqnessInTenant(String orgId, long tenantKey)
            throws ValidationException {
        boolean duplicatedUserIdExists = tenantService
                .doOrgUsersExistInTenant(orgId, tenantKey);

        if (duplicatedUserIdExists) {
            throw new ValidationException(ReasonEnum.USER_ID_DUPLICATED, null,
                    null);
        }
    }

}
