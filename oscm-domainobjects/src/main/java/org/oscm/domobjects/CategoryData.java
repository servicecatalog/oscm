/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container to hold the information on each category.
 * 
 * @author Mani Afschar
 * 
 */
@Embeddable
public class CategoryData extends DomainDataContainer {

    private static final long serialVersionUID = -892519799123672376L;

    /**
     * The identifier of the category.
     */
    @Column(nullable = false)
    private String categoryId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

}
