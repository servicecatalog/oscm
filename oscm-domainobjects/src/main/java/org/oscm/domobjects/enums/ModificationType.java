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

package org.oscm.domobjects.enums;

/**
 * ModificationType indicates wether a History-Entry for a Domain Object comes
 * from an Insert (ADD), Update (MODIFY) or Delete (DELETE) action.
 * 
 * @author schmid
 */
public enum ModificationType {
    ADD, MODIFY, DELETE
}
