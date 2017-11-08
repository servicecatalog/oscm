/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 13.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class Udas {

    public static VOUdaDefinition createVOUdaDefinition(String targetType,
            String udaId, String defaultValue,
            UdaConfigurationType configurationType) {
        VOUdaDefinition def = new VOUdaDefinition();
        def.setDefaultValue(defaultValue);
        def.setTargetType(targetType);
        def.setUdaId(udaId);
        def.setConfigurationType(configurationType);
        return def;
    }

    public static VOUda createVOUda(VOUdaDefinition def, String value,
            long targetObjectKey) {
        VOUda uda = new VOUda();
        uda.setTargetObjectKey(targetObjectKey);
        uda.setUdaDefinition(def);
        uda.setUdaValue(value);
        return uda;
    }

    public static UdaDefinition createUdaDefinition(DataService mgr,
            Organization owner, UdaTargetType type, String id,
            String defaultValue, UdaConfigurationType configurationType)
            throws NonUniqueBusinessKeyException {
        assertTrue(
                "Organization doesn't have the roles to create the uda definition",
                orgHasUdaRoles(owner, type));
        UdaDefinition def = new UdaDefinition();
        def.setOrganization(owner);
        def.setDefaultValue(defaultValue);
        def.setTargetType(type);
        def.setUdaId(id);
        def.setConfigurationType(configurationType);
        mgr.persist(def);
        return def;
    }

    public static Uda createUda(DataService mgr,
            DomainObject<? extends DomainDataContainer> target,
            UdaDefinition parent, String value)
            throws NonUniqueBusinessKeyException {
        // check if target entity exists
        try {
            mgr.getReference(target.getClass(), target.getKey());
        } catch (ObjectNotFoundException e) {
            fail("target object doesn't exist");
        }
        Uda uda = new Uda();
        uda.setTargetObjectKey(target);
        uda.setUdaDefinition(parent);
        uda.setUdaValue(value);
        mgr.persist(uda);
        return uda;
    }

    private static boolean orgHasUdaRoles(Organization organization,
            UdaTargetType type) {
        Set<OrganizationRoleType> roles = type.getRoles();
        for (OrganizationRoleType role : roles) {
            if (organization.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

}
