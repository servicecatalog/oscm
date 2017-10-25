/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: June 4, 2014                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;
import java.util.Set;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ModifiedEntity;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.ModifiedEntityType;
import org.oscm.domobjects.enums.ModifiedEntityType.TargetEntity;
import org.oscm.interceptor.ExceptionMapper;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class ModifiedEntityDao {

    public ModifiedEntityDao(DataService ds) {
        this.dataManager = ds;
    }

    private DataService dataManager;

    public void deleteModifiedEntityForSubscription(Subscription subscription) {
        // delete temporary information of subscription in modifiedentity
        Set<ModifiedEntityType> SubTypes = ModifiedEntityType
                .getModifiedEntityTypes(TargetEntity.SUBSCRIPTION);
        Query query = dataManager
                .createNamedQuery("ModifiedEntity.deleteByObjectAndTypes");
        query.setParameter("targetObjectKey",
                Long.valueOf(subscription.getKey()));
        query.setParameter("targetObjectTypes", SubTypes);
        query.executeUpdate();

        // delete uda values in temporary table
        Query queryUda = dataManager
                .createNamedQuery("ModifiedUda.deleteBySubscription");
        queryUda.setParameter("subscriptionKey",
                Long.valueOf(subscription.getKey()));
        queryUda.executeUpdate();
    }

    public List<ModifiedEntity> retrieveModifiedEntities(
            Subscription subscription) {
        Set<ModifiedEntityType> modifiedEntityTypes = ModifiedEntityType
                .getModifiedEntityTypes(TargetEntity.SUBSCRIPTION);
        Query query = dataManager
                .createNamedQuery("ModifiedEntity.findByObjectAndTypes");
        query.setParameter("targetObjectKey",
                Long.valueOf(subscription.getKey()));
        query.setParameter("targetObjectTypes", modifiedEntityTypes);
        List<ModifiedEntity> modifiedEntities = ParameterizedTypes.list(
                query.getResultList(), ModifiedEntity.class);
        return modifiedEntities;
    }

    public Long countSubscriptionOfOrganizationAndSubscription(
            Subscription subscriptionToModify, String subscriptionId) {
        Query query = dataManager
                .createNamedQuery("ModifiedEntity.countSubscriptionWithOrgIdAndSubId");
        query.setParameter("organizationId", subscriptionToModify
                .getOrganization().getOrganizationId());
        query.setParameter("subOrgIdType",
                ModifiedEntityType.SUBSCRIPTION_ORGANIZATIONID);
        query.setParameter("subscriptionId", subscriptionId);
        query.setParameter("subIdType",
                ModifiedEntityType.SUBSCRIPTION_SUBSCRIPTIONID);
        return (Long) query.getSingleResult();
    }
}
