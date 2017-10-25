/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-23
 *
 *******************************************************************************/
package org.oscm.marketplace.dao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.MarketplaceAccess;
import org.oscm.domobjects.Organization;

/**
 * Created by BadziakP on 2016-05-23.
 */
@Stateless
@LocalBean
public class MarketplaceAccessDao {

    @EJB(beanInterface = DataService.class)
    DataService dataService;

    public List<MarketplaceAccess> getForMarketplaceKey(long marketplaceKey) {
        Query query = dataService
                .createNamedQuery("MarketplaceAccess.findByMarketplace");
        query.setParameter("marketplace_tkey", marketplaceKey);
        return ParameterizedTypes.list(query.getResultList(),
                MarketplaceAccess.class);
    }

    public void removeAccessForMarketplace(long marketplaceKey) {
        Query query = dataService
                .createNamedQuery("MarketplaceAccess.removeAllForMarketplace");
        query.setParameter("marketplace_tkey", marketplaceKey);
        query.executeUpdate();
    }

    public List<Object[]> getOrganizationsWithMplAndSubscriptions(
            long marketplaceKey, long tenantKey) {

        String querySelect;
        
        String tenantPartForQuery;

        if (tenantKey == 0) {
            tenantPartForQuery = "FROM organization o, marketplace mkp WHERE o.tenant_tkey is null";
        } else {
            tenantPartForQuery = "FROM organization o, marketplace mkp WHERE o.tenant_tkey=" + String.valueOf(tenantKey) +" and mkp.tkey=:marketplaceKey";
        }
            
        querySelect = "SELECT DISTINCT (o.tkey) as orgKey, "
                + "o.organizationid as orgId, " + "o.name as name, "
                + "(SELECT true FROM marketplaceaccess ma where ma.organization_tkey=o.tkey and ma.marketplace_tkey=:marketplaceKey) as hasAccess, "
                + "(SELECT count(s.tkey) FROM subscription s WHERE s.organizationkey=o.tkey AND s.marketplace_tkey=:marketplaceKey AND s.status<>'DEACTIVATED') as subscriptions, "
                + "(SELECT count(p.tkey) FROM product p INNER JOIN catalogentry c ON c.product_tkey=p.tkey WHERE c.marketplace_tkey=:marketplaceKey AND p.vendorkey=o.tkey AND p.status='ACTIVE') as publishedServices "
                + tenantPartForQuery;

        Query query = dataService.createNativeQuery(querySelect);

        query.setParameter("marketplaceKey", Long.valueOf(marketplaceKey));

        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    public List<Organization> getAllOrganizationsWithAccessToMarketplace(
            long marketplaceKey, long tenantKey) {

        String tenantPartForQuery;

        if (tenantKey == 0) {
            tenantPartForQuery = "AND o.tenant_tkey IS NULL ";
        } else {
            tenantPartForQuery = "AND mp.tenant_tkey = o.tenant_tkey ";
        }

        String queryString = "SELECT DISTINCT (o.*) "
        + "FROM organization o, marketplaceaccess ma, marketplace mp "
        + "WHERE ma.marketplace_tkey=:marketplaceKey "
        + "AND o.tkey = ma.organization_tkey "
        + tenantPartForQuery
        + "ORDER BY o.tkey";

        Query query = dataService.createNativeQuery(queryString,
                Organization.class);
        query.setParameter("marketplaceKey", new Long(marketplaceKey));

        return query.getResultList();
    }

}
