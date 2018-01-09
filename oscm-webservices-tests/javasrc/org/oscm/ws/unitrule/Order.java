/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                  
 *  Creation Date: 18.06.15 15:41
 *
 *******************************************************************************/

package org.oscm.ws.unitrule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by FlorekS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Order {
    int order();
}
