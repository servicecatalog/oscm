/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.authorization;

/**
 * Call-back to check the visibility of a menu item.
 * 
 * @author hoffmann
 */
public interface Condition {

    public boolean eval();

}
