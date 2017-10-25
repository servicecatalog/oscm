/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package internal;

import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.enumtypes.SubscriptionStatus;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.PaymentDataException;
import org.oscm.types.exceptions.PaymentInformationException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VORoleDefinition;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceOperationParameterValues;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.vo.VOTechnicalServiceOperation;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserSubscription;

/**
 * This is a stub implementation of the {@link SubscriptionService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "SubscriptionService", targetNamespace = "http://oscm.org/xsd", portName = "SubscriptionServicePort", endpointInterface = "org.oscm.intf.SubscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

    @Override
    public void abortAsyncSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addRevokeUser(String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void completeAsyncSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserSubscription> getSubscriptionsForCurrentUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getCustomersForSubscriptionId(
            String subscriptionIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOSubscriptionIdAndOrganizations> getCustomerSubscriptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscriptionDetails getSubscriptionDetails(String subId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscriptionDetails getSubscriptionForCustomer(
            String organizationId, String subscriptionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSubscriptionIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOSubscription> getSubscriptionsForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOService> getUpgradeOptions(String subscriptionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscriptionDetails modifySubscription(
            VOSubscription subscription, List<VOParameter> modifiedParameters,
            List<VOUda> udas) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unsubscribeFromService(String subId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscription upgradeSubscription(VOSubscription current,
            VOService newProduct, VOPaymentInfo paymentInfo,
            VOBillingContact billingContact, List<VOUda> udas) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscription subscribeToService(VOSubscription subscription,
            VOService product, List<VOUsageLicense> users,
            VOPaymentInfo paymentInfo, VOBillingContact billingContact,
            List<VOUda> udas) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VORoleDefinition> getServiceRolesForSubscription(
            String subscription) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VORoleDefinition> getServiceRolesForService(VOService service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsyncSubscriptionProgress(String subscriptionId,
            String organizationId, List<VOLocalizedText> progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void executeServiceOperation(VOSubscription subscription,
            VOTechnicalServiceOperation operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void terminateSubscription(VOSubscription subscription, String reason)
            throws ObjectNotFoundException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, ConcurrentModificationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCurrentUserSubscriptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOSubscriptionDetails modifySubscriptionPaymentData(
            VOSubscription subscription, VOBillingContact billingContact,
            VOPaymentInfo paymentInfo) throws ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            PaymentInformationException, SubscriptionStateException,
            PaymentDataException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reportIssue(String subscriptionId, String subject,
            String issueText) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void completeAsyncModifySubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortAsyncModifySubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void completeAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOServiceOperationParameterValues> getServiceOperationParameterValues(
            VOSubscription subscription, VOTechnicalServiceOperation operation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAccessInformation(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsyncOperationProgress(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsyncSubscriptionStatus(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifySubscriptionAboutVmsNumber(String subscriptionId,
        String organizationId, VOInstanceInfo instanceInfo) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

}
