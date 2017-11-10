/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.oscm.ui.beans.marketplace.CategorySelectionBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.model.CategoryRow;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;

/**
 * The bean for the management of categories.
 * 
 * @author farmaki
 * 
 */
@ViewScoped
@ManagedBean(name = "categoryBean")
public class CategoryBean extends BaseBean implements Serializable {
    private static final long serialVersionUID = -6524681698960886091L;
    private static final int MAX_NUMBER_OF_APPENDED_MESSAGES = 10;
    private List<VOCategory> toDelete = null;
    private List<CategoryRow> categoriesRows = null;
    private boolean showConfirm = false;
    private boolean dirty = false;
    private String locale;
    private String marketplaceId = null;
    @ManagedProperty(value = "#{marketplaceBean}")
    private MarketplaceBean marketplaceBean;

    /**
     * @return The list of {@link CategoryRow}s.
     */
    public List<CategoryRow> getCategoriesRows() {
        if (categoriesRows == null) {
            String selectedLocale = getLocale();
            if (marketplaceId != null && marketplaceId.trim().length() > 0
                    && selectedLocale != null
                    && selectedLocale.trim().length() > 0) {
                loadCategories(selectedLocale);
            }
        }

        return categoriesRows;
    }

    /**
     * @param selectedLocale
     */
    private void loadCategories(String selectedLocale) {
        List<CategoryRow> result = new ArrayList<>();
        List<VOCategory> fetchedCategories = getCategorizationService()
                .getCategories(marketplaceId, selectedLocale);
        for (VOCategory voCategory : fetchedCategories) {
            result.add(new CategoryRow(voCategory));
        }
        categoriesRows = result;
    }

    /**
     * Creates an new {@link VOCategory} and adds it to the list of categories.
     * 
     */
    public void addCategory() {
        categoriesRows = add(categoriesRows);
        dirty = true;
    }

    private List<CategoryRow> add(List<CategoryRow> list) {
        VOCategory category = new VOCategory();
        category.setMarketplaceId(marketplaceId);
        CategoryRow categoryRow = new CategoryRow(category);

        list.add(categoryRow);
        return list;
    }

    /**
     * Save the not selected categories and delete the selected ones.
     * 
     * @return the logical outcome
     * @throws SaaSApplicationException
     */
    public String saveCategories() throws SaaSApplicationException {
        if (!isTokenValid()) {
            this.getMarketplaceBean().setMarketplaceId(null);
            return OUTCOME_REFRESH;
        }
        List<VOCategory> toSave = new ArrayList<>();
        toDelete = new ArrayList<>();
        for (CategoryRow row : categoriesRows) {
            if (row.isSelected() && !row.isNewCategory()) {
                toDelete.add(row.getCategory());
            } else if (!row.isSelected()) {
                toSave.add(row.getCategory());
            }
        }
        try {
            getCategorizationService().saveCategories(toSave, toDelete, locale);
            notifyCategorySelectionBean();
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_CATEGORIES_SAVED);

            resetCategoriesLists();
        } catch (ObjectNotFoundException e) {
            resetToken();
            loadCategories(getLocale());
            if (ClassEnum.MARKETPLACE == e.getDomainObjectClassEnum()) {
                this.getMarketplaceBean().resetMarketplaces();
                ui.handleException(e);
            } else {
                ui.handleException(new ConcurrentModificationException());
            }
        } finally {
            dirty = false;
        }

