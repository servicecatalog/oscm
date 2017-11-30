/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Lorenz Goebel                                         
 *                                                                              
 *  Creation Date: 14.11.2012                                                      
 *                                                                              
 *  Completion Time: 14.11.2012                                    
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.converter;

import java.util.HashMap;
import java.util.Map;

import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.UIComponentStub;

/**
 * 
 * @author goebel
 */
class ConverterTestHelper {
    private static final String DATA_TYPE = "dataType";
    private static final String REQUIRED = "required";
    private static final String MIN_VALUE = "minValue";
    private static final String MAX_VALUE = "maxValue";

    /**
     * Helper method constructing a UI component.
     */
    static final UIComponentStub getComponent(boolean mandatory, Long min,
            Long max, String dataType) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DATA_TYPE, dataType);
        map.put(REQUIRED, Boolean.valueOf(mandatory));
        map.put(MIN_VALUE, min);
        map.put(MAX_VALUE, max);
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        return new UIComponentStub(map);
    }
}
