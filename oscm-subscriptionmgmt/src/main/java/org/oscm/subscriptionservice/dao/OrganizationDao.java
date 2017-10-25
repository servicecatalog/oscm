/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.service.POPartner;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class OrganizationDao {

    private DataService dataManager;

    public OrganizationDao(DataService ds) {
        this.dataManager = ds;
    }

    public List<Organization> getCustomersForSubscriptionId(
            Organization offerer, String subscriptionId,
            Set<SubscriptionStatus> states) {
        Query query = dataManager
                .createNamedQuery("Organization.getForOffererKeyAndSubscriptionId");
        query.setParameter("offererKey", Long.valueOf(offerer.getKey()));
        query.setParameter("subscriptionId", subscriptionId);
        query.setParameter("states", states);
        return ParameterizedTypes.list(query.getResultList(),
                Organization.class);

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PlatformUser> getOrganizationAdmins(long organizationKey) {
        Query query = dataManager
                .createNamedQuery("Organization.getAdministrators");
        query.setParameter("orgkey", Long.valueOf(organizationKey));
        return ParameterizedTypes.list(query.getResultList(),
                PlatformUser.class);
    }

    private List<Object[]> getOrganizationsFromDB(OrganizationRoleType orgRole,
                                                 long serviceKey) {
        final String s = "SELECT\n"
                + " o.tkey,\n"
                + " o.organizationid,\n"
                + " o.name,\n"
                + " CASE WHEN p.tkey IS NULL THEN NULL\n"
                + "      ELSE (\n"
                + "          SELECT\n"
                + "              rsm.revenueShare\n"
                + "          FROM\n"
                + "              catalogentry ce,\n"
                + "              revenuesharemodel rsm\n" //
                + "          WHERE\n"
                + "              ce.product_tKey = p.tkey AND\n"
                + "              (ce.brokerpricemodel_tKey = rsm.tkey AND :orgRole = 'BROKER' OR\n"
                + "               ce.resellerpricemodel_tKey = rsm.tkey AND :orgRole = 'RESELLER'))\n"
                + " END AS revenueshare,\n" //
                + " p.productid\n"
                + "FROM\n"
                + " organization o LEFT JOIN product p on\n"
                + "     p.vendorkey = o.tkey AND\n"
                + "     p.template_tkey = :serviceKey AND\n"
                + "     p.status in ('ACTIVE', 'INACTIVE', 'SUSPENDED') AND\n"
                + "     p.type = 'PARTNER_TEMPLATE',\n"
                + " organizationtorole o2r,\n"
                + " organizationrole r\n"
                + "WHERE\n" //
                + " o2r.organization_tkey = o.tkey AND\n"
                + " o2r.organizationrole_tkey = r.tkey AND\n"
                + " r.rolename = :orgRole";

        final Query query = dataManager.createNativeQuery(s);
        query.setParameter("serviceKey", Long.valueOf(serviceKey));
        query.setParameter("orgRole", orgRole.name());
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<POPartner> getOrganizations(OrganizationRoleType orgRole,
                                            long serviceKey) {
        final List<Object[]> organizations = getOrganizationsFromDB(orgRole,
                serviceKey);
        final List<POPartner> result = new ArrayList<>();
        for (Object[] o : organizations) {
            PORevenueShare r = null;
            if (o[3] != null) {
                r = new PORevenueShare();
                r.setRevenueShare(new BigDecimal("" + o[3]));
            }
            result.add(new POPartner(((Number) o[0]).longValue(),
                    (String) o[1], (String) o[2], r, o[4] != null));
        }
        return result;
    }
}
