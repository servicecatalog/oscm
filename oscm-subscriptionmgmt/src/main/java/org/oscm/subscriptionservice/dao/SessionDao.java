/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import java.util.List;

import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Session;
import org.oscm.interceptor.ExceptionMapper;

/**
 * @author Mao
 * 
 */
@Interceptors({ ExceptionMapper.class })
public class SessionDao {

    public SessionDao(DataService ds) {
        this.dataManager = ds;
    }

    private DataService dataManager;

    public List<Session> getActiveSessionsForUser(PlatformUser onbehalfUser) {
        Query query = dataManager
                .createNamedQuery("Session.getActiveSessionsForUser");
        query.setParameter("userKey", Long.valueOf(onbehalfUser.getKey()));
        return ParameterizedTypes.list(query.getResultList(), Session.class);
    }

}
