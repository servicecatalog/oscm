/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * JPA managed entity representing the Landingpageservice data.
 */
@Embeddable
public class LandingpageProductData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 226658413689731229L;

    /**
     * position of services displayed on landingpage
     */
    @Column(nullable = false)
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
