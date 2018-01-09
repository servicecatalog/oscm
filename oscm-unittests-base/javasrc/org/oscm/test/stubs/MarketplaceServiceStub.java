/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.MarketplaceValidationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAlreadyBannedException;
import org.oscm.internal.types.exception.OrganizationAlreadyExistsException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

public class MarketplaceServiceStub implements MarketplaceService {

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
            List<VOCatalogEntry> entries) throws ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException {
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
    public VOMarketplace updateMarketplace(VOMarketplace marketplace)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException {
        throw new UnsupportedOperationException();

    }

    @Override
    public VOMarketplace createMarketplace(VOMarketplace marketplace)
            throws OperationNotPermittedException, ObjectNotFoundException, ValidationException, UserRoleAssignmentException, MarketplaceValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addOrganizationsToMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException,
            OrganizationAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeOrganizationsFromMarketplace(
            List<String> organizationIds, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getOrganizationsForMarketplace(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException,
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
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException,
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

    @Override
    public List<VOOrganization> getAllOrganizations(String marketplaceId) {
        return null;
    }

    @Override
    public void grantAccessToMarketPlaceToOrganization(
            VOMarketplace voMarketplace, VOOrganization voOrganization)
            throws ValidationException, NonUniqueBusinessKeyException {

    }

    @Override
    public void openMarketplace(String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException {

    }

    @Override
    public List<VOMarketplace> getRestrictedMarketplaces() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesOrganizationHaveAccessMarketplace(String marketplaceId,
            String organizationId) throws LoginException {
        return false;
    }

    @Override
    public void closeMarketplace(String marketplaceId,
            Set<Long> authorizedOrganizations,
            Set<Long> unauthorizedOrganizations)
            throws OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getAllOrganizationsWithAccessToMarketplace(
            String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarketplaceConfiguration getCachedMarketplaceConfiguration(
            String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCachedMarketplaceConfiguration(String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getAllMarketplacesForTenant(Long tenantKey)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTenantIdFromMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getSuppliersForMarketplace(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMarketplaceIdForKey(Long key)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

}
