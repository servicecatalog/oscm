/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * PlatformUser represents a registered user of the SaaS platform. A User is
 * always assigned to a dedicated organization. The user is identified with it's
 * UserId string, which has to be unique throughout the whole system.
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "PlatformUser.findByBusinessKey", query = "select obj from PlatformUser obj, "
                + "Organization o, Tenant t where obj.dataContainer.userId=:userId AND obj.organization = o AND o"
                + ".tenant = t AND t.dataContainer.tenantId = :tenantId "),
        @NamedQuery(name = "PlatformUser.findByUserId", query = "select obj from PlatformUser obj, "
                + "Organization o where obj.dataContainer.userId=:userId AND obj.organization = o AND o"
                + ".tenant is null "),
        @NamedQuery(name = "PlatformUser.findByUserIdAndTenantKey", query = "select obj from PlatformUser obj, "
                + "Organization o, Tenant t where obj.dataContainer.userId=:userId AND obj.organization = o AND o"
                + ".tenant = t AND t.key = :tenantKey "),
        @NamedQuery(name = "PlatformUser.findByUserIdAndOrgId", query = "select obj from PlatformUser obj, "
                + "Organization o where obj.dataContainer.userId=:userId AND obj.organization = o AND o.dataContainer.organizationId = :organizationId "),
        @NamedQuery(name = "PlatformUser.getOverdueOrganizationAdmins", query = "select obj from PlatformUser obj where obj.dataContainer.status = :status and obj.dataContainer.creationDate < :date"),
        @NamedQuery(name = "PlatformUser.getVisibleForOrganization", query = "SELECT DISTINCT pu FROM PlatformUser pu LEFT JOIN FETCH pu.assignedRoles LEFT JOIN FETCH pu.master WHERE pu.organization = :organization AND NOT EXISTS (SELECT ref FROM OnBehalfUserReference ref WHERE ref.slaveUser = pu)"),
        @NamedQuery(name = "PlatformUser.countRegisteredUsers", query = "select count(obj) from PlatformUser obj "),
        @NamedQuery(name = "PlatformUser.listByEmail", query = "select obj from PlatformUser obj where lower(obj.dataContainer.email) = lower(:email)"),
        @NamedQuery(name = "PlatformUser.findByIdPattern", query = "SELECT DISTINCT obj FROM PlatformUser obj where obj.dataContainer.userId like :userId order by obj.dataContainer.userId"),
        @NamedQuery(name = "PlatformUser.findByOrgAndReamUserId", query = "select obj from PlatformUser obj where obj.organization = :organization and obj.dataContainer.realmUserId=:realmUserId"),
        @NamedQuery(name = "PlatformUser.findUnassignedByOrg", query = "select obj from PlatformUser obj left join obj.licenses lic where ((obj.licenses is empty) or lic.subscription.key != :subscriptionKey) and obj.organization.key=:organizationKey") })
@BusinessKey(attributes = { "userId", "tenantId" })
public class PlatformUser extends DomainObjectWithHistory<PlatformUserData> {
    private static final long serialVersionUID = 150016765620902326L;

    public PlatformUser() {
        super();
        dataContainer = new PlatformUserData();
    }

    @Transient
    private String tenantId;

    /**
     * n:1 relation to the organization.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationKey")
    private Organization organization;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<TriggerProcess> triggerProcesses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private Set<RoleAssignment> assignedRoles = new HashSet<>();

    @OneToMany(mappedBy = "masterUser", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<OnBehalfUserReference> onBehalfUserReferences = new ArrayList<>();

    @OneToOne(optional = true, mappedBy = "slaveUser", fetch = FetchType.LAZY)
    private OnBehalfUserReference master;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "platformuser", fetch = FetchType.LAZY)
    private List<UserGroupToUser> userGroupToUsers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<OperationRecord> operationRecord = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Collection<UsageLicense> licenses;

    /**
     * Fill creation date (if not already set)
     */
    @PrePersist
    public void fillCreationDate() {
        if (getCreationDate() == null) {
            setCreationDate(GregorianCalendar.getInstance().getTime());
        }
    }

    /**
     * Refer to {@link PlatformUserData#additionalName}
     */
    public String getAdditionalName() {
        return dataContainer.getAdditionalName();
    }

    /**
     * Refer to {@link PlatformUserData#address}
     */
    public String getAddress() {
        return dataContainer.getAddress();
    }

    /**
     * Refer to {@link PlatformUserData#creationDate}
     */
    public Date getCreationDate() {
        return dataContainer.getCreationDate();
    }

    /**
     * Refer to {@link PlatformUserData#email}
     */
    public String getEmail() {
        return dataContainer.getEmail();
    }

    /**
     * Refer to {@link PlatformUserData#firstName}
     */
    public String getFirstName() {
        return dataContainer.getFirstName();
    }

