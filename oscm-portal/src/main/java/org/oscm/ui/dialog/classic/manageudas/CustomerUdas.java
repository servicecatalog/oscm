/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import java.util.List;

import org.oscm.ui.model.UdaRow;

/**
 * A UI model row object for customer UDAs.
 * 
 * @author yuyin
 * 
 */
public class CustomerUdas {

    /**
     * List of organization based UdaRow from a customer perspective
     */
    private List<UdaRow> organizationUdaRows;

    /**
     * List of subscription based UdaRow from a customer perspective
     */
    private List<UdaRow> subscriptionUdaRows;

    /**
     * @return the subscriptionUdaRows
     */

    public List<UdaRow> getSubscriptionUdaRows() {
        return subscriptionUdaRows;
    }

    /**
     * @param subscriptionUdaRows
     *            the subscriptionUdaRow to set
     */
    public void setSubscriptionUdaRows(List<UdaRow> subscriptionUdaRows) {
        this.subscriptionUdaRows = subscriptionUdaRows;
    }

    /**
     * @return the organizationUdaRows
     */
    public List<UdaRow> getOrganizationUdaRows() {
        return organizationUdaRows;
    }

    /**
     * @param organizationUdaRows
     *            the organizationUdaRow to set
     */
    public void setOrganizationUdaRows(List<UdaRow> organizationUdaRows) {
        this.organizationUdaRows = organizationUdaRows;
    }

}
