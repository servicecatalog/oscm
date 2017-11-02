/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.oscm.internal.intf.SessionService;

/**
 * SessionListener which manages a map with all sessions in the servlet context
 * 
 */
public class SessionListener implements HttpSessionListener {

    private static Map<String, HttpSession> sessionMap = Collections
            .synchronizedMap(new HashMap<String, HttpSession>());

    /**
     * Remove all subscriptions from the active subscription key map which are
     * not stored in the database anymore. Invalidate session for the given
     * sessionId if the user is neither logged on the platform nor on any
     * service.
     * 
     * @param sessionId
     *            the id of the HTTP session
     * @return true if the use is logged on the platform
     */
    public static boolean cleanup(String sessionId) {
        return cleanup(sessionMap.get(sessionId));
    }

    /**
     * Remove all subscriptions from the active subscription key map which are
     * not stored in the database anymore. Invalidate the given session if the
     * user is neither logged on the platform nor on any service.
     * 
     * @param session
     *            the HTTP session
     * @return true if the use is logged on the platform
     */
    public static boolean cleanup(HttpSession session) {

        if (session == null) {
            return false;
        }

        List<Long> list = null;

        // remove all service from the internal map which are not stored in the
        // database
        Map<?, ?> map = (Map<?, ?>) session
                .getAttribute(Constants.SESS_ATTR_ACTIVE_SUB_MAP);
        if (map != null) {
            synchronized (map) {
                SessionService service = ServiceAccess.getServiceAcccessFor(
                        session).getService(SessionService.class);
                list = service.getSubscriptionKeysForSessionId(session.getId());

                if (list == null || list.isEmpty()) {
                    map.clear();
                } else {
                    for (Iterator<?> it = map.keySet().iterator(); it.hasNext();) {
                        String subKey = (String) it.next();
                        boolean found = false;
                        for (Long subTkey : list) {
                            if (subTkey != null
                                    && Long.toHexString(subTkey.longValue())
                                            .equals(subKey)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            it.remove();
                        }
                    }
                }
            }
        }

        if (session.getAttribute(Constants.SESS_ATTR_USER) == null) {
            // the user is not logged in the platform
            if (list == null || list.isEmpty()) {
                session.invalidate();
                return false;
            }
        }
        return true;
    }

    public void sessionCreated(HttpSessionEvent event) {
        sessionMap.put(event.getSession().getId(), event.getSession());
    }

    public void sessionDestroyed(HttpSessionEvent event) {

        HttpSession session = event.getSession();
        sessionMap.remove(session.getId());

        ServiceAccess serviceAccess = ServiceAccess
                .getServiceAcccessFor(session);
        serviceAccess.getService(SessionService.class)
                .deleteSessionsForSessionId(event.getSession().getId());
    }

}
