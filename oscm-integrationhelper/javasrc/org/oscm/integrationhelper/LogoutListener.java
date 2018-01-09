/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationhelper;

import java.io.IOException;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Monitor session destroyed to informs the platform about a logout and also
 * monitor session created.
 */
public class LogoutListener implements HttpSessionListener {

    /**
     * This method monitors HttpSession created event.
     * 
     * @param event
     *            event notifications for changes to HttpSession
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
    }

    /**
     * This method monitors HttpSession destroyed event.
     * 
     * @param session
     *            event notifications for changes to HttpSession
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        try {
            BssClient.logoutUser(event.getSession());
        } catch (NumberFormatException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
