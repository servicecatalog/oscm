/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service operation parameter and its possible values requested
 * from the technical service.
 * 
 * @author weiser
 * 
 */
public class VOServiceOperationParameterValues extends
        VOServiceOperationParameter {

    private static final long serialVersionUID = 4140051869809208509L;

    private List<String> values = new ArrayList<>();

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
