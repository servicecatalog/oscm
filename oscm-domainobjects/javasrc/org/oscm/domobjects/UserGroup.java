/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-6-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * This object represents a user group that is created via organization.
 * 
 * @author Fang
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name",
        "organization_tkey" }))
@NamedQueries({
        @NamedQuery(name = "UserGroup.findByBusinessKey", query = "SELECT obj FROM UserGroup obj WHERE obj.dataContainer.name = :name AND obj.organization_tkey = :organization_tkey ORDER BY obj.dataContainer.name ASC"),
        @NamedQuery(name = "UserGroup.findByOrganizationKeyWithoutDefault", query = "SELECT obj FROM UserGroup obj WHERE obj.organization_tkey = :organization_tkey AND obj.dataContainer.isDefault = FALSE ORDER BY obj.dataContainer.name ASC"),
        @NamedQuery(name = "UserGroup.findByOrganizationKey", query = "SELECT obj FROM UserGroup obj WHERE obj.organization_tkey = :organization_tkey ORDER BY obj.dataContainer.name ASC"),
        @NamedQuery(name = "UserGroup.countUser", query = "SELECT COUNT(g2u.platformuser_tkey) FROM UserGroupToUser g2u WHERE g2u.usergroup_tkey = :usergroup_tkey"),
        @NamedQuery(name = "UserGroup.countUserForDefaultGroup", query = "SELECT COUNT(p) FROM PlatformUser p WHERE p.organization.dataContainer.organizationId = :organizationId"),
        @NamedQuery(name = "UserGroup.findAssignedUserIds", query = "SELECT pu.dataContainer.userId FROM UserGroupToUser g2u, PlatformUser pu WHERE g2u.usergroup_tkey = :usergroup_tkey AND pu = g2u.platformuser"),
        @NamedQuery(name = "UserGroup.countUserGroup", query = "SELECT COUNT(g2u1.userGroup) FROM UserGroupToUser g2u1 WHERE g2u1.platformuser_tkey = :user_tkey"),
        @NamedQuery(name = "UserGroup.findByUserWithoutDefault", query = "SELECT obj FROM UserGroup obj, UserGroupToUser g2u, PlatformUser pu  WHERE pu.key=:userKey AND pu = g2u.platformuser AND g2u.userGroup = obj AND obj.dataContainer.isDefault = FALSE  ORDER BY obj.dataContainer.name ASC"),
        @NamedQuery(name = "UserGroup.findByUserId", query = "SELECT obj FROM UserGroup obj, UserGroupToUser g2u, PlatformUser pu  WHERE pu.dataContainer.userId=:userId AND pu = g2u.platformuser AND g2u.userGroup = obj ORDER BY obj.dataContainer.name ASC"),
        @NamedQuery(name = "UserGroup.findInvisibleProductKeys", query = "SELECT u2p.product_tkey FROM UserGroupToInvisibleProduct u2p WHERE u2p.userGroup IN (SELECT g2u1.userGroup FROM UserGroupToUser g2u1 WHERE g2u1.platformuser_tkey = :user_tkey AND g2u1.userGroup.dataContainer.isDefault = FALSE) AND u2p.userGroup.dataContainer.isDefault = FALSE GROUP BY u2p.product_tkey HAVING COUNT(u2p.product_tkey) = (SELECT COUNT(g2u) FROM  UserGroupToUser g2u WHERE g2u.platformuser_tkey = :user_tkey AND g2u.userGroup.dataContainer.isDefault = FALSE)"),
        @NamedQuery(name = "UserGroup.findInvisibleProductKeysForGroup", query = "SELECT u2p.product_tkey FROM UserGroupToInvisibleProduct u2p WHERE u2p.usergroup_tkey = :usergroup_tkey"),
        @NamedQuery(name = "UserGroup.findByUserWithRole", query = "SELECT ug FROM UserGroup ug, UserGroupToUser ugtu, UnitRoleAssignment ura WHERE ug=ugtu.userGroup AND ugtu = ura.userGroupToUser AND ugtu.platformuser_tkey = :platformuser_tkey AND ura.unituserrole_tkey = :unituserrole_tkey"),
        @NamedQuery(name = "UserGroup.findByUserWithRoleWithoutDefault", query = "SELECT ug FROM UserGroup ug, UserGroupToUser ugtu, UnitRoleAssignment ura WHERE ug=ugtu.userGroup AND ugtu = ura.userGroupToUser AND ugtu.platformuser_tkey = :platformuser_tkey AND ura.unituserrole_tkey = :unituserrole_tkey AND ug.dataContainer.isDefault = FALSE"),
        @NamedQuery(name = "UserGroup.getInvisibleProducts", query = "SELECT u2p FROM UserGroupToInvisibleProduct u2p WHERE u2p.usergroup_tkey = :usergroup_tkey"),
        @NamedQuery(name = "UserGroup.findVisibleServices", query =
                "SELECT p FROM Product p, CatalogEntry ce, UserGroup ug " +
                "WHERE ce.marketplace.key=:marketplaceKey " +
                "AND ce.product.key = p.key " +
                "AND p.dataContainer.status IN (org.oscm.internal.types.enumtypes.ServiceStatus.SUSPENDED, org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE) " +
                "AND EXISTS (SELECT 1 FROM UserGroupToInvisibleProduct u2i WHERE u2i.usergroup_tkey=:userGroupKey AND u2i.product_tkey=p.key AND u2i.forallusers='false') " +
                "AND ug.key=:userGroupKey"),
        @NamedQuery(name = "UserGroup.findAccessibleServices", query =
                "SELECT p FROM Product p, CatalogEntry ce, UserGroup ug " +
                "WHERE ce.marketplace.key=:marketplaceKey " +
                "AND ce.product.key = p.key " +
                "AND p.dataContainer.status IN (org.oscm.internal.types.enumtypes.ServiceStatus.SUSPENDED, org.oscm.internal.types.enumtypes.ServiceStatus.ACTIVE) " +
                "AND NOT EXISTS (SELECT 1 FROM UserGroupToInvisibleProduct u2i WHERE u2i.usergroup_tkey=:userGroupKey AND u2i.product_tkey=p.key )" +
                "AND ug.key=:userGroupKey")
})
@BusinessKey(attributes = { "name", "organization_tkey" })
public class UserGroup extends DomainObjectWithHistory<UserGroupData> {

