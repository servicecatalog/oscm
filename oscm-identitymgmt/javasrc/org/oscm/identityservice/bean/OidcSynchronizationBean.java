/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 10.10.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Tenant;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.mapper.UserMapper;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.UserInfo;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.identityservice.rest.RestUtils;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * This EJB implementation is responsible for synchronizing organizations with
 * user groups that are managed with an OIDC provider.
 */
@Stateless
public class OidcSynchronizationBean {

    private static String DEFAULT_TENANT = "default";

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OidcSynchronizationBean.class);

    @SuppressWarnings("unchecked")
    public List<VOUserDetails> getAllUsersFromGroup(String groupId, String tenantId) {
        try {
            ApiIdentityClient client = RestUtils.createClient(tenantId);
            Set<UserInfo> info = client.getGroupMembers(groupId);
            return (List<VOUserDetails>) UserMapper.fromSet(info);
        } catch (IdentityClientException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_SYNCHRONIZATION, String.format(
                    "An error occurred while getting the members of the OIDC access group %s for tenant %s", groupId, tenantId));
        }
        return emptyList();
    }

    public List<Organization> synchronizeGroups(String tenantId) {
        List<Organization> orgs = new ArrayList<Organization>();
        List<GroupInfo> groups = getAllGroups(tenantId);
        for (GroupInfo g : groups) {
            List<Organization> groupOrg = syncOrganizations(g, tenantId);
            orgs.addAll(groupOrg);
        }
        return orgs;
    }

    protected List<GroupInfo> getAllGroups(String tenantId) {
        
        try {
            
            ApiIdentityClient client = RestUtils.createClient(tenantId);
           
            return new ArrayList<GroupInfo>(client.getGroups());
           
        } catch (IdentityClientException e) {
            logger.logInfo(Log4jLogger.SYSTEM_LOG, LogMessageIdentifier.ERROR_SYNCHRONIZATION,
                    String.format("Cannot get OIDC access groups for tenant %s. Check if the tenant is configured propperly. ", tenantId ));
        }
        return emptyList();
    }

    protected List<Organization> syncOrganizations(GroupInfo group, String tenantId) {
        try {

            return asList(new Organization[] {syncOrganization(group, tenantId)});

        } catch (NonUniqueResultException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_SYNCHRONIZATION,
                    String.format("Error synchronizing organization: %s (group id and organization name must be unique)", e.getMessage()));
        } catch (EntityNotFoundException e) {
            logger.logInfo(Log4jLogger.SYSTEM_LOG, LogMessageIdentifier.INFO_SYNCHRONIZATION, 
                    String.format("No organization for group %s.", group.getName()));
            
        } 
        return emptyList();
    }

    protected Organization syncOrganization(GroupInfo group, String tenantId) {
        try {
          
            return findOrganizationByGroupId(group.getId());
          
        } catch (EntityNotFoundException e) {
            // Look up in newly added groups 
            Organization org = findOrganizationByName(group.getName(), tenantId);
            setGroupId(org, group.getId());
            return org;
        }
    }

    private void setGroupId(Organization organization, String groupId) {
        organization.setGroupId(groupId);
        dm.merge(organization);
        dm.flush();
    }

    protected Organization findOrganizationByGroupId(String groupId)
            throws NonUniqueResultException {
        Query query = dm.createNamedQuery("Organization.findOrganizationsByGroupId");
        query.setParameter("groupId", groupId);
        return resultFrom(query, groupId);
    }

    protected Organization findOrganizationByName(String name, String tenantId) throws NonUniqueResultException, EntityNotFoundException {
        Query query = dm.createNamedQuery("Organization.findOrganizationsByName");
        query.setParameter("name", name);
        setTenant(tenantId, query);
        return resultFrom(query, name);
    }

    private void setTenant(String tenantId, Query query) {
        query.setParameter("tenantId", (DEFAULT_TENANT.equals(tenantId)) ? null : tenantId);
    }

    protected Organization resultFrom(Query query, String entity) throws NonUniqueResultException, EntityNotFoundException {
        List<Organization> orgs = ParameterizedTypes.list(query.getResultList(),
                Organization.class);

        if (orgs.size() == 0) {
            throw new EntityNotFoundException(String.format("%s not found", entity));
        }
        if (orgs.size() > 1) {
            throw new NonUniqueResultException(String.format("Found duplicate entries for %s", entity));
        }
        return orgs.get(0);

    }

    private void setFirstFoundMarketplace(UserImportModel m, Organization o) {
        List<Marketplace> mps = o.getMarketplaces();
        for (Marketplace mp : mps) {
            m.setMarketplace(mp);
            return;
        }
        m.setMarketplace(null);
    }

    public List<String> getAllTenantIds() {
        List<String> tenantIds = new ArrayList<String>();
        tenantIds.add(DEFAULT_TENANT); // it's necessary to add the default
                                       // tenant, because he is not in the DB
                                       // but needed.
        List<Tenant> tenants = getAllTenantsFromDb();
        for (Tenant t : tenants) {
            tenantIds.add(t.getTenantId());
        }
        return tenantIds;
    }

    protected List<Tenant> getAllTenantsFromDb() {
        Query query = dm.createNamedQuery("Tenant.getAll");
        return ParameterizedTypes.list(query.getResultList(), Tenant.class);
    }

    public UserImportModel getUserModel(String tenantId, Organization org, VOUserDetails user) {
        UserImportModel um = new UserImportModel();
        setUserWithRoles(um, org, user, tenantId);
        setFirstFoundMarketplace(um, org);
        return um;
    }

    protected void setUserWithRoles(UserImportModel um, Organization o, VOUserDetails u,
            String tenantId) {
        u.setOrganizationId(o.getOrganizationId());
        Set<UserRoleType> roles = new HashSet<UserRoleType>();
        for (OrganizationToRole orgToRole : o.getGrantedRoles()) {
            UserRoleType roleType = orgToRole.getOrganizationRole().getRoleName()
                    .correspondingUserRole();
            if (roleType != null) {
                roles.add(roleType);
            }
        }
        u.setUserRoles(roles);
        u.setTenantId(tenantId);
        um.setOrganization(o);
        um.setUser(u);
    }
}