    /**
     * Refer to {@link PlatformUserData#lastName}
     */
    public String getLastName() {
        return dataContainer.getLastName();
    }

    /**
     * Refer to {@link PlatformUserData#userId}
     */
    public String getUserId() {
        return dataContainer.getUserId();
    }

    /**
     * Refer to {@link PlatformUserData#phone}
     */
    public String getPhone() {
        return dataContainer.getPhone();
    }

    /**
     * Refer to {@link PlatformUserData#status}
     */
    public UserAccountStatus getStatus() {
        return dataContainer.getStatus();
    }

    /**
     * Refer to {@link PlatformUserData#additionalName}
     */
    public void setAdditionalName(String additionalName) {
        dataContainer.setAdditionalName(additionalName);
    }

    /**
     * Refer to {@link PlatformUserData#address}
     */
    public void setAddress(String address) {
        dataContainer.setAddress(address);
    }

    /**
     * Refer to {@link PlatformUserData#creationDate}
     */
    public void setCreationDate(Date creationDate) {
        dataContainer.setCreationDate(creationDate);
    }

    /**
     * Refer to {@link PlatformUserData#email}
     */
    public void setEmail(String email) {
        dataContainer.setEmail(email);
    }

    /**
     * Refer to {@link PlatformUserData#firstName}
     */
    public void setFirstName(String firstName) {
        dataContainer.setFirstName(firstName);
    }

    /**
     * Refer to {@link PlatformUserData#lastName}
     */
    public void setLastName(String lastName) {
        dataContainer.setLastName(lastName);
    }

    /**
     * Refer to {@link PlatformUserData#userId}
     */
    public void setUserId(String login) {
        dataContainer.setUserId(login);
    }

    /**
     * Refer to {@link PlatformUserData#phone}
     */
    public void setPhone(String phone) {
        dataContainer.setPhone(phone);
    }

    /**
     * Refer to {@link PlatformUserData#status}
     */
    public void setStatus(UserAccountStatus status) {
        dataContainer.setStatus(status);
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }

    public List<OperationRecord> getOperationRecord() {
        return operationRecord;
    }

    public void setOperationRecord(List<OperationRecord> operationRecord) {
        this.operationRecord = operationRecord;
    }

    public List<TriggerProcess> getTriggerProcesses() {
        return triggerProcesses;
    }

    public void setTriggerProcesses(List<TriggerProcess> triggerProcesses) {
        this.triggerProcesses = triggerProcesses;
    }

    public List<OnBehalfUserReference> getOnBehalfUserReference() {
        return onBehalfUserReferences;
    }

    public void setOnBehalfUserReference(
            List<OnBehalfUserReference> onBehalfUserReferences) {
        this.onBehalfUserReferences = onBehalfUserReferences;
    }

    /**
     * Refer to {@link PlatformUserData#organizationAdmin}
     */
    public boolean isOrganizationAdmin() {
        return hasRole(UserRoleType.ORGANIZATION_ADMIN);
    }

    /**
     * Check if user has an unit administrator role
     */
    public boolean isUnitAdmin() {
        return hasRole(UserRoleType.UNIT_ADMINISTRATOR);
    }

    /**
     * Refer to {@link PlatformUserData#failedLoginCounter}
     */
    public int getFailedLoginCounter() {
        return dataContainer.getFailedLoginCounter();
    }

    /**
     * Refer to {@link PlatformUserData#failedLoginCounter}
     */
    public void setFailedLoginCounter(int failedLoginCounter) {
        dataContainer.setFailedLoginCounter(failedLoginCounter);
    }

    /**
     * Refer to {@link PlatformUserData#locale}
     */
    public String getLocale() {
        return dataContainer.getLocale();
    }

    /**
     * Refer to {@link PlatformUserData#locale}
     */
    public void setLocale(String locale) {
        dataContainer.setLocale(locale);
    }

    /**
     * Refer to {@link PlatformUserData#salutation}
     */
    public Salutation getSalutation() {
        return dataContainer.getSalutation();
    }

    /**
     * Refer to {@link PlatformUserData#salutation}
     */
    public void setSalutation(Salutation salutation) {
        dataContainer.setSalutation(salutation);
    }

    /**
     * Refer to {@link PlatformUserData#passwordSalt}
     */
    public long getPasswordSalt() {
        return dataContainer.getPasswordSalt();
    }

    /**
     * Refer to {@link PlatformUserData#passwordSalt}
     */
    public void setPasswordSalt(long passwordSalt) {
        dataContainer.setPasswordSalt(passwordSalt);
    }

    /**
     * Refer to {@link PlatformUserData#passwordHash}
     */
    public byte[] getPasswordHash() {
        return dataContainer.getPasswordHash();
    }

    /**
     * Refer to {@link PlatformUserData#passwordHash}
     */
    public void setPasswordHash(byte[] passwordHash) {
        dataContainer.setPasswordHash(passwordHash);
    }

