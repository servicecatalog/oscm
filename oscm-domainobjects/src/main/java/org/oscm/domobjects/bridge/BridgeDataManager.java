/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 12, 2011                                                      
 *                                                                              
 *  Completion Time: July 13, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.bridge;

import javax.persistence.EntityManager;

/**
 * Container class for thread local objects user in hibernate search bridge classes.
 * 
 * @author Dirk Bernsau
 * 
 */
public class BridgeDataManager {

    private static ThreadLocal<EntityManager> MANAGER = new ThreadLocal<EntityManager>();

    public static void registerEntityManager(EntityManager entityManager) {
        MANAGER.set(entityManager);
    }

    static EntityManager getEntityManager() {
        return MANAGER.get();
    }
}
