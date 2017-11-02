/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.mp.landingpage;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.validation.Invariants;
import org.oscm.internal.landingpage.EnterpriseLandingpageData;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.vo.VOCategory;

/**
 * Model data for the enterprise landing page. Services are listed for a maximum
 * of three categories and structured in a list for each category.
 * 
 * @author cheld
 * 
 */
@ViewScoped
@ManagedBean(name = "enterpriseLandingpageModel")
public class EnterpriseLandingpageModel {

    public List<LandingpageEntryModel> entriesOfCateogry0;

    public List<LandingpageEntryModel> entriesOfCateogry1;

    public List<LandingpageEntryModel> entriesOfCateogry2;

    String selectedEntryKey;

    int selectedCategory;

    boolean initilized;

    private EnterpriseLandingpageData result;

    public EnterpriseLandingpageModel() {
        this.initilized = false;
    }

    /**
     * Number of categories stored. The number corresponds to the number of
     * column in the landing page.
     */
    public int getNumberOfColumns() {
        return result.numberOfColumns();
    }

    /**
     * Returns the category for the given index. The index corresponds to the
     * column of the landing page.
     */
    public VOCategory getCategory(int indexOfColumn) {
        return result.getCategory(indexOfColumn);
    }

    public int getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(int selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getSelectedEntryKey() {
        return selectedEntryKey;
    }

    public void setSelectedEntryKey(String selectedEntryKey) {
        this.selectedEntryKey = selectedEntryKey;
    }

    public String getNameFirstCategory() {
        return getCategory(0).getName();
    }

    public String getNameSecondCategory() {
        return getCategory(1).getName();
    }

    public String getNameThirdCategory() {
        return getCategory(2).getName();
    }

    public String getIdFirstCategory() {
        return getCategory(0).getCategoryId();
    }

    public String getIdSecondCategory() {
        return getCategory(1).getCategoryId();
    }

    public String getIdThirdCategory() {
        return getCategory(2).getCategoryId();
    }

    public List<LandingpageEntryModel> getServicesFirstCategory() {
        return this.entriesOfCateogry0;
    }

    public List<LandingpageEntryModel> getServicesSecondCategory() {
        return this.entriesOfCateogry1;
    }

    public List<LandingpageEntryModel> getServicesThirdCategory() {
        return this.entriesOfCateogry2;
    }

    public boolean isInitialized() {
        return this.initilized;
    }

    public void setInitialized(boolean initialized) {
        this.initilized = initialized;
    }

    public LandingpageType getLandingpageType() {
        return LandingpageType.ENTERPRISE;
    }

    public void addLandingpageData(EnterpriseLandingpageData result) {
        this.result = result;
    }

    public EnterpriseLandingpageData getLandingpageData() {
        return this.result;
    }

    public void addEntries(int categoryId, List<LandingpageEntryModel> entries) {
        Invariants.assertBetween(categoryId, 0, 2);
        switch (categoryId) {
        case 0:
            this.entriesOfCateogry0 = entries;
            break;
        case 1:
            this.entriesOfCateogry1 = entries;
            break;
        default:
            this.entriesOfCateogry2 = entries;
        }
    }
}
