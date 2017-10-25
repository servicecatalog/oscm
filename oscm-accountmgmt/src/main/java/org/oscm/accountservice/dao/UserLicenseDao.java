/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;

/**
 * @author qiu
 * 
 */
@Stateless
@LocalBean
public class UserLicenseDao {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    public long countRegisteredUsers() {
        Query q = dm.createNamedQuery("PlatformUser.countRegisteredUsers");
        Long count = (Long) q.getSingleResult();
        return count.longValue();
    }

    public List<PlatformUser> getPlatformOperators() {
        Query query = dm
                .createNamedQuery("RoleAssignment.getPlatformOperators");
        List<PlatformUser> users = ParameterizedTypes.list(
                query.getResultList(), PlatformUser.class);
        return users;
    }
}
