/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.dialog.classic.billingadapter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.billingadapter.POBillingAdapter;

/**
 *
 * @author BadziakP
 */
@ManagedBean
@ViewScoped
public class BillingAdapterListModel {

	private List<BillingAdapterWrapper> billingAdapters = new ArrayList<BillingAdapterWrapper>();

	private String selectedPanel;

	private int selectedIndex;

	public List<BillingAdapterWrapper> getBillingAdapters() {
		return billingAdapters;
	}

	public void setBillingAdapters(List<BillingAdapterWrapper> billingAdapters) {
		this.billingAdapters = billingAdapters;
	}

	public String getSelectedPanel() {
		return selectedPanel;
	}

	public void setSelectedPanel(String selectedPanel) {
		this.selectedPanel = selectedPanel;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	/**
	 * Method is used to get billing adapter selected by user.
	 *
	 * @return Selected billing adapter
	 */
	public POBillingAdapter getSelectedBillingAdapter() {
		int selectedIndex = getSelectedIndex();
		POBillingAdapter adapter = getBillingAdapters().get(selectedIndex)
				.getAdapter();
		return adapter;
	}
}
