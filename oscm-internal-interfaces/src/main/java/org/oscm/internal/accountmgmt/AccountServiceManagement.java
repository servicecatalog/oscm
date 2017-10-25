/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-12-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.accountmgmt;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * Service providing the functionality to manage user account.
 * 
 * @author yuyin
 * 
 */
@Remote
public interface AccountServiceManagement {

    /**
     * @return the cut-off day of current organization.
     */
    public int getCutOffDayOfOrganization();

    /**
     * set the cutOffDay to current organization.
     * <p>
     * 
     * @param cutOffDay
     *            a <code>cutOffDay</code> specifying the day of starting
     *            billing
     * @param organization
     *            an <code>organization</code> specifying the value object of
     *            current user in BackingBean
     */
    public void setCutOffDayOfOrganization(int cutOffDay,
            VOOrganization organization) throws ConcurrentModificationException;

    /**
     * Retrieves the data for the organization of the calling user.
     * <p>
     * Required role: service manager or reseller manager
     * 
     * @return the organization data
     */
    public VOOrganization getOrganizationData();

}
