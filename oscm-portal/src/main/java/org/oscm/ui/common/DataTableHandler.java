/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mao
 * 
 */
public class DataTableHandler {

    private static final String SERIALVERSIONUID = "serialVersionUID";
    private static final String JACOCO = "$jacocoData";

    static List<String> headers = new ArrayList<String>();

    public static List<String> getTableHeaders(String className)
            throws Exception {
        List<String> headers = new ArrayList<String>();
        Field[] fields = Class.forName(className).getDeclaredFields();
        for (Field field : fields) {
            if (!SERIALVERSIONUID.equals(field.getName())
                    && !JACOCO.equals(field.getName())) {
                headers.add(field.getName());
            }
        }
        return headers;
    }
}
