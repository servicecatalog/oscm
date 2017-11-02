/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.billingadapter.POBillingAdapter;

/**
 * @author stavreva
 * 
 */
@ManagedBean
@ViewScoped
public class BillingAdapterModel {

    private List<BillingAdapterWrapper> billingAdapters = new ArrayList<BillingAdapterWrapper>();

    private String selectedPanel;

    private static final String PANELBAR_ITEM_SUFIX = "panelAdapterItem";
    
    private static final String PANELBAR_ITEM_PREFIX = "panels";
    private boolean initialized;

    public String getPanelBarItemPrefix() {
        return PANELBAR_ITEM_PREFIX;
    }
    
    public String getPanelBarItemSufix() {
        return PANELBAR_ITEM_SUFIX;
    }

    private int selectedIndex;

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public List<BillingAdapterWrapper> getBillingAdapters() {
        return billingAdapters;
    }

    public void setBillingAdapters(List<BillingAdapterWrapper> billingAdapters) {
        this.billingAdapters = billingAdapters;
    }

    public void addBillingAdapter(POBillingAdapter adapter) {
        this.billingAdapters.add(new BillingAdapterWrapper(adapter));

    }

    public void setSelectedPanel(String selectedPanel) {
        this.selectedPanel = selectedPanel;
    }

    public String getSelectedPanel() {
        return selectedPanel;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * @return
     */
    BillingAdapterWrapper getSelectedBillingAdapter() {
        return billingAdapters.get(selectedIndex);
    }
}