    private static final long serialVersionUID = -5389889904871241801L;

    @Column(name = "organization_tkey", nullable = false, insertable = false, updatable = false)
    private long organization_tkey;

    public UserGroup() {
        super();
        dataContainer = new UserGroupData();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_tkey")
    private Organization organization;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "userGroup", fetch = FetchType.LAZY)
    private List<UserGroupToUser> userGroupToUsers = new ArrayList<UserGroupToUser>();

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "userGroup", fetch = FetchType.LAZY)
    private List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<UserGroupToInvisibleProduct>();

    @OneToMany(mappedBy = "userGroup", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getName() {
        return dataContainer.getName();
    }

    public void setName(String name) {
        this.dataContainer.setName(name);
    }

    public String getDescription() {
        return dataContainer.getDescription();
    }

    public void setDescription(String description) {
        this.dataContainer.setDescription(description);
    }

    public boolean isDefault() {
        return dataContainer.isDefault();
    }

    public void setIsDefault(boolean isDefault) {
        this.dataContainer.setIsDefault(isDefault);
    }

    public List<UserGroupToUser> getUserGroupToUsers() {
        return userGroupToUsers;
    }

    public void setUserGroupToUsers(List<UserGroupToUser> userGroupToUsers) {
        this.userGroupToUsers = userGroupToUsers;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            setOrganization_tkey(organization.getKey());
        }
    }

    public long getOrganization_tkey() {
        return organization_tkey;
    }

    public void setOrganization_tkey(long organization_tkey) {
        this.organization_tkey = organization_tkey;
    }

    public List<UserGroupToInvisibleProduct> getUserGroupToInvisibleProducts() {
        return userGroupToInvisibleProducts;
    }

    public void setUserGroupToInvisibleProducts(
            List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts) {
        this.userGroupToInvisibleProducts = userGroupToInvisibleProducts;
    }

    public List<Product> getInvisibleProducts() {
        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = getUserGroupToInvisibleProducts();
        List<Product> products = new ArrayList<Product>();
        for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : userGroupToInvisibleProducts) {
            products.add(userGroupToInvisibleProduct.getProduct());
        }
        return products;
    }

    public List<PlatformUser> getUsers() {
        List<UserGroupToUser> userGroupToUsers = getUserGroupToUsers();
        List<PlatformUser> users = new ArrayList<PlatformUser>();
        for (UserGroupToUser userGroupToUser : userGroupToUsers) {
            users.add(userGroupToUser.getPlatformuser());
        }
        return users;
    }

    public String getReferenceId() {
        return dataContainer.getReferenceId();
    }

    public void setReferenceId(String referenceId) {
        dataContainer.setReferenceId(referenceId);
    }

}
