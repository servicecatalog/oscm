/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 10.10.2019                                                      
 *                                                                              
 *******************************************************************************/

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
import org.oscm.identity.mapper.UserMapper;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.UserInfo;
import org.oscm.identityservice.model.UserImportModel;
import org.oscm.identityservice.rest.Userinfo;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

@Stateless
public class OidcSynchronizationBean {
    
    private static String DEFAULT_TENANT = "default"; 

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OidcSynchronizationBean.class);

    public List<VOUserDetails> getAllUsersFromOIDCForGroup(
            Organization organization, String tenantId) {
        ApiIdentityClient client = createClient(tenantId);
        List<VOUserDetails> userInfo = null;
        try {
            Set<UserInfo> info = client
                    .getGroupMembers(organization.getGroupId());
            userInfo = (List<VOUserDetails>) UserMapper.fromSet(info);
        } catch (IdentityClientException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR,
                    "An error occured while get the group members from the OIDC Provider");
        }
        return userInfo;
    }

    public List<Organization> synchronizeGroups(String tenantId) {
        List<Organization> organizations = new ArrayList<Organization>();
        List<GroupInfo> accessGroupModels = null;
        try {
            accessGroupModels = getAllOrganizations(tenantId);
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR,
                    "An error occured while getting the groups from the OIDC Provider");
        }
        if (accessGroupModels != null) {
            for (int i = 0; accessGroupModels.size() > i; i++) {
                Organization organization = synchronizeOIDCGroupsWithOrganizations(
                        accessGroupModels.get(i));
                if (organization != null) {
                    organizations.add(organization);
                }
            }
        }
        return organizations;
    }

    protected List<GroupInfo> getAllOrganizations(String tenantId)
            throws Exception {
        ApiIdentityClient client = createClient(tenantId);
        ArrayList<GroupInfo> groupInfo = new ArrayList<GroupInfo>();
        Set<GroupInfo> info = null;
        try {
            info = client.getGroups();
            info.size();
        } catch (IdentityClientException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR,
                    "An error occured while getting the groups from the OIDC Provider");
        }
        groupInfo.addAll(info);
        return groupInfo;
    }

    protected Organization synchronizeOIDCGroupsWithOrganizations(
            GroupInfo accessGroupModel) {
        try {
            Organization organization = getOrganizationByGroupId(
                    accessGroupModel.getId());
            if (organization == null) {
                organization = getOrganizationByName(
                        accessGroupModel.getName());
                if (organization != null) {
                    setOrganizationGroupId(organization,
                            accessGroupModel.getId());
                    return organization;
                }
            } else {
                return organization;
            }
        } catch (Exception e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_ALREADY_EXIST,
                    "GroupId or organization name are not unique");
        }
        return null;
    }

    private void setOrganizationGroupId(Organization organization,
            String groupId) {
        organization.setGroupId(groupId);
        dm.merge(organization);
        dm.flush();
    }

    protected Organization getOrganizationByGroupId(String groupId)
            throws Exception {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByGroupId");
        query.setParameter("groupId", groupId);
        return getOrganizationFromDB(query);
    }

    protected Organization getOrganizationByName(String name) throws Exception {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByName");
        query.setParameter("name", name);
        return getOrganizationFromDB(query);
    }

    protected Organization getOrganizationFromDB(Query query) throws Exception {
        List<Organization> organization = ParameterizedTypes
                .list(query.getResultList(), Organization.class);
        if (organization.size() > 1) {
            throw new Exception(
                    "More than one Organization exists for the given groupId");
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
        tenantIds.add(DEFAULT_TENANT); // it´s necessary to add the default tenant, because he is not in the DB but needed.
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

    protected List<Tenant> getAllTenantsFromDb() throws Exception {
        Query query = dm.createNamedQuery("Tenant.getAll");
        return ParameterizedTypes.list(query.getResultList(), Tenant.class);
    }

    public UserImportModel getUsersToSynchronizeFromOidcProvider(
            String tenantId, Organization organization,
            VOUserDetails userInGroup, boolean isUserExist) {
        UserImportModel userImport = null;
        if (!isUserExist) {
            userImport = new UserImportModel();
            userInGroup.setOrganizationId(organization.getOrganizationId());
            setUserRole(organization, userInGroup);
            Marketplace mp = getFirstMarktplaceIdFromOrganization(organization);
            userImport.setMarketplace(mp);
            userImport.setOrganization(organization);
            userImport.setUser(userInGroup);
        }
        return userImport;
    }

    protected VOUserDetails getUserinfoFromIdentityService(String tenantId,
            String token, VOUserDetails userInGroup) throws Exception {
        return Userinfo.getUserinfoFromIdentityService(userInGroup.getUserId(),
                tenantId, token);
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
    
    private ApiIdentityClient createClient(String tenantId) {
        IdentityConfiguration config = IdentityConfiguration.of()
                .tenantId(tenantId).sessionContext(null).build();
        ApiIdentityClient client = new ApiIdentityClient(config);
        return client;
    }


}
