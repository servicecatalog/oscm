/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2016-04-19                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.interceptor.ExceptionMapper;

@Interceptors({ ExceptionMapper.class })
public class BillingContactDao {

    private DataService dataManager;

    public BillingContactDao(DataService ds) {
        this.dataManager = ds;
    }

    public List<BillingContact> getBillingContactsForOrganization(long organizationKey, String email,
            String address) {
        Query query = dataManager
                .createNamedQuery("BillingContact.findByOrgAndAddress");
        query.setParameter("organization_tkey", organizationKey);
        query.setParameter("email", email);
        query.setParameter("address", address);
        return ParameterizedTypes.list(query.getResultList(),
                BillingContact.class);

    }

}
