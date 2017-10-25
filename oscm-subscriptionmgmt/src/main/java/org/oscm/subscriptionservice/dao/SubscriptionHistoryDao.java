/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Subscription;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOInstanceInfo;

@Interceptors({ ExceptionMapper.class })
public class SubscriptionHistoryDao {

    private final DataService dataManager;

    public SubscriptionHistoryDao(DataService ds) {
        this.dataManager = ds;
    }

    public List<String> getAccessInfos(Subscription subscription,
            VOInstanceInfo instance) {
        Query query = dataManager
                .createNamedQuery("SubscriptionHistory.getAccessInfos");
        query.setParameter("subscriptionId", subscription.getSubscriptionId());
        query.setParameter("productInstanceId", instance.getInstanceId());
        query.setParameter("status", SubscriptionStatus.ACTIVE);
        List<String> accessInfo = ParameterizedTypes.list(
                query.getResultList(), String.class);
        return accessInfo;
    }

}
