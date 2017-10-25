/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 4, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 4, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.util.Collections;
import java.util.List;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.taskhandling.payloads.TaskPayload;
import org.oscm.taskhandling.payloads.UpdateUserPayload;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;

/**
 * Task to update user data of the subscribed service.
 * 
 * @author tokoda
 * 
 */
public class UpdateUserHandler extends TaskHandler {

    private UpdateUserPayload payload;

    @Override
    public void setPayload(TaskPayload payload) {
        this.payload = (UpdateUserPayload) payload;
    }

    public void execute() throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ObjectNotFoundException {

        long subscriptionKey = payload.getSubscriptionKey();
        long usageLicenseKey = payload.getUsageLicenseKey();
        Subscription subscription = serviceFacade.getDataService()
                .getReference(Subscription.class, subscriptionKey);
        UsageLicense usageLicense = serviceFacade.getDataService()
                .getReference(UsageLicense.class, usageLicenseKey);
        serviceFacade.getApplicationService().updateUsers(subscription,
                Collections.singletonList(usageLicense));

    }

    public void handleError(Exception cause) throws Exception {

        long subscriptionKey = payload.getSubscriptionKey();

        Subscription subscription = serviceFacade.getDataService()
                .getReference(Subscription.class, subscriptionKey);

        TechnicalProduct tp = subscription.getProduct().getTechnicalProduct();
        Organization organization = tp.getOrganization();

        List<PlatformUser> adminList = organization.getOrganizationAdmins();
        PlatformUser[] admins = adminList.toArray(new PlatformUser[adminList
                .size()]);
        String[] params = new String[] { tp.getTechnicalProductId(),
                tp.getProvisioningURL() };

        serviceFacade.getCommunicationService().sendMail(
                EmailType.USER_UPDATE_FOR_SUBSCRIPTION_FAILED, params, null,
                admins);

    }
}
