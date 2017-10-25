/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 20, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.local;

import javax.jms.Message;

/**
 * Local interface allowing to pass messages directly to the message handler.
 * 
 * @author Dirk Bernsau
 * 
 */
public interface TriggerProcessListenerLocal {
    public void onMessage(Message message);
}
