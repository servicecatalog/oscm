/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;
import java.util.Set;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class UsageLicenseDao {

    public UsageLicenseDao(DataService ds) {
        this.dataManager = ds;
    }

    private DataService dataManager;

    public List<UsageLicense> getSubscriptionAssignments(PlatformUser user,
            Set<SubscriptionStatus> states) {
        Query q = dataManager.createNamedQuery("UsageLicense.getForUser");
        q.setParameter("userKey", Long.valueOf(user.getKey()));
        q.setParameter("status", states);
        List<UsageLicense> result = ParameterizedTypes.list(q.getResultList(),
                UsageLicense.class);
        return result;
    }

    public List<UsageLicense> getUsersforSubscription(
            Subscription subscription) {
        Query q = dataManager
                .createNamedQuery("UsageLicense.getUsersforSubscription");
        q.setParameter("subscription", subscription);
        q.setParameter("status", SubscriptionStatus.ACTIVE);
        List<UsageLicense> result = ParameterizedTypes.list(q.getResultList(),
                UsageLicense.class);
        return result;
    }

}
