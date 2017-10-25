/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 26, 2014            
 *  
 *  author: cmin                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

/**
 * This class is responsible to handle TimeStamp info in String
 * 
 * @author cmin
 * 
 */
public class TimeStampUtil {

    /**
     * remove time stamp information for Id#TimeStamp
     * 
     * @param id
     * @return
     */
    public static String removeTimestampFromId(String id) {

        if (id != null && id.contains("#")) {
            return id.substring(0, id.lastIndexOf("#"));
        }
        return id;
    }
}
