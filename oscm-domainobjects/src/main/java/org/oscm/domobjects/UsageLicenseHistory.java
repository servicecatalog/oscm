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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of Subscription, used for auditing. Will be automatically
 * created during persist, save or remove operations (if performed via
 * DataManager)
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UsageLicenseHistory.findByObject", query = "select c from UsageLicenseHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "UsageLicenseHistory.getForSubKey_VersionDESC", query = "SELECT hist FROM UsageLicenseHistory hist WHERE hist.subscriptionObjKey = :subscriptionKey AND hist.modDate < :endTimeAsDate AND (hist.objVersion = (SELECT max(hist2.objVersion) FROM UsageLicenseHistory hist2 WHERE hist2.objKey = hist.objKey AND hist2.modDate < :startTimeAsDate) OR hist.modDate >= :startTimeAsDate) ORDER BY hist.userObjKey ASC, hist.modDate DESC, hist.objVersion DESC") })
public class UsageLicenseHistory extends DomainHistoryObject<UsageLicenseData> {

    private static final long serialVersionUID = 1L;

    /**
     * Reference to the original subscription
     */
    private long subscriptionObjKey;

    /**
     * Reference to the original user
     */
    private long userObjKey;

    /**
     * Reference to the original role definition.
     */
    private Long roleDefinitionObjKey;

    public UsageLicenseHistory() {
        dataContainer = new UsageLicenseData();
    }

    /**
     * Constructs UsageLicenseHistory from a UsageLicense domain object
     * 
     * @param c
     *            - the UsageLicense
     */
    public UsageLicenseHistory(UsageLicense c) {
        super(c);
        if (c.getSubscription() != null) {
            setSubscriptionObjKey(c.getSubscription().getKey());
        }
        if (c.getUser() != null) {
            setUserObjKey(c.getUser().getKey());
        }
        if (c.getRoleDefinition() != null) {
            setRoleDefinitionObjKey(Long
                    .valueOf(c.getRoleDefinition().getKey()));
        }
    }

    public void setSubscriptionObjKey(long subscriptionObjid) {
        this.subscriptionObjKey = subscriptionObjid;
    }

    public long getSubscriptionObjKey() {
        return subscriptionObjKey;
    }

    public void setUserObjKey(long userObjid) {
        this.userObjKey = userObjid;
    }

    public long getUserObjKey() {
        return userObjKey;
    }

    public void setRoleDefinitionObjKey(Long roleDefinitionObjKey) {
        this.roleDefinitionObjKey = roleDefinitionObjKey;
    }

    public Long getRoleDefinitionObjKey() {
        return roleDefinitionObjKey;
    }

}
