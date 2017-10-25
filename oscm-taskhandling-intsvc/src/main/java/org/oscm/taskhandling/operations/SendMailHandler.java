/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 30, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.util.List;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.taskhandling.payloads.MailDefiniton;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.taskhandling.payloads.TaskPayload;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Task handling object to send e-mail.
 * 
 * @author tokoda
 */
public class SendMailHandler extends TaskHandler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SendMailHandler.class);

    private SendMailPayload payload;

    @Override
    public void setPayload(TaskPayload payload) {
        this.payload = (SendMailPayload) payload;
    }

    public void execute() throws ObjectNotFoundException,
            MailOperationException {
        

        List<MailDefiniton> mailObjects = payload.getMailObjects();
        for (MailDefiniton mailObject : mailObjects) {
            // determine marketplace
            Long marketplaceKey = mailObject.getMarketplaceKey();
            Marketplace marketplace = null;
            if (marketplaceKey != null) {
                marketplace = serviceFacade.getDataService().getReference(
                        Marketplace.class, marketplaceKey.longValue());
            }

            // determine user
            PlatformUser user = serviceFacade.getDataService().getReference(
                    PlatformUser.class, mailObject.getKey().longValue());

            // send mail
            serviceFacade.getCommunicationService().sendMail(user,
                    mailObject.getType(), mailObject.getParams(), marketplace);
        }

        
    }

    public void handleError(Exception cause) throws Exception {
        
        if (cause instanceof MailOperationException) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cause,
                    LogMessageIdentifier.WARN_MAIL_SENDING_TASK_FAILED,
                    payload.getInfo());
        } else {
            
            throw cause;
        }
        
    }
}