    /**
     * Refer to {@link PlatformUserData#realmUserId}
     */
    public String getRealmUserId() {
        return dataContainer.getRealmUserId();
    }

    /**
     * Refer to {@link PlatformUserData#realmUserId}
     */
    public void setRealmUserId(String realmUserId) {
        dataContainer.setRealmUserId(realmUserId);
    }

    /**
     * Refer to {@link PlatformUserData#passwordRecoveryStartDate}
     */
    public void setPasswordRecoveryStartDate(long passwordRecoveryStartDate) {
        dataContainer.setPasswordRecoveryStartDate(passwordRecoveryStartDate);
    }

    /**
     * Refer to {@link PlatformUserData#passwordRecoveryStartDate}
     */
    public long getPasswordRecoveryStartDate() {
        return dataContainer.getPasswordRecoveryStartDate();
    }

    public Set<RoleAssignment> getAssignedRoles() {
        return assignedRoles;
    }

    /**
     * @return a {@link Set} of {@link UserRoleType} assigned to the user
     */
    public Set<UserRoleType> getAssignedRoleTypes() {
        Set<UserRoleType> result = new HashSet<>();
        Set<RoleAssignment> roles = getAssignedRoles();
        for (RoleAssignment ra : roles) {
            result.add(ra.getRole().getRoleName());
        }
        return result;
    }

    public void setAssignedRoles(Set<RoleAssignment> grantedRoles) {
        this.assignedRoles = grantedRoles;
    }

    /**
     * Returns the role assignment object for the given role name.
     * 
     * @param roleToCheckFor
     *            The enum with all possible role names.
     * @return RoleAssignment
     */
    public RoleAssignment getAssignedRole(UserRoleType roleToCheckFor) {
        for (RoleAssignment assignedRole : assignedRoles) {
            if (assignedRole.getRole().getRoleName() == roleToCheckFor) {
                return assignedRole;
            }
        }
        return null;
    }

    /**
     * Checks if the user has been granted the given role.
     * 
     * @param roleToCheckFor
     *            The role the user is supposed to be granted.
     * @return <code>true</code> in case the user has been granted the given
     *         role, <code>false</code> otherwise.
     */
    public boolean hasRole(UserRoleType roleToCheckFor) {
        return getAssignedRole(roleToCheckFor) != null;
    }

    /**
     * Checks if the user has at least one of the manager roles:
     * 
     * service manager, technology manager, marketplace owner, broker manager,
     * reseller manager, platform operator, subscription manager.
     * 
     * @return <code>true</code> in case the user has been granted with manager
     *         role, <code>false</code> otherwise.
     */
    public boolean hasManagerRole() {
        for (RoleAssignment roleAssignment : assignedRoles) {
            if (roleAssignment.getRole().getRoleName().isManagerRole())
                return true;
        }
        return false;
    }

    /**
     * Checks if the user has at least one of the subscription owner roles:
     * organization administrator or subscription manager.
     * 
     * @return <code>true</code> in case the user has been granted with
     *         subscription owner role, <code>false</code> otherwise.
     */
    public boolean hasSubscriptionOwnerRole() {
        for (RoleAssignment roleAssignment : assignedRoles) {
            if ((roleAssignment.getRole()
                    .getRoleName() == UserRoleType.ORGANIZATION_ADMIN)
                    || (roleAssignment.getRole()
                            .getRoleName() == UserRoleType.SUBSCRIPTION_MANAGER)
                    || (roleAssignment.getRole()
                            .getRoleName() == UserRoleType.UNIT_ADMINISTRATOR))
                return true;
        }
        return false;
    }

    /**
     * Returns true if this user has been created to work on behalf of an
     * existing user.
     * 
     * @return boolean
     */
    public boolean isOnBehalfUser() {
        return master != null;
    }

    /**
     * Returns the user that acts on behalf of this user or null.
     */
    public OnBehalfUserReference getMaster() {
        return master;
    }

    /**
     * Sets the user that acts on behalf of this user
     */
    public void setMaster(OnBehalfUserReference master) {
        this.master = master;
    }

    public List<UserGroupToUser> getUserGroupToUsers() {
        return userGroupToUsers;
    }

    public void setUserGroupToUsers(List<UserGroupToUser> userGroupToUsers) {
        this.userGroupToUsers = userGroupToUsers;
    }

    /**
     * @return the licenses
     */
    public Collection<UsageLicense> getLicenses() {
        return licenses;
    }

    /**
     * @param licenses
     *            the licenses to set
     */
    public void setLicenses(Collection<UsageLicense> licenses) {
        this.licenses = licenses;
    }

    /**
     * 
     * @returns true if the user has SUBSCRIPTION_MANAGER role
     */
    public boolean isSubscriptionManager() {
        return hasRole(UserRoleType.SUBSCRIPTION_MANAGER);
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
