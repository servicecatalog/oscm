/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 20.05.2011                                                      
 *                                                                              
 *  Completion Time: 20.05.2011                                              
 *                                                                              
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.MarketplaceService;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAlreadyBannedException;
import org.oscm.types.exceptions.OrganizationAlreadyExistsException;
import org.oscm.types.exceptions.OrganizationAuthorityException;
import org.oscm.types.exceptions.PublishingToMarketplaceNotPermittedException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;

/**
 * This is a stub implementation of the {@link MarketplaceService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author weiser
 * 
 */
@WebService(serviceName = "MarketplaceService", targetNamespace = "http://oscm.org/xsd", portName = "MarketplaceServicePort", endpointInterface = "org.oscm.intf.MarketplaceService")
public class MarketplaceServiceImpl implements MarketplaceService {

    @Override
    public List<VOMarketplace> getMarketplacesForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCatalogEntry> getMarketplacesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails publishService(VOService service,
            List<VOCatalogEntry> entries)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            PublishingToMarketplaceNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOMarketplace getMarketplaceForSubscription(long subscriptionKey,
            String locale) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getMarketplacesOwned() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getMarketplacesForOperator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getAccessibleMarketplaces() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOMarketplace updateMarketplace(VOMarketplace vo)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException {
        throw new UnsupportedOperationException();

    }

    @Override
    public VOMarketplace createMarketplace(VOMarketplace vo)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addOrganizationsToMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException, OrganizationAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getOrganizationsForMarketplace(
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOMarketplace getMarketplaceById(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void banOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAlreadyBannedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void liftBanOrganizationsFromMarketplace(
            List<String> organizationIds, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getBannedOrganizationsForMarketplace(
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBrandingUrl(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveBrandingUrl(VOMarketplace marketplace, String brandingUrl)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

}
