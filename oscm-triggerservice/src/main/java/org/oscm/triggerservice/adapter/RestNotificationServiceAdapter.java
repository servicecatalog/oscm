/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 8, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Query;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.notification.vo.VONotification;
import org.oscm.triggerservice.data.TriggerProcessRepresentation;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Notification service adapter for REST web services
 * 
 * @author miethaner
 */
public class RestNotificationServiceAdapter implements
        INotificationServiceAdapter {

    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private DataService ds;
    private WebTarget r;

    @Override
    public void billingPerformed(String xmlBillingData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onAddSupplier(VOTriggerProcess triggerProcess, String supplierId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRemoveSupplier(VOTriggerProcess triggerProcess,
            String supplierId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRegisterCustomer(VOTriggerProcess triggerProcess,
            VOOrganization organization, VOUserDetails user,
            Properties organizationProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveCustomerPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOOrganizationPaymentConfiguration customerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onActivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDeactivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscribeToProduct(VOTriggerProcess triggerProcess,
            VOSubscription subscription, VOService product,
            List<VOUsageLicense> users) {

        try {
            TriggerProcess process = ds.getReference(TriggerProcess.class,
                    triggerProcess.getKey());

            Subscription sub = new Subscription();
            sub.setKey(0);
            sub.setSubscriptionId(subscription.getSubscriptionId());

            ParameterSet set = new ParameterSet();

            List<Parameter> params = new ArrayList<Parameter>();

            for (VOParameter vop : product.getParameters()) {
                Parameter p = new Parameter();
                ParameterDefinition pd = new ParameterDefinition();
                pd.setParameterId(vop.getParameterDefinition().getParameterId());
                p.setParameterDefinition(pd);
                p.setValue(vop.getValue());
                params.add(p);
            }
            set.setParameters(params);

            Product prod = new Product();
            prod.setProductId(product.getServiceId());
            prod.setParameterSet(set);

            handleRequest(process, sub, prod);

        } catch (ObjectNotFoundException | ClientErrorException e) {
            throw new SaaSSystemException(
                    "Failed to send notification to rest endpoint");
        }
    }

    @Override
    public void onUnsubscribeFromProduct(VOTriggerProcess triggerProcess,
            String subId) {

        try {
            TriggerProcess process = ds.getReference(TriggerProcess.class,
                    triggerProcess.getKey());

            Query q = ds.createNamedQuery("Subscription.findByBusinessKey");

            q.setParameter("subscriptionId", subId);
            q.setParameter("organizationKey", new Long(process
                    .getTriggerDefinition().getOrganization().getKey()));

            Subscription sub = (Subscription) q.getSingleResult();

            Product prod = sub.getProduct();

            handleRequest(process, sub, prod);

        } catch (ObjectNotFoundException | ClientErrorException e) {
            throw new SaaSSystemException(
                    "Failed to send notification to rest endpoint");
        }
    }

    @Override
    public void onModifySubscription(VOTriggerProcess triggerProcess,
            VOSubscription subscription, List<VOParameter> modifiedParameters) {

        try {
            TriggerProcess process = ds.getReference(TriggerProcess.class,
                    triggerProcess.getKey());

            Subscription sub = ds.getReference(Subscription.class,
                    subscription.getKey());

            Product prod = new Product();
            prod.setProductId(sub.getProduct().getProductId());

            ParameterSet set = new ParameterSet();

            List<Parameter> params = new ArrayList<Parameter>();

            for (VOParameter vop : modifiedParameters) {
                Parameter p = new Parameter();
                ParameterDefinition pd = new ParameterDefinition();
                pd.setParameterId(vop.getParameterDefinition().getParameterId());
                p.setParameterDefinition(pd);
                p.setValue(vop.getValue());
                params.add(p);
            }
            set.setParameters(params);

            prod.setParameterSet(set);

            handleRequest(process, sub, prod);

        } catch (ObjectNotFoundException | ClientErrorException e) {
            throw new SaaSSystemException(
                    "Failed to send notification to rest endpoint");
        }
    }

    @Override
    public void onUpgradeSubscription(VOTriggerProcess triggerProcess,
            VOSubscription current, VOService newProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onAddRevokeUser(VOTriggerProcess triggerProcess,
            String subscriptionId, List<VOUsageLicense> usersToBeAdded,
            List<VOUser> usersToBeRevoked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveServicePaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOServicePaymentConfiguration serviceConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveServiceDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionCreation(VOTriggerProcess triggerProcess,
            VOService service, List<VOUsageLicense> users,
            VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionModification(VOTriggerProcess triggerProcess,
            List<VOParameter> modifiedParameters, VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionTermination(VOTriggerProcess triggerProcess,
            VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRegisterUserInOwnOrganization(
            VOTriggerProcess triggerProcess, VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCancelAction(long actionKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotificationService(Object notificationService) {
        this.r = (WebTarget) notificationService;
    }

    @Override
    public void setConfigurationService(
            ConfigurationServiceLocal configurationService) {
    }

    @Override
    public void setDataService(DataService dataService) {
        this.ds = dataService;
    }

    private void handleRequest(TriggerProcess process,
            Subscription subscription, Product product) {
        TriggerProcessRepresentation rep = new TriggerProcessRepresentation(
                process, subscription, product);

        Gson gson = new GsonBuilder().setDateFormat(dateFormat).create();
        String json = gson.toJson(rep);


        Response response = r.request().post(Entity.json(json));

        if (response == null
                || response.getStatus() != Response.Status.NO_CONTENT
                        .getStatusCode()) {
            throw new SaaSSystemException(
                    "Failed to send notification to rest endpoint");
        }
    }

}
