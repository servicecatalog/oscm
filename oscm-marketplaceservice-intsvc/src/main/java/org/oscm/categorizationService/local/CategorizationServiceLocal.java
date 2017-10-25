/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.categorizationService.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;

@Local
public interface CategorizationServiceLocal {

    /**
     * De-assign all categories from the catalogEntry
     * 
     * this method is implicitly invoked when the marketplace to which a product
     * is published is changed.
     * 
     * @param catalogEntry
     */
    void deassignAllCategories(CatalogEntry catalogEntry);

    /**
     * Change assignment of categories to a catalogEntry. If a category is not
     * included in the list of entries but exists in the catalogEntry it will be
     * removed from the database.
     * 
     * And vice versa, if a category is in the list of entries included but does
     * not exist in the catalogEntry it will be added to the assignment list.
     * @return false if the categories have no update
     *         true  if the categories have changed
     */
    boolean updateAssignedCategories(CatalogEntry catalogEntry,
            List<VOCategory> categories) throws ObjectNotFoundException;

}
