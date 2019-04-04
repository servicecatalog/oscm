/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019
 *                                                                              
 *  Creation Date: 02-04-2019                                                      
 *                                                                              
 *******************************************************************************/

 package org.oscm.app.remote;

import javax.ejb.Remote;

@Remote
public interface APPCredentialsUpdateService {

  void updateUserCredentials(long userId, String username, String password);
}
