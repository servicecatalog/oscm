/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.kafka.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.internal.types.exception.SaaSSystemException;

import java.util.Properties;

public class ServiceLocator {

    public static <T> T findService(final Class<T> clazz) {

        try {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.client.LocalInitialContextFactory");
            Context context = new InitialContext(p);
            T service = clazz.cast(context.lookup(clazz.getName()));
            return service;
        } catch (NamingException e) {
            throw new SaaSSystemException("Service lookup failed!", e);
        }
    }

}
