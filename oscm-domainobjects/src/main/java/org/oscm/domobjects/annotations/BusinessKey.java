/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The BusinessKey annotation specifies these fields of a DomainObjectWithHistory defining
 * a unique and identifying feature set of the domain object. The annotation is
 * interpreted by the DataManager for various search tasks. A DomainObjectWithHistory not
 * declaring its business key can be found only via its artificial technical
 * key.
 * 
 * @author schmid
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessKey {
    String[] attributes();
}
