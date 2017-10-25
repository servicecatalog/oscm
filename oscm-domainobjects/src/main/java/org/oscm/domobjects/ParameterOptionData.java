/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 10, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container to hold the information on each ParameterOption.
 * 
 * @author Ravi
 * 
 */
@Embeddable
public class ParameterOptionData extends DomainDataContainer {

    private static final long serialVersionUID = -3610156668828285331L;

    @Column(nullable = false)
    private String optionId;

    /**
     * @return the optionId
     */
    public String getOptionId() {
        return optionId;
    }

    /**
     * @param optionId
     *            the optionId to set
     */
    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }
}
