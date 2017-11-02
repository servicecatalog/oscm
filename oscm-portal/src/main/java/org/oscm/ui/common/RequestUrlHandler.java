/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mao                                                    
 *                                                                              
 *  Creation Date: 29.08.2013                                                      
 *                                                                              
 *  Completion Time: 29.08.2013                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Validate if the url is accessible.
 * 
 * @author Mao
 * 
 */
public class RequestUrlHandler {

    public static boolean isUrlAccessible(String url) throws IOException {
        boolean result = false;
        if (url == null || url.length() <= 0) {
            return false;
        }

        URL requestUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) requestUrl.openConnection();
        result = (con.getResponseCode() == 200);
        return result;
    }
}
