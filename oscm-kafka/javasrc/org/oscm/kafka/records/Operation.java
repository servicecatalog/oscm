/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 11.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.records;

import com.google.gson.annotations.SerializedName;

/**
 * @author stavreva
 *
 */
public enum Operation {
    @SerializedName(SerializedValues.OPTION_UPDATE)
    UPDATE, //

    @SerializedName(SerializedValues.OPTION_DELETE)
    DELETE; //

    public static class SerializedValues {
        public static final String OPTION_UPDATE = "upd";
        public static final String OPTION_DELETE = "del";
    }

}
