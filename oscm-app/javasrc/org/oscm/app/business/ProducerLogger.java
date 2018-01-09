/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 02.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kulle
 *
 */
public class ProducerLogger {

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        String clazz = injectionPoint.getMember().getDeclaringClass().getName();
        return LoggerFactory.getLogger(clazz);
    }
}