        return null;
    }

    /**
     * reset categories for marketplace so that fresh data is loaded if
     * currently logged in user wants to preview his changes.
     */
    private void notifyCategorySelectionBean() {
        CategorySelectionBean bean = ui.findCategorySelectionBean();
        bean.resetCategoriesForMarketplace();
    }

    /**
     * Reset all categories lists to <code>null</code>.
     */
    private void resetCategoriesLists() {
        this.categoriesRows = null;
    }

    public String getLocale() {
        if (locale == null) {
            locale = getUserFromSession().getLocale();
        }
        return locale;
    }

    public void localeValueChanged(ValueChangeEvent e) {
        // assign new value to locale
        locale = e.getNewValue().toString();
        resetCategoriesLists();
        getCategoriesRows();
    }

    public String getDisplayAsColumnHeader() {
        Locale currentLocale = new Locale(getUserFromSession().getLocale());
        Locale chooselanguage = new Locale(getLocale());
        return JSFUtils
                .getText("category.displayLocale",
                        new Object[] { chooselanguage
                                .getDisplayLanguage(currentLocale) });

    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isDirty() {
        return dirty;
    }

    /*
     * value change listener for marketplace chooser
     */
    public void processValueChange(final ValueChangeEvent event) {
        final String selectedMarketplaceId = (String) event.getNewValue();
        this.resetCategoriesLists();

        if (this.isMarketplaceValid(selectedMarketplaceId)) {
            this.setMarketplaceId(selectedMarketplaceId);
            this.getCategoriesRows();
        } else {
            this.setMarketplaceId(null);
        }

        this.marketplaceBean.processValueChange(event);
    }

    private boolean isMarketplaceValid(final String marketplaceId){
        return !marketplaceId.equals("0") && this.validateMarketplaceId(marketplaceId);
    }

    private boolean validateMarketplaceId(String marketplaceId) {
        try {
            this.getMarketplaceService().getMarketplaceById(marketplaceId);
        } catch (SaaSApplicationException e) {
            this.ui.handleException(e);
            return false;
        }
        return true;
    }

    @Override
    public String getMarketplaceId() {
        return marketplaceId;
    }

    @Override
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * Returns the message shown in the confirmation box indicating that some of
     * the categories to be deleted have assigned services.
     * 
     * @return a String displayed in the confirmation box.
     */
    public String getConfirmMessage() {
        if (toDelete != null && toDelete.size() > 0) {

            int numOfMessages = 0;
            StringBuilder buf = new StringBuilder();
            for (VOCategory voCategory : toDelete) {
                if (numOfMessages < MAX_NUMBER_OF_APPENDED_MESSAGES) {
                    String categoryMsg = getNumOfServicesToDisplay(voCategory);
                    if (categoryMsg.length() > 0) {
                        if (numOfMessages > 0) {
                            buf.append(JSFUtils.getText(
                                    "confirm.category.comma", null));
                        }
                        buf.append(categoryMsg);
                        numOfMessages++;
                    }
                } else {
                    buf.append(JSFUtils
                            .getText("confirm.category.points", null));
                    break;
                }
            }
            return JSFUtils.getText("confirm.categories.delete",
                    new Object[] { buf.toString() });
        }

        return "";
    }

    public String getNumOfServicesToDisplay(VOCategory voCategory) {
        if (voCategory != null) {
            // if there are existing services assigned to the category,
            // then show the category name and number of selected services.
            List<VOService> services = getCategorizationService()
                    .getServicesForCategory(voCategory.getKey());
            if (services != null && services.size() > 0) {
                return voCategory.getCategoryId();
            }
        }

        return "";
    }

    public boolean isShowConfirm() {
        return showConfirm;
    }

    public String setPopupAssignedServices() {
        // if there are existing services assigned to the category,
        // then show the category name and number of selected services.
        showConfirm = false;
        getCategoriesRows();

        toDelete = new ArrayList<>();

        if (categoriesRows != null) {
            for (CategoryRow row : categoriesRows) {
                if (row.isSelected() && !row.isNewCategory()) {
                    toDelete.add(row.getCategory());
                }
            }

            if (toDelete.size() > 0) {

                for (VOCategory voCategory : toDelete) {
                    List<VOService> services = getCategorizationService()
                            .getServicesForCategory(voCategory.getKey());
                    if (services != null && services.size() > 0) {
                        showConfirm = true;
                        break;
                    }
                }

            }
        }
        return "";
    }

    public MarketplaceBean getMarketplaceBean() {
        return this.marketplaceBean;
    }

    public void setMarketplaceBean(MarketplaceBean marketplaceBean) {
        this.marketplaceBean = marketplaceBean;
    }
}
