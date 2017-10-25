/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.techproductoperation.dao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.OperationRecord;

/**
 * @author zhaoh.fnst
 * 
 */
@Stateless
@LocalBean
public class OperationRecordDao {
    @EJB(beanInterface = DataService.class)
    DataService dm;

    public List<OperationRecord> getOperationsForOrgAdmin(long orgKey) {
        Query query = dm.createNamedQuery("OperationRecord.findByOrgKey");
        query.setParameter("organization_tkey", Long.valueOf(orgKey));
        List<OperationRecord> operationRecords = ParameterizedTypes.list(
                query.getResultList(), OperationRecord.class);
        return operationRecords;
    }

    public List<OperationRecord> getOperationsForSubManager(long userKey) {
        Query query = dm.createNamedQuery("OperationRecord.findBySubOwnerKey");
        query.setParameter("subscriptionOwner_tkey", Long.valueOf(userKey));
        List<OperationRecord> operationRecords = ParameterizedTypes.list(
                query.getResultList(), OperationRecord.class);
        return operationRecords;
    }

    public List<OperationRecord> getOperationsForUser(long userKey) {
        Query query = dm.createNamedQuery("OperationRecord.findByUserKey");
        query.setParameter("platformUser_tkey", Long.valueOf(userKey));
        List<OperationRecord> operationRecords = ParameterizedTypes.list(
                query.getResultList(), OperationRecord.class);
        return operationRecords;
    }

    public void removeOperationsForSubscription(long subscriptionKey) {
        Query query = dm
                .createNamedQuery("OperationRecord.removeBySubscriptionKey");
        query.setParameter("subscription_tkey", Long.valueOf(subscriptionKey));
        query.executeUpdate();
    }
}
