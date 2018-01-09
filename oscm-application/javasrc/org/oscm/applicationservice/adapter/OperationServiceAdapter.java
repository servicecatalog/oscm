/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 21.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import org.oscm.operation.intf.OperationService;

/**
 * @author weiser
 * 
 */
public interface OperationServiceAdapter extends OperationService {

    void setOperationService(Object port);

}
