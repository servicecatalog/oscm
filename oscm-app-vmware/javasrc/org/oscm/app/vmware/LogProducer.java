/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kulle
 *
 */
public class LogProducer {

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        String clazz = injectionPoint.getMember().getDeclaringClass().getName();
        return LoggerFactory.getLogger(clazz);
    }

}
