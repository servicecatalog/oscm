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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * A user being granted access to a subscription gets a UsageLicense for this
 * subscription. The license data contains the current status (if the license is
 * still valid), whether the user has administrative privileges in the product,
 * as well as the dates of assignment, activation and revocation of the user. In
 * order to allow charging of a subscription upon usage a license is activated
 * with it's first usage; before it is in the status ASSIGNED. Thus, if a
 * organization admin accidentally assigns a wrong user to a subscription he can
 * correct this without being charged before the wrong user really attempts to
 * access the product.
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UsageLicense.countWithRole", query = "SELECT count(lic) FROM UsageLicense lic WHERE lic.roleDefinition = :role"),
        @NamedQuery(name = "UsageLicense.getForUser", query = "SELECT lic FROM UsageLicense lic JOIN FETCH lic.subscription LEFT JOIN FETCH lic.roleDefinition "
                + "WHERE lic.user.key = :userKey AND lic.subscription.dataContainer.status IN (:status) ORDER BY lic.subscription.key ASC"),
        @NamedQuery(name = "UsageLicense.getUsersforSubscription", query = "SELECT lic FROM UsageLicense lic,Subscription sub "
                + "WHERE lic.subscription = sub and lic.subscription=:subscription AND sub.dataContainer.status =:status"),
        @NamedQuery(name = "UsageLicense.getWithUserByProductTemplate", query = "SELECT count(*) FROM UsageLicense lic LEFT JOIN lic.subscription sub "
                + "WHERE lic.user.key = :userKey AND sub.dataContainer.status IN (:status)"
                + "AND EXISTS (SELECT sub FROM Subscription sub WHERE sub.product.template = :prodTemplate AND sub.key=lic.subscription.key)") })
public class UsageLicense extends DomainObjectWithHistory<UsageLicenseData> {

    private static final long serialVersionUID = 1L;

    public UsageLicense() {
        super();
        dataContainer = new UsageLicenseData();
    }

    /**
     * n:1 relation to the subscription.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Subscription subscription;

    /**
     * n:1 relation to the platform user.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlatformUser user;

    /**
     * n:1 relation to the role definition.<br>
     * CascadeType: NONE
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private RoleDefinition roleDefinition;

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public PlatformUser getUser() {
        return user;
    }

    public void setUser(PlatformUser user) {
        this.user = user;
    }

    /**
     * Refer to {@link UsageLicenseData#assignmentDate}
     */
    public long getAssignmentDate() {
        return dataContainer.getAssignmentDate();
    }

    /**
     * Refer to {@link UsageLicenseData#assignmentDate}
     */
    public void setAssignmentDate(long assignmentDate) {
        dataContainer.setAssignmentDate(assignmentDate);
    }

    /**
     * Refer to {@link UsageLicenseData#applicationUserId}
     */
    public void setApplicationUserId(String applicationUserId) {
        dataContainer.setApplicationUserId(applicationUserId);
    }

    /**
     * Refer to {@link UsageLicenseData#applicationUserId}
     */
    public String getApplicationUserId() {
        return dataContainer.getApplicationUserId();
    }

    public void setRoleDefinition(RoleDefinition roleDefinition) {
        this.roleDefinition = roleDefinition;
    }

    public RoleDefinition getRoleDefinition() {
        return roleDefinition;
    }

}
