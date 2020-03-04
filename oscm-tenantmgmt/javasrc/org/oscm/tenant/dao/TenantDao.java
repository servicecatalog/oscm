/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 30.08.2016
 *
 * <p>*****************************************************************************
 */
package org.oscm.tenant.dao;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.ObjectNotFoundException;

@Stateless
@LocalBean
public class TenantDao {

  @EJB DataService dataManager;

  public List<Tenant> getAllTenants() {
    Query query = dataManager.createNamedQuery("Tenant.getAll");
    return ParameterizedTypes.list(query.getResultList(), Tenant.class);
  }

  public Tenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException {
    Tenant tenant = new Tenant();
    tenant.setTenantId(tenantId);
    return (Tenant) dataManager.getReferenceByBusinessKey(tenant);
  }

  public List<Tenant> getTenantsByIdPattern(String tenantIdPattern) {
    Query query = dataManager.createNamedQuery("Tenant.getTenantsByIdPattern");
    query.setParameter("tenantIdPattern", tenantIdPattern);
    return ParameterizedTypes.list(query.getResultList(), Tenant.class);
  }

  public Tenant find(long tenantID) {
    return dataManager.find(Tenant.class, tenantID);
  }

  public long doesOrganizationForTenantExist(Tenant tenant) {
    Query query = dataManager.createNamedQuery("Tenant.checkOrganization");
    query.setParameter("tenant", tenant);
    return (long) query.getSingleResult();
  }

  public long doesMarketplaceAssignedToTenantExist(Tenant tenant) {
    Query query = dataManager.createNamedQuery("Tenant.checkMarketplace");
    query.setParameter("tenant", tenant);
    return (long) query.getSingleResult();
  }

  public List<String> getNonUniqueOrgUserIdsInTenant(String orgId, long tenantKey) {

    final String queryString =
        "SELECT groupedusers.userid "
            + "FROM "
            + "(SELECT users.userid, count(users.orgkey) as numberOfUsers FROM"
            + "(SELECT u.userid, o.tkey as orgkey FROM platformuser u "
            + "LEFT JOIN organization o ON u.organizationkey=o.tkey WHERE o.organizationid=:orgId "
            + "UNION  "
            + "SELECT u.userid, o.tkey as orgkey FROM platformuser u "
            + "LEFT JOIN organization o ON u.organizationkey=o.tkey WHERE (CASE :tenantKey WHEN 0 THEN o.tenant_tkey IS NULL ELSE o.tenant_tkey=:tenantKey END)) "
            + "as users "
            + "GROUP BY users.userid) "
            + "as groupedusers "
            + "WHERE groupedusers.numberOfUsers>1";

    Query query = dataManager.createNativeQuery(queryString);
    query.setParameter("orgId", orgId);
    query.setParameter("tenantKey", tenantKey);

    return ParameterizedTypes.list(query.getResultList(), String.class);
  }

   public List<Organization> getOrgNameInTenant(String orgName, String tenantId){
     Query query = dataManager.createNamedQuery("Organization.findOrganizationsByName");
     query.setParameter("name", orgName);
     query.setParameter("tenantId", !"default".equals(tenantId) ? tenantId : null);
     return ParameterizedTypes.list(query.getResultList(), Organization.class);
   }
}
