/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.02.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.util.Map;
import java.util.UUID;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.taskhandling.payloads.ExternalPriceModelPayload;
import org.oscm.taskhandling.payloads.TaskPayload;
import org.oscm.types.enumtypes.LogMessageIdentifier;

public class ExternalPriceModelHandler extends TaskHandler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ExternalPriceModelHandler.class);

    private ExternalPriceModelPayload payload;

    @Override
    public void execute() throws Exception {

        PriceModel priceModel = payload.getPriceModel();
        UUID uuid = priceModel.getId();

        serviceFacade.getExternalPriceModelService().updateCache(priceModel);

        Map<ContextKey, ContextValue<?>> context = priceModel.getContext();
        String subscriptionId = (String) context.get(ContextKey.SUBSCRIPTION_ID)
                .getValue();
        String organizationId = (String) context.get(ContextKey.TENANT_ID)
                .getValue();

        DataService dataService = serviceFacade.getDataService();

        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) dataService
                .getReferenceByBusinessKey(organization);
        long organizationKey = organization.getKey();

        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setOrganizationKey(organizationKey);
        subscription = (Subscription) dataService
                .find(subscription);
        subscription.getPriceModel().setUuid(uuid);

        dataService.flush();
        dataService.refresh(subscription);
    }

    @Override
    public void handleError(Exception cause) throws Exception {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, cause,
                LogMessageIdentifier.ERROR_RETRIEVAL_EXTERNAL_PRICE_MODEL_FAILED,
                payload.getInfo());
    }

    @Override
    void setPayload(TaskPayload payload) {
        this.payload = (ExternalPriceModelPayload) payload;
    }

}
