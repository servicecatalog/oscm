/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                            
 *                                                                              
 *  Creation Date: 01.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.local;

public interface ILdapResultMapper<T> {

    /**
     * Return the names of the attributes which are read.
     * 
     * @return the names of the attributes which are read.
     */
    String[] getAttributes();

    /**
     * Processes the attribute values of a found search result
     * 
     * @param values
     *            the attribute values of one search result
     */
    T map(String[] values);
}
