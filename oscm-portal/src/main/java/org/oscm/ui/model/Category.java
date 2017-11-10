/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                          
 *                                                                              
 *  Creation Date: 14.05.2012                                                      
 *                                                                                                                     
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOCategory;

/**
 * 
 * Adapter class for VOCategory.
 * 
 * @author cheld
 * 
 */
public class Category {

    private VOCategory category;

    public Category(VOCategory category) {
        this.category = category;
    }

    /**
     * Returns the localized name of the category for the language specified by
     * the caller or, if this name does not exist, the category's identifier.
     * 
     * @return the name or identifier
     */
    public String getDisplayName() {
        return getDisplayName(category);
    }

    public String getCategoryId() {
        return category.getCategoryId();
    }

    /**
     * Returns the localized name of the category for the language specified by
     * the caller or, if this name does not exist, the category's identifier.
     * 
     * @return the name or identifier
     */
    public static String getDisplayName(VOCategory category) {
        if (category.getName() == null || category.getName().length() == 0) {
            return category.getCategoryId();
        } else {
            return category.getName();
        }
    }

}
