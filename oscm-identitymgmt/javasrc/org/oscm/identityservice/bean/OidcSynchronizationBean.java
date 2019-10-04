package org.oscm.identityservice.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Tenant;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.IdentityConfiguration;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.AccessType;
import org.oscm.identityservice.model.AccessGroupModel;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.identityservice.rest.AccessGroup;
import org.oscm.identityservice.rest.AccessToken;
import org.oscm.identityservice.rest.Userinfo;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

@Stateless
public class OidcSynchronizationBean {

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OidcSynchronizationBean.class);

    public List<VOUserDetails> getAllUsersFromOIDCForGroup(
            Organization organization, String tenantId, String token) {
        return getUsersInGroup(organization.getGroupId(), tenantId, token);
    }

    protected List<VOUserDetails> getUsersInGroup(String organizationId,
            String tenantId, String token) {
        return Userinfo.getAllUserDetailsForGroup(organizationId,
                tenantId, token);
    }

    public List<Organization> synchronizeGroups(String tenantId, String token) {
        List<Organization> organizations = new ArrayList<Organization>();
        List<AccessGroupModel> AccessGroupModels = null;
        try {
            AccessGroupModels = getAllOrganizations(tenantId, token);
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR,
                    "An error occured while get the organizations from the OIDC Provider");
        }
        for (int i = 0; AccessGroupModels.size() > i; i++) {
            synchronizeOIDCGroupsWithOrganizations(organizations,
                    AccessGroupModels, i);
        }
        return organizations;
    }

    protected List<AccessGroupModel> getAllOrganizations(String tenantId,
            String token) throws Exception {
        return AccessGroup
                .getAllOrganizationsFromOIDCProvider(tenantId, token);
    }

    protected void synchronizeOIDCGroupsWithOrganizations(
            List<Organization> organizations,
            List<AccessGroupModel> AccessGroupModels, int i) {
        try {
            Organization organization = getOrganizationByGroupId(
                    AccessGroupModels.get(i).getId());
            if (organization == null) {
                organization = getOrganizationByName(
                        AccessGroupModels.get(i).getName());
                if (organization != null) {
                    setOrganizationGroupId(organization,
                            AccessGroupModels.get(i).getId());
                    organizations.add(organization);
                }
            } else {
                organizations.add(organization);
            }
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_ALREADY_EXIST,
                    "GroupId or organization name are not unique");
        }
    }

    private void setOrganizationGroupId(Organization organization,
            String groupId) {
        organization.setGroupId(groupId);
        dm.merge(organization);
        dm.flush();
    }

    private Organization getOrganizationByGroupId(String groupId)
            throws Exception {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByGroupId");
        query.setParameter("groupId", groupId);
        return getOrganizationFromDB(query);
    }

    private Organization getOrganizationByName(String name) throws Exception {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByName");
        query.setParameter("name", name);
        return getOrganizationFromDB(query);
    }

    private Organization getOrganizationFromDB(Query query) throws Exception {
        List<Organization> organization = ParameterizedTypes
                .list(query.getResultList(), Organization.class);
        if (organization.size() > 1) {
            throw new Exception(
                    "More than one Organization for the given groupId");
        } else if (organization.size() > 0) {
            return organization.get(0);
        } else {
            return null;
        }
    }

    public Marketplace getFirstMarktplaceIdFromOrganization(
            Organization organization) {
        List<Marketplace> marketplaces = organization.getMarketplaces();
        if (marketplaces.size() > 0) {
            return marketplaces.get(0);
        }
        return null;
    }

    public List<String> getAllTenantIdsForSynchronization() {
        List<String> tenantIds = new ArrayList<String>();
        tenantIds.add("default"); // it´s necessary to add the default user,
                                  // because he is not in the DB but needed.
        try {
            List<Tenant> tenants = getAllTenantsFromDb();
            if (tenants != null) {
                for (int i = 0; tenants.size() > i; i++) {
                    tenantIds.add(tenants.get(i).getTenantId());
                }
            }
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_TENANT_NOT_FOUND,
                    "Can´t get tenants from DB");
        }
        return tenantIds;
    }

    private List<Tenant> getAllTenantsFromDb() throws Exception {
        Query query = dm.createNamedQuery("Tenant.getAll");
        return ParameterizedTypes.list(query.getResultList(), Tenant.class);
    }
    
    public UserImportModel getUsersToSynchronizeFromOidcProvider(String tenantId, String token,
            Organization organization, VOUserDetails usersInGroup, boolean isUserExist) {
        UserImportModel userImport = null;
        VOUserDetails user = null;
        try {
            user = Userinfo.getUserinfoFromIdentityService(
                    usersInGroup.getUserId(), tenantId, token);
            if (!isUserExist) {
                userImport = new UserImportModel();
                user.setOrganizationId(organization.getOrganizationId());
                setUserRole(organization, user);
                Marketplace mp = getFirstMarktplaceIdFromOrganization(organization);
                userImport.setMarketplace(mp);
                userImport.setOrganization(organization);
            }
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ADD_CUSTOMER,
                    "An error occured while trying to import the User ");
        }
        return userImport;
    }
    
    private void setUserRole(Organization organization, VOUserDetails user) {
        Set<UserRoleType> roles = new HashSet<UserRoleType>();
        for (OrganizationToRole orgToRole : organization.getGrantedRoles()) {
            UserRoleType roleType = orgToRole.getOrganizationRole()
                    .getRoleName().correspondingUserRole();
            if (roleType != null) {
                roles.add(roleType);
            }
        }
        user.setUserRoles(roles);
    }

}
