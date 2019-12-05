/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.intf.AccountService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.*;
import org.oscm.vo.*;
import org.oscm.webservices.logger.WebServiceLogger;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "AccountService", serviceName = "AccountService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.AccountService")
public class AccountServiceWS implements AccountService {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(AccountServiceWS.class);

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(AccountServiceWS.class));
    org.oscm.internal.intf.AccountService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public void deregisterOrganization() throws DeletionConstraintException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deregisterOrganization();
        } catch (org.oscm.internal.types.exception.DeletionConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            LOGGER.logError(
                    Log4jLogger.SYSTEM_LOG,
                    se,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY);
            throw se;
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            LOGGER.logError(
                    Log4jLogger.SYSTEM_LOG,
                    se,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY);
            throw se;
        }
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesForOrganization() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertSet(
                delegate.getAvailablePaymentTypesForOrganization(),
                VOPaymentType.class);
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesFromOrganization(
            Long serviceKey) throws OrganizationAuthoritiesException,
            ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertSet(delegate
                    .getAvailablePaymentTypesFromOrganization(serviceKey),
                    VOPaymentType.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOBillingContact> getBillingContacts() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getBillingContacts(),
                VOBillingContact.class);
    }

    @Override
    public List<VOOrganizationPaymentConfiguration> getCustomerPaymentConfiguration() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getCustomerPaymentConfiguration(),
                VOOrganizationPaymentConfiguration.class);
    }

    @Override
    public List<VOOrganization> getMyCustomers()
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(delegate.getMyCustomers(),
                    VOOrganization.class);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public Set<VOPaymentType> getDefaultPaymentConfiguration() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertSet(
                delegate.getDefaultPaymentConfiguration(), VOPaymentType.class);
    }

    @Override
    public VOOrganization getOrganizationData() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter.convertToApi(delegate.getOrganizationData());
    }

    @Override
    public String getOrganizationId(long subscriptionKey)
            throws ObjectNotFoundException, ServiceParameterException,
            OperationNotPermittedException, SubscriptionStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getOrganizationId(subscriptionKey);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceParameterException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOOrganization registerKnownCustomer(VOOrganization organization,
            VOUserDetails user, LdapProperties organizationProperties,
            String marketplaceId) throws OrganizationAuthoritiesException,
            ValidationException, NonUniqueBusinessKeyException,
            MailOperationException, ObjectNotFoundException,
            OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.registerKnownCustomer(
                    VOConverter.convertToUp(organization),
                    VOConverter.convertToUp(user),
                    VOConverter.convertToUp(organizationProperties),
                    marketplaceId));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOOrganization registerCustomer(VOOrganization voOrganization,
            VOUserDetails admin, String password, Long serviceKey,
            String marketplaceId, String sellerId)
            throws NonUniqueBusinessKeyException, ValidationException,
            ObjectNotFoundException, MailOperationException,
            RegistrationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.registerCustomer(
                    VOConverter.convertToUp(voOrganization),
                    VOConverter.convertToUp(admin), password, serviceKey,
                    marketplaceId, sellerId));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.RegistrationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOBillingContact saveBillingContact(VOBillingContact billingContact)
            throws ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.saveBillingContact(VOConverter
                            .convertToUp(billingContact)));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteBillingContact(VOBillingContact billingContact)
            throws ObjectNotFoundException, ConcurrentModificationException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteBillingContact(VOConverter
                    .convertToUp(billingContact));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean savePaymentConfiguration(
            Set<VOPaymentType> defaultConfiguration,
            List<VOOrganizationPaymentConfiguration> customerConfigurations,
            Set<VOPaymentType> defaultServiceConfiguration,
            List<VOServicePaymentConfiguration> serviceConfigurations)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate
                    .savePaymentConfiguration(
                            VOConverter
                                    .convertToUpVOPaymentType(defaultConfiguration),
                            VOCollectionConverter
                                    .convertList(
                                            customerConfigurations,
                                            org.oscm.internal.vo.VOOrganizationPaymentConfiguration.class),
                            VOCollectionConverter.convertSet(
                                    defaultServiceConfiguration,
                                    org.oscm.internal.vo.VOPaymentType.class),
                            VOCollectionConverter
                                    .convertList(
                                            serviceConfigurations,
                                            org.oscm.internal.vo.VOServicePaymentConfiguration.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOPaymentInfo savePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, PaymentDeregistrationException,
            NonUniqueBusinessKeyException, ConcurrentModificationException,
            ValidationException, OperationNotPermittedException,
            PaymentDataException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .savePaymentInfo(VOConverter.convertToUp(paymentInfo)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentDeregistrationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentDataException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void updateAccountInformation(VOOrganization voOrganization,
            VOUserDetails voUser, String marketplaceId,
            VOImageResource imageResource) throws ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ObjectNotFoundException,
            DistinguishedNameException, ConcurrentModificationException,
            ImageException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.updateAccountInformation(
                    VOConverter.convertToUp(voOrganization),
                    VOConverter.convertToUp(voUser), marketplaceId,
                    VOConverter.convertToUp(imageResource));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.DistinguishedNameException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ImageException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOOrganization updateCustomerDiscount(VOOrganization voOrganization)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .updateCustomerDiscount(VOConverter
                            .convertToUp(voOrganization)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public Set<String> getUdaTargetTypes() {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getUdaTargetTypes();
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitions() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getUdaDefinitions(),
                org.oscm.vo.VOUdaDefinition.class);
    }

    @Override
    public List<VOUda> getUdas(String targetType, long targetObjectKey)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getUdas(targetType, targetObjectKey, true),
                    org.oscm.vo.VOUda.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveUdaDefinitions(List<VOUdaDefinition> udaDefinitionsToSave,
            List<VOUdaDefinition> udaDefinitionsToDelete)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveUdaDefinitions(VOCollectionConverter.convertList(
                    udaDefinitionsToSave,
                    org.oscm.internal.vo.VOUdaDefinition.class, ds),
                    VOCollectionConverter.convertList(udaDefinitionsToDelete,
                            org.oscm.internal.vo.VOUdaDefinition.class, ds));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveUdas(List<VOUda> udas) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            MandatoryUdaMissingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveUdas(VOCollectionConverter.convertList(udas,
                    org.oscm.internal.vo.VOUda.class, ds));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MandatoryUdaMissingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            LOGGER.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    OrganizationAuthoritiesException.class.getName());
            throw new SaaSSystemException(e.getMessage());
        }
    }

    @Override
    public List<String> getSupportedCountryCodes() {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getSupportedCountryCodes();
    }

    @Override
    public VOImageResource loadImageOfOrganization(long organizationKey) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter.convertToApi(delegate
                .loadImageOfOrganization(organizationKey));
    }

    @Override
    public VOOrganization getSeller(String sellerId, String locale)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .getSeller(sellerId, locale));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deletePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, PaymentDeregistrationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deletePaymentInfo(VOConverter.convertToUp(paymentInfo));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentDeregistrationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOPaymentInfo> getPaymentInfos() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getPaymentInfos(),
                org.oscm.vo.VOPaymentInfo.class);
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypes() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertSet(
                delegate.getAvailablePaymentTypes(),
                org.oscm.vo.VOPaymentType.class);
    }

    @Override
    public Set<VOPaymentType> getDefaultServicePaymentConfiguration() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertSet(
                delegate.getDefaultServicePaymentConfiguration(),
                org.oscm.vo.VOPaymentType.class);
    }

    @Override
    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getServicePaymentConfiguration(),
                org.oscm.vo.VOServicePaymentConfiguration.class);
    }

    @Override
    public void addSuppliersForTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws ObjectNotFoundException, OperationNotPermittedException,
            AddMarketingPermissionException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.addSuppliersForTechnicalService(
                    VOConverter.convertToUp(technicalService), organizationIds);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.AddMarketingPermissionException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void removeSuppliersFromTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.removeSuppliersFromTechnicalService(
                    VOConverter.convertToUp(technicalService), organizationIds);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketingPermissionNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganization> getSuppliersForTechnicalService(
            VOTechnicalService technicalService)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(delegate
                    .getSuppliersForTechnicalService(VOConverter
                            .convertToUp(technicalService)),
                    org.oscm.vo.VOOrganization.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitionsForCustomer(String supplierId)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getUdaDefinitionsForCustomer(supplierId),
                    org.oscm.vo.VOUdaDefinition.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOUda> getUdasForCustomer(String targetType,
            long targetObjectKey, String supplierId)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getUdasForCustomer(targetType, targetObjectKey,
                            supplierId), org.oscm.vo.VOUda.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    public void method1() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method2() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method3() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method4() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method5() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method6() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method7() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method8() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method9() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method110() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void method11() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }
}
