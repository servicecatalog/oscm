package org.oscm.identityservice.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.identityservice.model.AccessGroupModel;
import org.oscm.identityservice.rest.AccessToken;
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
            Organization organization) {
        List<VOUserDetails> usersInGroup = null;
        if (organization.getKey() == 18000) { // organizations.get(i).getTenant().getTenantId()
                                              // != null;
            // organizations.get(i).getTenant().getTenantId() ;
            usersInGroup = userListMock(); // userinfo.getAllUserDetailsForGroup(organizations.get(i).getGroupId(),
                                           // tenantId,
                                           // token);
        }
        return usersInGroup;
    }

    public List<Organization> synchronizeGroups(String tenantId, String token) {
        List<Organization> organizations = new ArrayList<Organization>();
        List<AccessGroupModel> AccessGroupModels = AccessGroupModelsMock();// AccessGroup.getAllOrganizationsFromOIDCProvider(tenantId,
                                                                           // token);
        for (int i = 0; AccessGroupModels.size() > i; i++) {
            synchronizeOIDCGroupsWithOrganizations(organizations,
                    AccessGroupModels, i);
        }
        return organizations;
    }

    private void synchronizeOIDCGroupsWithOrganizations(
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

    private List<Tenant> getAllTenantsFromDb() throws Exception {
        Query query = dm.createNamedQuery("Tenant.getAll");
        return ParameterizedTypes.list(query.getResultList(), Tenant.class);
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

    private List<VOUserDetails> userListMock() {
        VOUserDetails user = new VOUserDetails();
        List<VOUserDetails> users = new ArrayList();
        user.setKey(17000);
        user.setEMail("christian.worf@est.fujitsu.com");
        user.setUserId("customer@ctmg.onmicrosoft.com");
        user.setRealmUserId("customer@ctmg.onmicrosoft.com");
        users.add(user);
        return users;
    }

    private VOUserDetails userMock() {
        VOUserDetails user = new VOUserDetails();
        user.setKey(17000);
        user.setEMail("customer@ctmg.onmicrosoft.com");
        user.setUserId("customer@ctmg.onmicrosoft.com");
        user.setRealmUserId("customer@ctmg.onmicrosoft.com");
        user.setLocale("en");
        return user;
    }

    private List<AccessGroupModel> AccessGroupModelsMock() {
        List<AccessGroupModel> accessGroupModels = new ArrayList<AccessGroupModel>();
        AccessGroupModel accessGroupModel = new AccessGroupModel();
        accessGroupModel.setId("3b190ce1-b7fa-410a-bb0a-c00f2353d0d8");
        accessGroupModel.setName("customerorg");
        accessGroupModels.add(accessGroupModel);
        return accessGroupModels;
    }

    public String getFirstMarktplaceIdFromOrganization(
            Organization organization) {
        List<Marketplace> marketplaces = organization.getMarketplaces();
        if (marketplaces.size() > 0) {
            return marketplaces.get(0).getMarketplaceId();
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

}
