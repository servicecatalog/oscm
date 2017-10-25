/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014年11月12日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author gaowenxin
 * 
 */
@Stateless
@LocalBean
public class PaymentTypeDao {
    @EJB(beanInterface = DataService.class)
    private DataService dm;

    private static final String QUERY_PAYMENT_TYPE_FOR_CUSTOMER = "SELECT ortpt FROM OrganizationRefToPaymentType ortpt WHERE "
            + "ortpt.organizationReference IN "
            + "(SELECT o FROM OrganizationReference o where o.source.dataContainer.organizationId = :definitionId) "
            + "AND "
            + "ortpt.organizationRole.dataContainer.roleName = :roleName";

    public PaymentTypeDao() {
    }

    public List<OrganizationRefToPaymentType> retrievePaymentTypeForCustomer(
            Organization supplier) {
        List<OrganizationRefToPaymentType> paymentTypes = new ArrayList<OrganizationRefToPaymentType>();
        Query query = dm.createQuery(QUERY_PAYMENT_TYPE_FOR_CUSTOMER);
        query.setParameter("definitionId", supplier.getOrganizationId());
        query.setParameter("roleName", OrganizationRoleType.CUSTOMER);
        paymentTypes = ParameterizedTypes.list(query.getResultList(),
                OrganizationRefToPaymentType.class);
        return paymentTypes;
    }

}
