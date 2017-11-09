/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * @author stavreva
 * 
 */
@ManagedBean
@ViewScoped
public class BaseBillingAdapterModel {

    private String selectedBillingAdapter;

    public String getSelectedBillingAdapter() {
        return selectedBillingAdapter;
    }

    public void setSelectedBillingAdapter(String selectedBillingAdapter) {
        this.selectedBillingAdapter = selectedBillingAdapter;
    }
}
