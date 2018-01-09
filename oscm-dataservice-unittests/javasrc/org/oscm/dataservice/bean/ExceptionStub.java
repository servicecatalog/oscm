/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid                                                 
 *                                                                              
 *  Creation Date: 04.03.2009                                                      
 *                                                                              
 *  Completion Time:                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.bean;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author schmid
 * 
 */
@Remote
public interface ExceptionStub {

    public void throwSystemException();

    public void throwApplicationException() throws SaaSApplicationException;

    public boolean findData(String id);

}
