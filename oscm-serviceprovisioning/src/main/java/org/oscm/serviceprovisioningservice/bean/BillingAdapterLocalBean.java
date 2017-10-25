/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 07.09.15 19:55
 *
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.dataservice.local.DataService;

/**
 * Created by ChojnackiD on 2015-09-07.
 */
@Stateless
@LocalBean
public class BillingAdapterLocalBean {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    public String getDefaultBillingIdentifier() {
        Query namedQuery = dm.createNamedQuery("BillingAdapter.getDefaultAdapterIdentifier");
        return (String) namedQuery.getSingleResult();
    }
}
