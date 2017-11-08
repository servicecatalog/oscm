/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 1, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author zhaoh.fnst
 * 
 */
public class UserGroups {

    public static UserGroup createUserGroup(DataService mgr, String name,
            Organization org, boolean isDefault, String description,
            String referenceId, PlatformUser user)
            throws NonUniqueBusinessKeyException {
        UserGroup group = new UserGroup();

        group.setName(name);
        group.setDescription(description);
        group.setReferenceId(referenceId);
        group.setIsDefault(isDefault);
        group.setOrganization(org);
        mgr.persist(group);
        mgr.flush();

        if (user != null) {
            UserGroupToUser g2u = new UserGroupToUser();
            g2u.setUserGroup(group);
            g2u.setPlatformuser(user);
            mgr.persist(g2u);
            mgr.flush();
        }
        return group;
    }

    public static UserGroupToUser assignUserToGroup(DataService mgr,
            PlatformUser user, UserGroup group)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        mgr.getReference(UserGroup.class, group.getKey());

        UserGroupToUser g2u = new UserGroupToUser();
        g2u.setUserGroup(group);
        g2u.setPlatformuser(user);
        mgr.persist(g2u);
        mgr.flush();

        return g2u;
    }
}
