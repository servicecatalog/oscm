/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.categorizationService.local.CategorizationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.QueryBasedObjectFactory;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceAccess;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.AuditLogDataInterceptor;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.landingpageService.local.LandingpageType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.Invariants;
import org.oscm.validation.VersionAndKeyValidator;
import org.oscm.validator.BLValidator;

/**
 * @author barzu
 */
@Stateless
@Local(MarketplaceServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class MarketplaceServiceLocalBean implements MarketplaceServiceLocal {

    private static final String FIELD_REVENUE_SHARE = "revenue share";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceServiceLocalBean.class);

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB(beanInterface = ServiceProvisioningPartnerServiceLocal.class)
    ServiceProvisioningPartnerServiceLocal partnerSrvProv;

    @EJB
    AccountServiceLocal accountService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB
    CommunicationServiceLocal commService;

    @EJB
    IdentityServiceLocal identityService;

    @Resource
    protected SessionContext sessionCtx;

    @EJB
    LandingpageServiceLocal landingpageService;

    @EJB
    CategorizationServiceLocal categorizationService;

    @EJB
    MarketplaceAuditLogCollector audit;

    @EJB
    MarketplaceAccessDao marketplaceAccessDao;

    @EJB
    MarketplaceCacheService marketplaceCache;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Marketplace> getAllMarketplaces() {

        Query query = ds.createNamedQuery("Marketplace.getAll");
        List<Marketplace> marketplaceList = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        return marketplaceList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Marketplace> getAllAccessibleMarketplacesForOrganization(
            long organizationKey) {

        Query query = ds.createNamedQuery("Marketplace.getAllAccessible");
        query.setParameter("organization_tkey", organizationKey);
        List<Marketplace> marketplaceList = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        return marketplaceList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Marketplace> getMarketplacesForSupplier() {

        Organization supplier = ds.getCurrentUser().getOrganization();

        Query query = ds.createNamedQuery(
                "Marketplace.findMarketplacesForPublishingForOrg");
        query.setParameter("organization_tkey",
                Long.valueOf(supplier.getKey()));
        query.setParameter("publishingAccessGranted",
                PublishingAccess.PUBLISHING_ACCESS_GRANTED);
        query.setParameter("publishingAccessDenied",
                PublishingAccess.PUBLISHING_ACCESS_DENIED);

        List<Marketplace> marketplaceList = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        return marketplaceList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @RolesAllowed({ "PLATFORM_OPERATOR", "SERVICE_MANAGER" })
    public void createRevenueModels(Marketplace mp,
            BigDecimal brokerRevenueShare, BigDecimal resellerRevenueShare,
            BigDecimal marketplaceRevenueShare) {
        // SERVICE_MANAGER is only allowed for 1.1 compatibility
        RevenueShareModel bmodel = createRevenueModel(
                RevenueShareModelType.BROKER_REVENUE_SHARE, brokerRevenueShare);
        RevenueShareModel rmodel = createRevenueModel(
                RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerRevenueShare);
        RevenueShareModel mmodel = createRevenueModel(
                RevenueShareModelType.MARKETPLACE_REVENUE_SHARE,
                marketplaceRevenueShare);
        mp.setBrokerPriceModel(bmodel);
        mp.setResellerPriceModel(rmodel);
        mp.setPriceModel(mmodel);
        try {
            ds.persist(bmodel);
            ds.persist(rmodel);
            ds.persist(mmodel);
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "revenue share model could not be persisted", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_MARKETPLACE_CREATION_FAILED);
            throw sse;
        }
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type, BigDecimal revenueShare) {
        RevenueShareModel model = new RevenueShareModel();
        model.setRevenueShare(revenueShare);
        model.setRevenueShareModelType(type);
        return model;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean updateOwningOrganization(Marketplace marketplace,
            final String newOwningOrganizationId, boolean forCreate)
            throws OperationNotPermittedException, ObjectNotFoundException {

        if (newOwningOrganizationId == null) {
            throw new java.lang.IllegalStateException(
                    "Owning organization must be set at this point");
        }

        // Check current organization, if already set.
        String currentOwningOrganizationId = (marketplace
                .getOrganization() != null)
                        ? marketplace.getOrganization().getOrganizationId()
                        : null;

        if (currentOwningOrganizationId == null || !currentOwningOrganizationId
                .equals(newOwningOrganizationId)) {
            // Organization assignment must only be changed by platform
            // operator!
            if (!ds.getCurrentUser().hasRole(UserRoleType.PLATFORM_OPERATOR)) {
                throw new OperationNotPermittedException();
            }

            Organization org = new Organization();
            org.setOrganizationId(newOwningOrganizationId);
            org = (Organization) ds.getReferenceByBusinessKey(org);

            if (!forCreate) {

                if (currentOwningOrganizationId != null) {
                    boolean isCurrentOrgMPOwner = marketplace.getOrganization()
                            .hasRole(OrganizationRoleType.MARKETPLACE_OWNER);
                    if (isCurrentOrgMPOwner) {
                        // Find if current organization has other marketplaces
                        // than chosen one if not remove the owner role!
                        List<Marketplace> orgMPs = marketplace.getOrganization()
                                .getMarketplaces();
                        boolean found = false;
                        for (Marketplace orgMp : orgMPs) {
                            Organization anOrg = orgMp.getOrganization();
                            if (anOrg == null)
                                continue;
                            if (currentOwningOrganizationId
                                    .equals(anOrg.getOrganizationId())
                                    && !orgMp.getMarketplaceId().equals(
                                            marketplace.getMarketplaceId())) {
                                found = true;
                                break;
                            }

                        }
                        if (!found) {
                            // NO other marketplace found!
                            // ==> Remove marketplace owner role
                            removeOwnerRole(marketplace.getOrganization());
                            removeUserRoles(marketplace.getOrganization()
                                    .getOrganizationId());
                        }
                    }
                }
            }

            if (!org.hasRole(OrganizationRoleType.MARKETPLACE_OWNER)) {
                try {
                    org = accountService.addOrganizationToRole(
                            org.getOrganizationId(),
                            OrganizationRoleType.MARKETPLACE_OWNER);
                } catch (IncompatibleRolesException e) {
                    SaaSSystemException sse = new SaaSSystemException(e);
                    logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                            LogMessageIdentifier.ERROR_UNEXPECTED_INCOMPATIBLE_ROLES_EXCEPTION);
                    throw sse;
                } catch (AddMarketingPermissionException e) {
                    // should not happen here
                    SaaSSystemException sse = new SaaSSystemException(e);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                            LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                            org.getOrganizationId());
                    throw sse;
                }
            }

            // Finally assign new owner
            marketplace.setOrganization(org);

            return true;

        }
        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeOwnerRole(Organization org) {
        // Get role marketplace owner
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(OrganizationRoleType.MARKETPLACE_OWNER);
        orgRole = (OrganizationRole) ds.find(orgRole);
        if (orgRole == null) {
            return;
        }
        // second retrieve relevant marketplaces by query
        Query query = ds.createNamedQuery(
                "OrganizationToRole.getByOrganizationAndRole");
        query.setParameter("orgTKey", new Long(org.getKey()));
        query.setParameter("orgRoleTKey", new Long(orgRole.getKey()));
        List<OrganizationToRole> tempList = ParameterizedTypes
                .list(query.getResultList(), OrganizationToRole.class);

        if (tempList.isEmpty()) {
            return;
        }
        // Remove relation
        OrganizationToRole orgToRole = tempList.get(0);
        ds.remove(orgToRole);
        ds.flush();
        org.getGrantedRoles().remove(orgToRole);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeUserRoles(String organizationId)
            throws ObjectNotFoundException {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        Organization org = (Organization) ds
                .getReferenceByBusinessKey(organization);
        for (PlatformUser pUser : org.getPlatformUsers()) {
            Set<RoleAssignment> roles = pUser.getAssignedRoles();
            if (roles != null && roles.size() > 0) {
                RoleAssignment roleToRemove = null;
                for (RoleAssignment userRole : roles) {
                    if (userRole.getRole().getRoleName()
                            .equals(UserRoleType.MARKETPLACE_OWNER)) {
                        roleToRemove = userRole;
                        break;
                    }
                }
                if (roleToRemove != null) {
                    pUser.getAssignedRoles().remove(roleToRemove);
                    ds.remove(roleToRemove);
                }
            }

        }
        ds.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public RevenueShareModel loadMarketplaceRevenueShare(String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace marketplace = getMarketplace(marketplaceId);
        return marketplace.getPriceModel();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public RevenueShareModel updateRevenueShare(
            RevenueShareModel revenueShareModelNew, int version)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException {

        RevenueShareModel revenueShareModel = ds.getReference(
                RevenueShareModel.class, revenueShareModelNew.getKey());

        BLValidator.isNotNull("revenueShare",
                revenueShareModelNew.getRevenueShare());
        String fieldName = FIELD_REVENUE_SHARE + " for "
                + revenueShareModel.getRevenueShareModelType();
        BLValidator.isInRange(fieldName, revenueShareModelNew.getRevenueShare(),
                RevenueShareModel.MIN_REVENUE_SHARE,
                RevenueShareModel.MAX_REVENUE_SHARE);
        VersionAndKeyValidator.verify(revenueShareModel, revenueShareModelNew,
                version);

        revenueShareModel
                .setRevenueShare(revenueShareModelNew.getRevenueShare());
        ds.flush();
        return revenueShareModel;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateMarketplaceName(Marketplace marketplace,
            String newMarketplaceName) throws OperationNotPermittedException {
        LocalizerFacade facade = new LocalizerFacade(localizer,
                ds.getCurrentUser().getLocale());
        String persistedMarketplaceName = facade.getText(marketplace.getKey(),
                LocalizedObjectTypes.MARKETPLACE_NAME);
        if (!persistedMarketplaceName.equals(newMarketplaceName)) {
            if (!ds.getCurrentUser().hasRole(UserRoleType.MARKETPLACE_OWNER)) {
                throw new OperationNotPermittedException();
            }
            checkMarketplaceOwner(marketplace.getMarketplaceId());
            localizer.storeLocalizedResource(ds.getCurrentUser().getLocale(),
                    marketplace.getKey(), LocalizedObjectTypes.MARKETPLACE_NAME,
                    newMarketplaceName);
        }
    }

    private void checkMarketplaceOwner(String marketplaceId)
            throws OperationNotPermittedException {
        if (marketplaceId != null) {
            Organization org = ds.getCurrentUser().getOrganization();
            for (Marketplace mp : org.getMarketplaces()) {
                if (marketplaceId.equals(mp.getMarketplaceId())) {
                    return;
                }
            }
            throw new OperationNotPermittedException();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendNotification(EmailType type, Marketplace marketplace,
            long organizationKey) {
        sendNotification(type, marketplace,
                accountService.getOrganizationAdmins(organizationKey));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendNotification(EmailType type, Marketplace domMp,
            List<PlatformUser> admins) {
        LocalizerFacade facade = new LocalizerFacade(localizer,
                ds.getCurrentUser().getLocale());
        String marketplaceName = facade.getText(domMp.getKey(),
                LocalizedObjectTypes.MARKETPLACE_NAME);

        // build URLs for mail
        String adminUrl = null;
        String publicAccessUrl = null;
        try {
            adminUrl = commService.getBaseUrl();
            adminUrl = addMarketplaceAdminPath(adminUrl);
            publicAccessUrl = commService
                    .getMarketplaceUrl(domMp.getMarketplaceId());
        } catch (MailOperationException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_RETRIEVE_MARKETPLACE_URL_FAILED,
                    type.toString(), domMp.getMarketplaceId());
        }

        for (PlatformUser admin : admins) {
            try {
                if ((EmailType.MARKETPLACE_SUPPLIER_ASSIGNED).equals(type)) {

                    sendMailToSupplierAssigned(type, domMp, marketplaceName,
                            adminUrl, publicAccessUrl, admin);
                } else {
                    commService.sendMail(admin, type, new Object[] {
                            marketplaceName, publicAccessUrl, adminUrl },
                            domMp);
                }

            } catch (MailOperationException e) {
                // mail delivery is not critical, log and proceed
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_NOTIFY_ASSIGNMENT_TO_MARKETPLACE_FAILED,
                        admin.getEmail());
            }
        }
    }

    private void sendMailToSupplierAssigned(EmailType type, Marketplace domMp,
            String marketplaceName, String adminUrl, String publicAccessUrl,
            PlatformUser admin) throws MailOperationException {
        boolean userIsMarketplaceOwner = isMarketplaceOwner(admin,
                domMp.getMarketplaceId());
        // If the user is the marketplace owner of given marketplace
        // then send an e-mail containing the admin url of the
        // marketplace.
        if (userIsMarketplaceOwner) {
            commService.sendMail(admin,
                    EmailType.MARKETPLACE_SUPPLIER_ASSIGNED_OWNED,
                    new Object[] { marketplaceName, publicAccessUrl, adminUrl },
                    domMp);
        }

        // Otherwise do not include the admin url in the e-mail.
        else {
            commService.sendMail(admin, type,
                    new Object[] { marketplaceName, publicAccessUrl }, domMp);
        }
    }

    private boolean isMarketplaceOwner(PlatformUser user,
            String marketplaceId) {
        boolean isMPOwner = false;

        boolean hasMPOwnerRole = user.getOrganization()
                .hasRole(OrganizationRoleType.MARKETPLACE_OWNER);

        if (hasMPOwnerRole) {
            List<Marketplace> marketplacesOwned = user.getOrganization()
                    .getMarketplaces();

            for (Marketplace marketplace : marketplacesOwned) {
                if (marketplace.getMarketplaceId().equals(marketplaceId)) {
                    isMPOwner = true;
                    break;
                }
            }
        }
        return isMPOwner;
    }

    private static String addMarketplaceAdminPath(String marketplaceAdminUrl) {
        if (marketplaceAdminUrl == null) {
            return null;
        }
        String prefix = marketplaceAdminUrl;
        String suffix = "";
        if (marketplaceAdminUrl.contains("?")) {
            prefix = marketplaceAdminUrl.substring(0,
                    marketplaceAdminUrl.lastIndexOf('?'));
            suffix = marketplaceAdminUrl
                    .substring(marketplaceAdminUrl.lastIndexOf('?'));
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + "shop/updateMarketplace.jsf" + suffix;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean updateMarketplace(Marketplace mp, String newMarketplaceName,
            String newOwningOrganizationId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, UserRoleAssignmentException {
        BLValidator.isNotBlank("marketplace.id", mp.getMarketplaceId());
        BLValidator.isNotBlank("marketplace.name", newMarketplaceName);

        if (newOwningOrganizationId == null) {
            // If no owner is given take that of the current user
            newOwningOrganizationId = ds.getCurrentUser().getOrganization()
                    .getOrganizationId();
        }

        if (isPublicLandingpage(mp)) {
            setLandingpageDefaultFillinCriterion(mp);
        }

        boolean ownerAssignmentUpdated = updateOwningOrganization(mp,
                newOwningOrganizationId, false);
        if (ownerAssignmentUpdated) {
            grantPublishingRights(mp);
        }
        ds.flush();
        if (ownerAssignmentUpdated) {
            List<PlatformUser> admins = accountService
                    .getOrganizationAdmins(mp.getOrganization().getKey());
            // Add MARKETPLACE_OWNER role to all administrators
            for (PlatformUser admin : admins) {
                identityService.grantUserRoles(admin, Collections
                        .singletonList(UserRoleType.MARKETPLACE_OWNER));
            }
        }
        updateMarketplaceName(mp, newMarketplaceName);
        marketplaceCache.resetConfiguration(mp.getMarketplaceId());
        return ownerAssignmentUpdated;
    }

    /**
     * @param mp
     * @return
     * @throws ObjectNotFoundException
     */
    protected boolean isPublicLandingpage(Marketplace mp)
            throws ObjectNotFoundException {
        return landingpageService.loadLandingpageType(
                mp.getMarketplaceId()) == LandingpageType.PUBLIC;
    }

    /**
     * If the rating is disabled on the marketplace (e.g. update) and the rating
     * is set as fillin criterion on the landing page, reset the fillin
     * criterion to the default value.
     * 
     * @param marketplace
     *            the Marketplace object
     */
    private void setLandingpageDefaultFillinCriterion(Marketplace marketplace) {
        if (!marketplace.isReviewEnabled()) {
            PublicLandingpage landingpage = marketplace.getPublicLandingpage();
            if (landingpage
                    .getFillinCriterion() == FillinCriterion.RATING_DESCENDING) {
                landingpage.setFillinCriterion(
                        PublicLandingpage.DEFAULT_FILLINCRITERION);
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void grantPublishingRights(Marketplace mp) {
        Organization org = mp.getOrganization();
        if (org != null && (org.hasRole(OrganizationRoleType.SUPPLIER)
                || org.hasRole(OrganizationRoleType.RESELLER)
                || org.hasRole(OrganizationRoleType.BROKER))) {
            MarketplaceToOrganization rel = new MarketplaceToOrganization(mp,
                    org);
            try {
                ds.persist(rel);
            } catch (NonUniqueBusinessKeyException e) {
                // ignore - supplier is already allowed to publish on the
                // marketplace;
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Marketplace getMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace mp = (Marketplace) ds
                .getReferenceByBusinessKey(new Marketplace(marketplaceId));
        Invariants.assertNotNull(mp.getPriceModel(),
                "marketplace price model must not be null.");
        Invariants.assertNotNull(mp.getBrokerPriceModel(),
                "broker price model must not be null.");
        Invariants.assertNotNull(mp.getResellerPriceModel(),
                "reseller price model must not be null.");
        return mp;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean updateMarketplace(Marketplace marketplace,
            Marketplace newMarketplace, String marketplaceName,
            String owningOrganizationId, int marketplaceRevenueShareVersion,
            int resellerRevenueShareVersion, int brokerRevenueShareVersion)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException,
            UserRoleAssignmentException {
        try {
            boolean ownerAssignmentUpdated = updateMarketplace(marketplace,
                    marketplaceName, owningOrganizationId);
            if (ds.getCurrentUser().hasRole(UserRoleType.PLATFORM_OPERATOR)) {
                updateRevenueShare(newMarketplace.getPriceModel(),
                        marketplaceRevenueShareVersion);
                updateRevenueShare(newMarketplace.getResellerPriceModel(),
                        resellerRevenueShareVersion);
                updateRevenueShare(newMarketplace.getBrokerPriceModel(),
                        brokerRevenueShareVersion);
            }
            marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
            return ownerAssignmentUpdated;

        } catch (ValidationException | UserRoleAssignmentException
                | ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateMarketplaceTrackingCode(String marketplaceId,
            int marketplaceVersion, String trackingCode)
            throws ObjectNotFoundException, ConcurrentModificationException {
        Marketplace dbMarketplace = getMarketplace(marketplaceId);
        VersionAndKeyValidator.verify(dbMarketplace, dbMarketplace,
                marketplaceVersion);
        dbMarketplace.setTrackingCode(trackingCode);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String getTrackingCodeFromMarketplace(String marketplaceID)
            throws ObjectNotFoundException {
        Marketplace dbMarketplace = getMarketplace(marketplaceID);
        return dbMarketplace.getTrackingCode();
    }

    /**
     * Grants several resale permissions
     * 
     * @param permissionsToGrant
     *            A list of resale permissions, which should be granted. Each
     *            resale permission contains the related service template, the
     *            organization, which grants the resale permission, the
     *            organization, which receives the resale permission, and the
     *            type of the resale permission.
     * @throws ValidationException
     *             if a resale permission is invalid
     * @throws ObjectNotFoundException
     *             if a service template or an organization is not found
     * @throws OperationNotPermittedException
     *             if a service template doesn't belong to the corresponding
     *             grantor
     * @throws NonUniqueBusinessKeyException
     *             if a service copy or a new catalog entry cannot be created,
     *             because an entity with the same key already exists in the
     *             database
     * @throws ServiceOperationException
     *             if a service template is no template or if a service template
     *             has no valid price model defined for itself or if a service
     *             template is not assigned to a marketplace
     * @throws ConcurrentModificationException
     *             if the same resale permission is granted by another user at
     *             the same time
     * @throws OrganizationAuthorityException
     *             if a grantee organization has neither the BROKER nor the
     *             RESELLER role
     * @throws ServiceStateException
     *             if a service template is not in status INACTIVE or ACTIVE
     */
    void grantResalePermissions(
            List<POResalePermissionDetails> permissionsToGrant)
            throws ValidationException, ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException,
            ServiceOperationException, ConcurrentModificationException,
            OrganizationAuthorityException, ServiceStateException {

        ArgumentValidator.notNull("permissionsToGrant", permissionsToGrant);

        for (POResalePermissionDetails resalePerm : permissionsToGrant) {
            ArgumentValidator.notNull("service", resalePerm.getService());
            ArgumentValidator.notNull("grantor", resalePerm.getGrantor());
            ArgumentValidator.notNull("grantee", resalePerm.getGrantee());

            String templateId = resalePerm.getService().getServiceId();
            String grantorId = resalePerm.getGrantor().getOrganizationId();
            String granteeId = resalePerm.getGrantee().getOrganizationId();
            OfferingType resaleType = resalePerm.getOfferingType();
            partnerSrvProv.grantResalePermission(templateId, grantorId,
                    granteeId, resaleType);
        }

    }

    /**
     * Revokes several resale permissions
     * 
     * @param permissionsToRevoke
     *            A list of resale permissions, which should be revoked. Each
     *            resale permission contains the related service template, the
     *            organization, which grants the resale permission and the
     *            organization, which receives the resale permission.
     * @throws ObjectNotFoundException
     *             if a service template or an organization is not found
     * @throws ServiceOperationException
     *             if a service template is no template
     * @throws OrganizationAuthorityException
     *             if a grantee organization has neither the BROKER nor the
     *             RESELLER role
     */
    void revokeResalePermissions(
            List<POResalePermissionDetails> permissionsToRevoke)
            throws ObjectNotFoundException, ServiceOperationException,
            OrganizationAuthorityException, OperationNotPermittedException {

        ArgumentValidator.notNull("permissionsToRevoke", permissionsToRevoke);

        for (POResalePermissionDetails resalePerm : permissionsToRevoke) {
            ArgumentValidator.notNull("service", resalePerm.getService());
            ArgumentValidator.notNull("grantor", resalePerm.getGrantor());
            ArgumentValidator.notNull("grantee", resalePerm.getGrantee());

            String templateId = resalePerm.getService().getServiceId();
            String grantorId = resalePerm.getGrantor().getOrganizationId();
            String granteeId = resalePerm.getGrantee().getOrganizationId();

            partnerSrvProv.revokeResalePermission(templateId, grantorId,
                    granteeId);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Product publishService(long serviceKey, CatalogEntry ceNew,
            List<VOCategory> categories)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException {
        Marketplace ceNewMarketplace = ceNew.getMarketplace();
        if (ceNewMarketplace != null) {
            BLValidator.isNotNull("voCatalogEntry.marketplaceId",
                    ceNewMarketplace.getMarketplaceId());
        }

        // check for correct role
        Organization supplier = ds.getCurrentUser().getOrganization();

        // first find existing service/product
        Product product = loadProductAndVerifyOwner(serviceKey, supplier);

        CatalogEntry catalogEntry = retrieveCatalogEntry(product);

        boolean isServicePublicChanged = false;
        if (catalogEntry.isAnonymousVisible() != ceNew.isAnonymousVisible()) {
            isServicePublicChanged = true;
        }
        setCatalogEntryVisibility(ceNew, catalogEntry);

        // check if MP of given service hasn't changed and skip publication in
        // that case
        Marketplace productMarketplace = catalogEntry.getMarketplace();
        boolean marketPlaceUpdated = false;
        if (shouldMkpBeUpdated(ceNewMarketplace, productMarketplace)) {

            if (productMarketplace != null) {
                landingpageService.removeProductFromLandingpage(
                        productMarketplace, product);
            }

            // if marketplace changes all categories assigned to 'old'
            // marketplace must be removed
            categorizationService.deassignAllCategories(catalogEntry);

            // finally publish given service to new MP by reassigning new MP
            // to existing catalog entry, and partner price model copied from
            // previous marketplace is removed
            if (!catalogEntry.getProduct().isCopy()) {
                removePartnerPriceModelsForCatalogEntry(catalogEntry);
            }

            Marketplace mp = retrieveMarketplaceForGivenId(ceNewMarketplace,
                    supplier);
            catalogEntry.setMarketplace(mp);
            clearUpgradePath(product);
            marketPlaceUpdated = true;
        }

        logActivities(categories, product, isServicePublicChanged, catalogEntry,
                marketPlaceUpdated);

        return product;
    }

    private boolean shouldMkpBeUpdated(Marketplace ceNewMarketplace,
            Marketplace productMarketplace) {
        return isNewMkpNullAndOldNotNull(ceNewMarketplace, productMarketplace)
                || isOldNullAndNewNotNull(ceNewMarketplace, productMarketplace)
                || bothNotNullAndDifferent(ceNewMarketplace,
                        productMarketplace);
    }

    private Marketplace retrieveMarketplaceForGivenId(
            Marketplace ceNewMarketplace, Organization supplier)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Marketplace mp = null;
        if (ceNewMarketplace != null) {
            // retrieve MP object for succeeding check
            mp = new Marketplace(ceNewMarketplace.getMarketplaceId());
            mp = (Marketplace) ds.getReferenceByBusinessKey(mp);

            // check if supplier is permitted to publish on the MP
            PermissionCheck.canPublish(mp, supplier, logger, sessionCtx);

            // create reference for the open marketplaces
            if (mp.isOpen()) {
                addMarketplaceToOrganization(mp, supplier);
            }
        }
        return mp;
    }

    private void setCatalogEntryVisibility(CatalogEntry ceNew,
            CatalogEntry catalogEntry) {
        // is the catalog entry is visible to anonymous users?
        catalogEntry.setAnonymousVisible(ceNew.isAnonymousVisible());

        // is the catalog entry is visible in the service catalog?
        catalogEntry.setVisibleInCatalog(ceNew.isVisibleInCatalog());
    }

    private CatalogEntry retrieveCatalogEntry(Product product)
            throws NonUniqueBusinessKeyException {
        // get existing catalog entry for service
        CatalogEntry catalogEntry = null;
        if (product.getCatalogEntries().size() > 0) {
            catalogEntry = product.getCatalogEntries().get(0);
        }
        if (catalogEntry == null) {
            catalogEntry = QueryBasedObjectFactory.createCatalogEntry(product,
                    null);
            ds.persist(catalogEntry);
            ds.flush();
        }
        return catalogEntry;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Product publishServiceWithPermissions(long serviceKey,
            CatalogEntry catalogEntry, List<VOCategory> categories,
            List<POResalePermissionDetails> permissionsToGrant,
            List<POResalePermissionDetails> permissionsToRevoke)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthorityException, ConcurrentModificationException,
            ServiceStateException, ServiceOperationException {
        Product retVal = publishService(serviceKey, catalogEntry, categories);
        if (ds.getCurrentUser().hasRole(UserRoleType.SERVICE_MANAGER)) {
            grantResalePermissions(permissionsToGrant);
            revokeResalePermissions(permissionsToRevoke);
        }
        return retVal;
    }

    private boolean isOldNullAndNewNotNull(Marketplace ceNewMarketplace,
            Marketplace productMarketplace) {
        return productMarketplace == null && ceNewMarketplace != null;
    }

    private boolean bothNotNullAndDifferent(Marketplace ceNewMarketplace,
            Marketplace productMarketplace) {
        boolean retVal = false;
        if (ceNewMarketplace != null && productMarketplace != null) {
            retVal = !ceNewMarketplace.getMarketplaceId()
                    .equals(productMarketplace.getMarketplaceId());
        }
        return retVal;
    }

    private boolean isNewMkpNullAndOldNotNull(Marketplace ceNewMarketplace,
            Marketplace productMarketplace) {
        return ceNewMarketplace == null && productMarketplace != null;
    }

    private void logActivities(List<VOCategory> categories, Product product,
            boolean isServicePublicChanged, CatalogEntry catalogEntry,
            boolean marketPlaceUpdated) throws ObjectNotFoundException {
        Marketplace marketplace = catalogEntry.getMarketplace();
        if (marketplace != null) {
            boolean isCatagoriesChanged = categorizationService
                    .updateAssignedCategories(catalogEntry, categories);

            if (isServicePublicChanged) {
                audit.setServiceAsPublic(ds, product,
                        catalogEntry.isAnonymousVisible());
            }

            if (marketPlaceUpdated) {
                LocalizerFacade facade = new LocalizerFacade(localizer,
                        ds.getCurrentUser().getLocale());
                audit.assignToMarketPlace(ds, product,
                        marketplace.getMarketplaceId(),
                        facade.getText(marketplace.getKey(),
                                LocalizedObjectTypes.MARKETPLACE_NAME));
            }
            if (isCatagoriesChanged) {
                audit.assignCategories(ds, product, categories);
            }
        }
    }

    void addMarketplaceToOrganization(Marketplace mp, Organization supplier) {
        MarketplaceToOrganization mto = new MarketplaceToOrganization(mp,
                supplier);
        if (ds.find(mto) == null) {
            try {
                ds.persist(mto);
            } catch (NonUniqueBusinessKeyException e) {
                // implicit reference creation, reference exists already
            }
        }
    }

    Product loadProductAndVerifyOwner(long serviceKey, Organization supplier)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Product prod = ds.getReference(Product.class, serviceKey);

        // make sure the calling supplier actually owns the service
        PermissionCheck.owns(prod, supplier, logger, null);
        return prod;
    }

    private void removePartnerPriceModelsForCatalogEntry(
            CatalogEntry catalogEntry) {
        RevenueShareModel brokerRevenueShare = catalogEntry
                .getBrokerPriceModel();
        RevenueShareModel resellerRevenueShare = catalogEntry
                .getResellerPriceModel();
        catalogEntry.setBrokerPriceModel(null);
        catalogEntry.setResellerPriceModel(null);
        if (brokerRevenueShare != null) {
            ds.remove(brokerRevenueShare);
        }
        if (resellerRevenueShare != null) {
            ds.remove(resellerRevenueShare);
        }
    }

    /**
     * Removes all incoming and outgoing product references for this product.
     */
    private void clearUpgradePath(Product product) {
        if (product != null) {
            for (ProductReference pr : product.getAllCompatibleProducts()) {
                ds.remove(pr);
            }
            product.getAllCompatibleProducts().clear();
            ArrayList<ProductReference> references = new ArrayList<>(
                    product.getCompatibleProductsTarget());
            for (ProductReference pr : references) {
                pr.getSourceProduct().getAllCompatibleProducts().remove(pr);
                ds.remove(pr);
            }
            product.getCompatibleProductsTarget().clear();
        }
    }

    public ServiceProvisioningPartnerServiceLocal getPartnerSrvProv() {
        return partnerSrvProv;
    }

    public void setPartnerSrvProv(
            ServiceProvisioningPartnerServiceLocal partnerSrvProv) {
        this.partnerSrvProv = partnerSrvProv;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Marketplace getMarketplaceForId(String marketplaceId)
            throws ObjectNotFoundException {
        return (Marketplace) ds
                .getReferenceByBusinessKey(new Marketplace(marketplaceId));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Organization> getAllOrganizations() {
        Query query = ds.createNamedQuery("Organization.getAllOrganizations");
        return ParameterizedTypes.list(query.getResultList(),
                Organization.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Marketplace updateMarketplaceAccessType(String marketplaceId,
            boolean isRestricted)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        Marketplace marketplace = (Marketplace) ds
                .getReferenceByBusinessKey(new Marketplace(marketplaceId));
        if (marketplace.isRestricted() == isRestricted) {
            return marketplace;
        }
        marketplace.setRestricted(isRestricted);
        ds.persist(marketplace);
        marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
        return marketplace;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void grantAccessToMarketPlaceToOrganization(Marketplace marketplace,
            Organization organization) throws NonUniqueBusinessKeyException {

        MarketplaceAccess marketplaceAccess = new MarketplaceAccess();
        marketplaceAccess.setMarketplace(marketplace);
        marketplaceAccess.setOrganization(organization);

        try {
            ds.getReferenceByBusinessKey(marketplaceAccess);
        } catch (ObjectNotFoundException e) {
            ds.persist(marketplaceAccess);
        }

        marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeMarketplaceAccesses(long marketplaceKey) {
        marketplaceAccessDao.removeAccessForMarketplace(marketplaceKey);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeMarketplaceAccess(long marketplaceKey,
            long organizationKey) throws ObjectNotFoundException {
        MarketplaceAccess marketplaceAccess = new MarketplaceAccess();
        marketplaceAccess.setMarketplace_tkey(marketplaceKey);
        marketplaceAccess.setOrganization_tkey(organizationKey);

        try {
            MarketplaceAccess mpa = (MarketplaceAccess) ds
                    .getReferenceByBusinessKey(marketplaceAccess);
            ds.remove(mpa);
        } catch (ObjectNotFoundException e) {
            // if no entry exists just continue
        }

        Organization org = ds.getReference(Organization.class, organizationKey);
        Marketplace mp = ds.getReference(Marketplace.class, marketplaceKey);

        CatalogEntry ce;
        for (Product prod : org.getProducts()) {
            ce = prod.getCatalogEntryForMarketplace(mp);
            if (ce != null) {
                ce.setMarketplace(null);
            }
        }
    }

    @Override
    public List<Marketplace> getMarketplacesForOrganizationWithRestrictedAccess(
            long orgKey) {

        String selectQuery = "SELECT m.* FROM marketplace m JOIN marketplaceaccess ma ON m.tkey = ma.marketplace_tkey WHERE m.restricted = 'true' AND ma.organization_tkey = :orgKey";

        Query query = ds.createNativeQuery(selectQuery, Marketplace.class);
        query.setParameter("orgKey", orgKey);

        List<Marketplace> marketplaces = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        return marketplaces;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean doesAccessToMarketplaceExistForOrganization(
            long marketplaceKey, long organizationKey) {
        MarketplaceAccess marketplaceAccess = new MarketplaceAccess();
        marketplaceAccess.setMarketplace_tkey(marketplaceKey);
        marketplaceAccess.setOrganization_tkey(organizationKey);
        try {
            ds.getReferenceByBusinessKey(marketplaceAccess);
        } catch (ObjectNotFoundException e) {
            return false;
        }
        return marketplaceAccess != null;
    }

    @Override
    public List<Object[]> getOrganizationsWithMarketplaceAccess(
            String marketplaceId) throws ObjectNotFoundException {

        Marketplace marketplace = (Marketplace) ds
                .getReferenceByBusinessKey(new Marketplace(marketplaceId));

        long tenantKey = 0;
        if (marketplace.getTenant() != null) {
            tenantKey = marketplace.getTenant().getKey();
        }

        return marketplaceAccessDao.getOrganizationsWithMplAndSubscriptions(
                marketplace.getKey(), tenantKey);
    }

    @Override
    public List<Organization> getAllOrganizationsWithAccessToMarketplace(
            long marketplaceKey) throws ObjectNotFoundException {

        Marketplace marketplace = ds.getReference(Marketplace.class,
                marketplaceKey);

        long tenantKey = 0;
        if (marketplace.getTenant() != null) {
            tenantKey = marketplace.getTenant().getKey();
        }

        return marketplaceAccessDao.getAllOrganizationsWithAccessToMarketplace(
                marketplaceKey, tenantKey);
    }

    @Override
    public void updateTenant(Marketplace marketplace, String tenantId)
            throws ObjectNotFoundException {

        if (StringUtils.isBlank(tenantId)) {
            marketplace.setTenant(null);
            return;
        }

        String currentTenantId = (marketplace.getTenant() != null)
                ? marketplace.getTenant().getTenantId() : null;

        if (currentTenantId == null || !currentTenantId.equals(tenantId)) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(tenantId);
            tenant = (Tenant) ds.getReferenceByBusinessKey(tenant);
            marketplace.setTenant(tenant);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Marketplace> getAllMarketplacesForTenant(Long tenantKey)
            throws ObjectNotFoundException {

        Query query;
        if (tenantKey != null && tenantKey.intValue() != 0) {
            Tenant tenant = ds.getReference(Tenant.class, tenantKey);
            query = ds.createNamedQuery("Marketplace.getAllForTenant");
            query.setParameter("tenant", tenant);
        } else {
            query = ds.createNamedQuery("Marketplace.getAllForDefaultTenant");
        }
        List<Marketplace> marketplaceList = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        return marketplaceList;
    }
}
