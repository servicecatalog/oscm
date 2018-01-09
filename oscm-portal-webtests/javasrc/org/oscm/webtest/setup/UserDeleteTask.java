/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 22, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 22, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Custom ANT task deleting users using the WS-API.
 * 
 * @author Dirk Bernsau
 * 
 */
public class UserDeleteTask extends WebtestTask {

    private String userIds;

    public void setUserIds(String value) {
        userIds = value;
    }

    @Override
    public void executeInternal() throws Exception {
        IdentityService idSvc = getServiceInterface(IdentityService.class);
        if (userIds == null || userIds.trim().length() == 0) {
            throwBuildException("No userIds specified - use the 'userIds' attribute to specify one or more users");
            return;
        }
        String[] split = userIds.split(",");
        List<String> deletionUserIds = new ArrayList<String>(split.length);
        for (int i = 0; i < split.length; i++) {
            if (split[i].trim().length() > 0) {
                deletionUserIds.add(split[i].trim());
            }
        }
        for (VOUserDetails voUd : idSvc.getUsersForOrganization()) {
            if (deletionUserIds.contains(voUd.getUserId())) {
                idSvc.deleteUser(voUd, null);
                deletionUserIds.remove(voUd.getUserId());
                log("Deleted user with ID " + voUd.getUserId());
            }
        }

        if (deletionUserIds.size() > 0) {
            logForIds("The following users were not found: ", deletionUserIds);
        }
    }
}
