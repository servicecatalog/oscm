/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * File billing application with REST interface.
 */
@ApplicationPath("/rest")
public class FileBillingApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(BillingAppResource.class);
        classes.add(PriceModelResource.class);
        classes.add(PriceModelFileResource.class);
        return classes;
    }

}
