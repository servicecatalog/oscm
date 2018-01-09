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

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.exceptions.BillingAdapterNotFoundException;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.CurrencyException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.ImportException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.PaymentInformationException;
import org.oscm.types.exceptions.PriceModelException;
import org.oscm.types.exceptions.SaaSSystemException;
import org.oscm.types.exceptions.ServiceCompatibilityException;
import org.oscm.types.exceptions.ServiceNotPublishedException;
import org.oscm.types.exceptions.ServiceOperationException;
import org.oscm.types.exceptions.ServiceStateException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.TechnicalServiceActiveException;
import org.oscm.types.exceptions.TechnicalServiceMultiSubscriptions;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.UnchangeableAllowingOnBehalfActingException;
import org.oscm.types.exceptions.UpdateConstraintException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCompatibleService;
import org.oscm.vo.VOCustomerService;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPriceModelLocalization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceActivation;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOServiceEntry;
import org.oscm.vo.VOServiceLocalization;
import org.oscm.vo.VOTechnicalService;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "ServiceProvisioningService", serviceName = "ServiceProvisioningService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.ServiceProvisioningService")
public class ServiceProvisioningServiceWS
        implements ServiceProvisioningService {

    Log4jLogger LOGGER = LoggerFactory
            .getLogger(ServiceProvisioningServiceWS.class);
    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(ServiceProvisioningServiceWS.class));
    org.oscm.internal.intf.ServiceProvisioningService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public VOService activateService(VOService product)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.activateService(VOConverter.convertToUp(product)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceNotPublishedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            SaaSSystemException ex = new SaaSSystemException(e.getMessage());
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    e.getClass().getName());
            throw ex;
        }
    }

    @Override
    public VOServiceDetails createService(VOTechnicalService technicalProduct,
            VOService productToCreate, VOImageResource voImageResource)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    ValidationException, NonUniqueBusinessKeyException,
                    ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.createService(
                    VOConverter.convertToUp(technicalProduct),
                    VOConverter.convertToUp(productToCreate),
                    VOConverter.convertToUp(voImageResource)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOTechnicalService createTechnicalService(
            VOTechnicalService technicalProduct)
                    throws OrganizationAuthoritiesException,
                    ValidationException, NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.createTechnicalService(
                    VOConverter.convertToUp(technicalProduct)));
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOService deactivateService(VOService product)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .deactivateService(VOConverter.convertToUp(product)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            SaaSSystemException ex = new SaaSSystemException(e.getMessage());
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    e.getClass().getName());
            throw ex;
        }
    }

    @Override
    public void deleteService(VOService product)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            ServiceOperationException, ServiceStateException,
            ConcurrentModificationException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteService(VOConverter.convertToUp(product));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteTechnicalService(VOTechnicalService technicalProduct)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            DeletionConstraintException, ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteTechnicalService(
                    VOConverter.convertToUp(technicalProduct));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.DeletionConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public byte[] exportTechnicalServices(List<VOTechnicalService> products)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.exportTechnicalServices(
                    VOCollectionConverter.convertList(products,
                            org.oscm.internal.vo.VOTechnicalService.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOCustomerService> getAllCustomerSpecificServices()
            throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getAllCustomerSpecificServices(),
                    org.oscm.vo.VOCustomerService.class);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> getCompatibleServices(VOService referenceProduct)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getCompatibleServices(
                            VOConverter.convertToUp(referenceProduct)),
                    org.oscm.vo.VOService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> getServicesForCustomer(VOOrganization customer)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getServicesForCustomer(
                            VOConverter.convertToUp(customer)),
                    org.oscm.vo.VOService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOPriceModelLocalization getPriceModelLocalization(
            VOPriceModel pricemodel) throws ObjectNotFoundException,
                    OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.getPriceModelLocalization(
                    VOConverter.convertToUp(pricemodel)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails getServiceDetails(VOService product)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .getServiceDetails(VOConverter.convertToUp(product)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails getServiceForCustomer(VOOrganization customer,
            VOService product) throws OperationNotPermittedException,
                    ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.getServiceForCustomer(
                    VOConverter.convertToUp(customer),
                    VOConverter.convertToUp(product)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceLocalization getServiceLocalization(VOService product)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .getServiceLocalization(VOConverter.convertToUp(product)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails getServiceForSubscription(VOOrganization customer,
            String subscriptionId) throws OrganizationAuthoritiesException,
                    ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.getServiceForSubscription(
                    VOConverter.convertToUp(customer), subscriptionId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<String> getSupportedCurrencies() {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getSupportedCurrencies();
    }

    @Override
    public String importTechnicalServices(byte[] xml) throws ImportException,
            OperationNotPermittedException, TechnicalServiceActiveException,
            UpdateConstraintException, TechnicalServiceMultiSubscriptions,
            UnchangeableAllowingOnBehalfActingException,
            BillingAdapterNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.importTechnicalServices(xml);
        } catch (org.oscm.internal.types.exception.UpdateConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ImportException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceActiveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceMultiSubscriptions e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UnchangeableAllowingOnBehalfActingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.BillingAdapterNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOImageResource loadImage(Long productKey) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOConverter.convertToApi(delegate.loadImage(productKey));
    }

    @Override
    public VOServiceDetails savePriceModel(VOServiceDetails productDetails,
            VOPriceModel priceModel) throws ObjectNotFoundException,
                    OperationNotPermittedException, CurrencyException,
                    ValidationException, ServiceStateException,
                    ConcurrentModificationException, PriceModelException {
        WS_LOGGER.logAccess(wsContext, ds);

        if ((productDetails != null
                && productDetails.getTechnicalService() != null
                && productDetails.getTechnicalService().isExternalBilling())
                || (priceModel != null && priceModel.isExternal())) {
            throw new PriceModelException(
                    "Unable to save price model for service with external billing adapter connected");
        }

        try {
            return VOConverter.convertToApi(delegate.savePriceModel(
                    VOConverter.convertToUp(productDetails),
                    VOConverter.convertToUp(priceModel)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.CurrencyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PriceModelException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails savePriceModelForCustomer(
            VOServiceDetails productDetails, VOPriceModel priceModel,
            VOOrganization customer) throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    CurrencyException, ValidationException,
                    ServiceStateException, ServiceOperationException,
                    ConcurrentModificationException, PriceModelException {
        WS_LOGGER.logAccess(wsContext, ds);

        if ((productDetails != null
                && productDetails.getTechnicalService() != null
                && productDetails.getTechnicalService().isExternalBilling())
                || (priceModel != null && priceModel.isExternal())) {
            throw new PriceModelException(
                    "Unable to save price model for service with external billing adapter connected");
        }

        try {
            return VOConverter.convertToApi(delegate.savePriceModelForCustomer(
                    VOConverter.convertToUp(productDetails),
                    VOConverter.convertToUp(priceModel),
                    VOConverter.convertToUp(customer)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.CurrencyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PriceModelException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails savePriceModelForSubscription(
            VOServiceDetails productDetails, VOPriceModel priceModel)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    CurrencyException, ValidationException,
                    ConcurrentModificationException, SubscriptionStateException,
                    PaymentInformationException, PriceModelException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.savePriceModelForSubscription(
                            VOConverter.convertToUp(productDetails),
                            VOConverter.convertToUp(priceModel)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.CurrencyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.SubscriptionStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PaymentInformationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PriceModelException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void savePriceModelLocalization(VOPriceModel pricemodel,
            VOPriceModelLocalization localization)
                    throws ObjectNotFoundException,
                    OperationNotPermittedException,
                    ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.savePriceModelLocalization(
                    VOConverter.convertToUp(pricemodel),
                    VOConverter.convertToUp(localization));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveServiceLocalization(VOService product,
            VOServiceLocalization localization) throws ObjectNotFoundException,
                    OperationNotPermittedException, ValidationException,
                    ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveServiceLocalization(VOConverter.convertToUp(product),
                    VOConverter.convertToUp(localization));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveTechnicalServiceLocalization(
            VOTechnicalService technicalProduct)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    UpdateConstraintException, ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveTechnicalServiceLocalization(
                    VOConverter.convertToUp(technicalProduct));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UpdateConstraintException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void setCompatibleServices(VOService sourceProduct,
            List<VOService> compatibleProducts)
                    throws OrganizationAuthoritiesException,
                    ObjectNotFoundException, OperationNotPermittedException,
                    ServiceCompatibilityException, ServiceStateException,
                    ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.setCompatibleServices(
                    VOConverter.convertToUp(sourceProduct),
                    VOCollectionConverter.convertList(compatibleProducts,
                            org.oscm.internal.vo.VOService.class));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceCompatibilityException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails updateService(VOServiceDetails productDetails,
            VOImageResource voImageResource) throws ObjectNotFoundException,
                    OrganizationAuthoritiesException,
                    OperationNotPermittedException, ValidationException,
                    NonUniqueBusinessKeyException, ServiceStateException,
                    ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.updateService(
                    preserveExistingValues(productDetails),
                    VOConverter.convertToUp(voImageResource)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void validateTechnicalServiceCommunication(
            VOTechnicalService technicalProduct) throws ObjectNotFoundException,
                    OperationNotPermittedException,
                    TechnicalServiceNotAliveException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.validateTechnicalServiceCommunication(
                    VOConverter.convertToUp(technicalProduct));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails copyService(VOService service, String serviceId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceStateException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            ValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .copyService(VOConverter.convertToUp(service), serviceId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> getSuppliedServices() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(delegate.getSuppliedServices(),
                org.oscm.vo.VOService.class);
    }

    @Override
    public List<VOService> getServicesForMarketplace(String marketplaceId) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getServicesForMarketplace(marketplaceId),
                org.oscm.vo.VOService.class);
    }

    @Override
    public VOImageResource loadImageForSupplier(String serviceId,
            String supplierId) throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.loadImageForSupplier(serviceId, supplierId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOLocalizedText> getPriceModelLicenseTemplateLocalization(
            VOServiceDetails service) throws ObjectNotFoundException,
                    OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getPriceModelLicenseTemplateLocalization(
                            VOConverter.convertToUp(service)),
                    org.oscm.vo.VOLocalizedText.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOService> setActivationStates(
            List<VOServiceActivation> activations)
                    throws ObjectNotFoundException, ServiceStateException,
                    OrganizationAuthoritiesException,
                    OperationNotPermittedException, ServiceOperationException,
                    TechnicalServiceNotAliveException,
                    ServiceNotPublishedException, OperationPendingException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.setActivationStates(
                            VOCollectionConverter.convertList(activations,
                                    org.oscm.internal.vo.VOServiceActivation.class)),
                    org.oscm.vo.VOService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.TechnicalServiceNotAliveException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceNotPublishedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationPendingException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            SaaSSystemException ex = new SaaSSystemException(e.getMessage());
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_CONVERT_TO_RUNTIME_EXCEPTION_FOR_COMPATIBILITY,
                    e.getClass().getName());
            throw ex;
        }
    }

    @Override
    public List<VOService> getRelatedServicesForMarketplace(VOService service,
            String marketplaceId, String locale)
                    throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter
                    .convertList(
                            delegate.getRelatedServicesForMarketplace(
                                    VOConverter.convertToUp(service),
                                    marketplaceId, locale),
                            org.oscm.vo.VOService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceEntry getServiceForMarketplace(Long serviceKey,
            String marketplaceId, String locale) throws ObjectNotFoundException,
                    OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.getServiceForMarketplace(
                    serviceKey, marketplaceId, locale));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOOrganization getServiceSeller(long serviceKey, String locale)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.getServiceSeller(serviceKey, locale));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<String> getInstanceIdsForSellers(List<String> organizationIds) {
        WS_LOGGER.logAccess(wsContext, ds);
        return delegate.getInstanceIdsForSellers(organizationIds);
    }

    @Override
    public VOService suspendService(VOService service, String reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .suspendService(VOConverter.convertToUp(service), reason));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOService resumeService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(
                    delegate.resumeService(VOConverter.convertToUp(service)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ServiceStateException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean statusAllowsDeletion(VOService service)
            throws OperationNotPermittedException,
            ConcurrentModificationException, ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate
                    .statusAllowsDeletion(VOConverter.convertToUp(service));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOCompatibleService> getPotentialCompatibleServices(
            VOService service) throws ObjectNotFoundException,
                    OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getPotentialCompatibleServices(
                            VOConverter.convertToUp(service)),
                    org.oscm.vo.VOCompatibleService.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public boolean isPartOfUpgradePath(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate
                    .isPartOfUpgradePath(VOConverter.convertToUp(service));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role) throws OrganizationAuthoritiesException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getTechnicalServices(EnumConverter.convert(role,
                            org.oscm.internal.types.enumtypes.OrganizationRoleType.class)),
                    org.oscm.vo.VOTechnicalService.class);
        } catch (org.oscm.internal.types.exception.OrganizationAuthoritiesException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    private org.oscm.internal.vo.VOServiceDetails preserveExistingValues(
            VOServiceDetails productDetails) throws ObjectNotFoundException,
                    OperationNotPermittedException {
        try {
            if (productDetails == null) {
                return null;
            }
            org.oscm.internal.vo.VOServiceDetails service = VOConverter
                    .convertToUp(productDetails);
            org.oscm.internal.vo.VOServiceDetails storedService = delegate
                    .getServiceDetails(VOConverter.convertToUp(productDetails));
            if (storedService != null) {
                service.setAutoAssignUserEnabled(
                        storedService.isAutoAssignUserEnabled());
            }
            return service;
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

}
