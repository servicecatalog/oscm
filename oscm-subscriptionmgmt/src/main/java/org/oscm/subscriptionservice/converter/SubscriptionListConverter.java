/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.usermanagement.POServiceRole;
import org.oscm.internal.usermanagement.POSubscription;
import org.oscm.internal.usermanagement.POUsagelicense;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;

/**
 * Converts query result lists better 'readable', type safe format.
 * 
 * @author weiser
 * 
 */
public class SubscriptionListConverter {

    /**
     * Expects a {@link Query} result list containing arrays with the first
     * element being a {@link Subscription} and the second one a
     * {@link RoleDefinition} or <code>null</code>. Converts it to a 'better
     * readable' format.
     * 
     * @param list
     *            the list to convert
     * @return the result
     */
    public List<SubscriptionWithRoles> convert(List<Object[]> list) {
        if (list == null) {
            return new ArrayList<SubscriptionWithRoles>();
        }
        ArrayList<SubscriptionWithRoles> result = new ArrayList<SubscriptionWithRoles>();
        Subscription last = null;
        SubscriptionWithRoles swr = new SubscriptionWithRoles();
        for (Object[] a : list) {
            if (last != a[0]) {
                swr = new SubscriptionWithRoles();
                swr.setSubscription((Subscription) a[0]);
                result.add(swr);
                last = swr.getSubscription();
            }
            if (a[1] != null) {
                swr.getRoles().add((RoleDefinition) a[1]);
            }
        }
        return result;
    }

    public POSubscription toPOSubscription(Object[] subscription,
            List<RoleDefinition> roles, LocalizerFacade lf) {

        POSubscription poSubscription = new POSubscription();

        poSubscription.setId((String) subscription[0]);
        poSubscription.setAssigned((boolean) subscription[1]);

        List<POServiceRole> poRoles = new ArrayList<>();

        if (!roles.isEmpty()) {

            for (RoleDefinition roleDef : roles) {
                if (roleDef != null) {
                    POServiceRole role = toPOServiceRole(lf, roleDef);
                    poRoles.add(role);

                    String roleId = (String) subscription[2];

                    if (role.getId().equals(roleId)) {
                        POUsagelicense poUsageLicense = new POUsagelicense();
                        poUsageLicense.setKey(
                                ((BigInteger) subscription[4]).longValue());
                        poUsageLicense.setVersion((int) subscription[5]);
                        poUsageLicense.setPoServieRole(role);

                        poSubscription.setUsageLicense(poUsageLicense);
                    }
                }
            }
        }
        poSubscription.setRoles(poRoles);

        return poSubscription;
    }

    public POServiceRole toPOServiceRole(LocalizerFacade lf,
            RoleDefinition role) {
        if (role == null) {
            return null;
        }
        POServiceRole r = new POServiceRole();
        r.setId(role.getRoleId());
        r.setKey(role.getKey());
        r.setName(
                lf.getText(role.getKey(), LocalizedObjectTypes.ROLE_DEF_NAME));
        r.setVersion(role.getVersion());
        return r;
    }

}
