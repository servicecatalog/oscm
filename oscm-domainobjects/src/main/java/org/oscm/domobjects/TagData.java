/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Oliver Soehnges                                                      
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: n/a                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TagData extends DomainDataContainer implements Serializable {

    private static final long serialVersionUID = 620084488785171924L;

    /**
     * Defined locale of this tag.
     */
    @Column(nullable = false)
    private String locale;

    /**
     * Value of tag.
     */
    @Column(nullable = false)
    private String value;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
