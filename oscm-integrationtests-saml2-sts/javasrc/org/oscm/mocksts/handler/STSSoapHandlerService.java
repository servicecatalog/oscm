/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 07.06.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.mocksts.handler;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Gao
 * 
 */
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface STSSoapHandlerService {

    @WebMethod
    public void initMessageList();

    @WebMethod
    public List<String> getCollectedMessages();
}
