/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: 20.05.2011                                                      
 *                                                                              
 *  Completion Time: 27.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.converter.api.ExceptionConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.converter.api.VOConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.internal.types.exception.MarketplaceValidationException;
import org.oscm.intf.MarketplaceService;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAlreadyBannedException;
import org.oscm.types.exceptions.OrganizationAlreadyExistsException;
import org.oscm.types.exceptions.OrganizationAuthorityException;
import org.oscm.types.exceptions.PublishingToMarketplaceNotPermittedException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.webservices.logger.WebServiceLogger;

/**
 * End point facade for marketplace WS.
 * 
 * @author Dirk Bernsau
 * 
 */
@WebService(name = "MarketplaceService", serviceName = "MarketplaceService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.MarketplaceService")
public class MarketplaceServiceWS implements MarketplaceService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(MarketplaceServiceWS.class));
    org.oscm.internal.intf.MarketplaceService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public List<VOMarketplace> getMarketplacesForOrganization() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getMarketplacesForOrganization(),
                org.oscm.vo.VOMarketplace.class);
    }

    @Override
    public List<VOCatalogEntry> getMarketplacesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getMarketplacesForService(
                            VOConverter.convertToUp(service)),
                    org.oscm.vo.VOCatalogEntry.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOServiceDetails publishService(VOService service,
            List<VOCatalogEntry> entries)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            PublishingToMarketplaceNotPermittedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate.publishService(
                    VOConverter.convertToUp(service),
                    VOCollectionConverter.convertList(entries,
                            org.oscm.internal.vo.VOCatalogEntry.class)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOMarketplace getMarketplaceForSubscription(long subscriptionKey,
            String locale) throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .getMarketplaceForSubscription(subscriptionKey, locale));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOMarketplace> getMarketplacesOwned() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getMarketplacesOwned(),
                org.oscm.vo.VOMarketplace.class);
    }

    @Override
    public List<VOMarketplace> getAccessibleMarketplaces() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getAccessibleMarketplaces(),
                org.oscm.vo.VOMarketplace.class);
    }

    @Override
    public List<VOMarketplace> getMarketplacesForOperator() {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getMarketplacesForOperator(),
                org.oscm.vo.VOMarketplace.class);
    }

    @Override
    public VOMarketplace updateMarketplace(VOMarketplace marketplace)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            UserRoleAssignmentException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .updateMarketplace(VOConverter.convertToUp(marketplace)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOMarketplace createMarketplace(VOMarketplace marketplace)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, UserRoleAssignmentException, org.oscm.types.exceptions.MarketplaceValidationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter.convertToApi(delegate
                    .createMarketplace(VOConverter.convertToUp(marketplace)));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.ValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.UserRoleAssignmentException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (MarketplaceValidationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.deleteMarketplace(marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void addOrganizationsToMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException, OrganizationAlreadyExistsException,
            MarketplaceAccessTypeUneligibleForOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.addOrganizationsToMarketplace(organizationIds,
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthorityException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAlreadyExistsException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void removeOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.removeOrganizationsFromMarketplace(organizationIds,
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthorityException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganization> getOrganizationsForMarketplace(
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter.convertList(
                    delegate.getOrganizationsForMarketplace(marketplaceId),
                    org.oscm.vo.VOOrganization.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public VOMarketplace getMarketplaceById(String marketplaceId)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOConverter
                    .convertToApi(delegate.getMarketplaceById(marketplaceId));
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void banOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAlreadyBannedException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.banOrganizationsFromMarketplace(organizationIds,
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthorityException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAlreadyBannedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void liftBanOrganizationsFromMarketplace(
            List<String> organizationIds, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.liftBanOrganizationsFromMarketplace(organizationIds,
                    marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OrganizationAuthorityException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganization> getBannedOrganizationsForMarketplace(
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return VOCollectionConverter
                    .convertList(
                            delegate.getBannedOrganizationsForMarketplace(
                                    marketplaceId),
                            org.oscm.vo.VOOrganization.class);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public String getBrandingUrl(String marketplaceId)
            throws ObjectNotFoundException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            return delegate.getBrandingUrl(marketplaceId);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void saveBrandingUrl(VOMarketplace marketplace, String brandingUrl)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {
        WS_LOGGER.logAccess(wsContext, ds);
        try {
            delegate.saveBrandingUrl(VOConverter.convertToUp(marketplace),
                    brandingUrl);
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
}
