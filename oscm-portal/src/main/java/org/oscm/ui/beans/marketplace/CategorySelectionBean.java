/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans.marketplace;

import java.io.Serializable;
import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.model.Category;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.vo.VOCategory;

@SessionScoped      
@ManagedBean(name="categorySelectionBean")
public class CategorySelectionBean extends BaseBean implements Serializable {

    private static final int SAME = 0;

    private static final long serialVersionUID = 4379336302422232706L;

    String lastUsedLanguage;
    String lastUsedMarketplaceId;
    String selectedCategoryId;
    
    List<VOCategory> categoriesForMarketplace = null;

    public String getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    /*
     * selects all categories for the current marketplace. the categories are
     * sorted by their localized names.
     */
    public List<Category> getCategoriesForMarketplace() {
        loadCategories();
        return new AbstractList<Category>() {

            @Override
            public Category get(int index) {
                return new Category(categoriesForMarketplace.get(index));
            }

            @Override
            public Spliterator<Category> spliterator() {
                return super.spliterator();
            }

            @Override
            public int size() {
                return categoriesForMarketplace.size();
            }
        };
    }

    void loadCategories() {
        if (this.categoriesForMarketplace == null || hasLanguageChanged()
                || hasMarketplaceChanged()) {
            lastUsedMarketplaceId = ui.getMarketplaceId();
            if (lastUsedLanguage == null) {
                lastUsedLanguage = getUserLanguage();
            }

            categoriesForMarketplace = ui.findService(
                    CategorizationService.class).getCategories(
                    ui.getMarketplaceId(), lastUsedLanguage);

            if (categoriesForMarketplace != null
                    && categoriesForMarketplace.size() > 1) {
                sortCategories();
            }
        }
    }

    boolean hasMarketplaceChanged() {
        return (lastUsedMarketplaceId == null || !lastUsedMarketplaceId
                .equals(ui.getMarketplaceId()));
    }

    void sortCategories() {
        Collections.sort(categoriesForMarketplace, categoriesComparator);
    }

    /**
     * language can be changed when user logs in or if user changes his profile
     * setting.
     */
    boolean hasLanguageChanged() {
        if ((lastUsedLanguage == null)
                || !lastUsedLanguage.equals(getUserLanguage())) {
            lastUsedLanguage = getUserLanguage();
            return true;
        }
        return false;
    }

    /**
     * Compares the display name of two categories.
     */
    private Comparator<VOCategory> categoriesComparator = new Comparator<VOCategory>() {

        public int compare(VOCategory category0, VOCategory category1) {
            String categoryName0 = Category.getDisplayName(category0);
            String categoryName1 = Category.getDisplayName(category1);

            // compare case insensitive
            int order = categoryName0.compareToIgnoreCase(categoryName1);

            // compare case sensitive if both names are equal without case
            if (order == SAME) {
                order = categoryName0.compareTo(categoryName1);
            }
            return order;
        }
    };

    public boolean isCategorySelected() {
        if (this.selectedCategoryId == null
                || this.selectedCategoryId.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean getHasCategories() {
        loadCategories();
        if (categoriesForMarketplace == null
                || categoriesForMarketplace.size() < 1) {
            return false;
        } else {
            return true;
        }
    }

    public String selectByCategory() {
        ServicePagingBean spb = ui.findServicePagingBean();
        spb.setFilterTag("");
        spb.setSelectedPage(1);
        return OUTCOME_SHOW_SERVICE_LIST;
    }

    public String resetCategorySelection() {
        this.selectedCategoryId = null;
        ui.findServicePagingBean().setFilterTag("");
        return OUTCOME_SHOW_SERVICE_LIST;
    }

    public String getCategoryCrumb() {
        if (this.selectedCategoryId == null || categoriesForMarketplace == null
                || categoriesForMarketplace.isEmpty())
            return null;
        for (VOCategory cat : categoriesForMarketplace) {
            if (cat.getCategoryId().equals(selectedCategoryId)) {
                return Category.getDisplayName(cat);
            }
        }
        return null;
    }

    public void resetCategoriesForMarketplace() {
        categoriesForMarketplace = null;
    }

    private void unselectCategory() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String view = fc.getViewRoot().getViewId();
        if (view.equals("/marketplace/index.xhtml"))
            this.selectedCategoryId = null;
    }

    public PhaseListener getListener() {
        return new PhaseListener() {
            private static final long serialVersionUID = -66585096775189540L;

            public PhaseId getPhaseId() {
                return PhaseId.RENDER_RESPONSE;
            }

            public void beforePhase(PhaseEvent event) {
                unselectCategory();
            }

            public void afterPhase(PhaseEvent arg0) {
                // nothing
            }
        };
    }

    @Override
    protected String getUserLanguage() {
        return super.getUserLanguage();
    }
}
