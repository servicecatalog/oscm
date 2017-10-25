/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 06.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.marketplace;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PricingServiceBean;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.validation.ArgumentValidator;

/**
 * @author barzu
 */
@Stateless
@Remote(MarketplaceServiceManagePartner.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class MarketplaceServiceManagePartnerBean implements
        MarketplaceServiceManagePartner {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    MarketplaceServiceLocal mpServiceLocal;

    @Override
    @RolesAllowed({ "MARKETPLACE_OWNER", "PLATFORM_OPERATOR" })
    public Response updateMarketplace(VOMarketplace marketplace,
            POMarketplacePriceModel marketplacePriceModel,
            POPartnerPriceModel partnerPriceModel)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException,
            AddMarketingPermissionException, UserRoleAssignmentException {

        Response response = new Response();
        try {
            ArgumentValidator.notNull("marketplace", marketplace);
            ArgumentValidator.notNull("marketplacePriceModel",
                    marketplacePriceModel);
            ArgumentValidator.notNull("partnerPriceModel", partnerPriceModel);

            Marketplace mp = mpServiceLocal.getMarketplace(marketplace
                    .getMarketplaceId());
            MarketplaceAssembler.updateMarketplace(mp, marketplace);

            final Marketplace newMarketplace = new Marketplace();
            newMarketplace.setPriceModel(PricingServiceBean
                    .toRevenueShareModel(marketplacePriceModel
                            .getRevenueShare()));
            newMarketplace.setResellerPriceModel(PricingServiceBean
                    .toRevenueShareModel(partnerPriceModel
                            .getRevenueShareResellerModel()));
            newMarketplace.setBrokerPriceModel(PricingServiceBean
                    .toRevenueShareModel(partnerPriceModel
                            .getRevenueShareBrokerModel()));
            
            mpServiceLocal.updateTenant(mp, marketplace.getTenantId());
            
            boolean ownerAssignmentUpdated = mpServiceLocal
                    .updateMarketplace(mp, newMarketplace, marketplace
                            .getName(), marketplace.getOwningOrganizationId(),
                            marketplacePriceModel.getRevenueShare()
                                    .getVersion(), partnerPriceModel
                                    .getRevenueShareResellerModel()
                                    .getVersion(), partnerPriceModel
                                    .getRevenueShareBrokerModel().getVersion());
            
            
            // build the response
            LocalizerFacade facade = new LocalizerFacade(localizer, dm
                    .getCurrentUser().getLocale());
            response.getResults().add(
                    MarketplaceAssembler.toVOMarketplace(mp, facade));
            response.getResults().add(
                    PricingServiceBean.toPOMarketplacePriceModel(mp
                            .getPriceModel()));
            response.getResults().add(
                    PricingServiceBean.toPOPartnerPriceModel(mp));

            // Send email to all admins of the organization about new assignment
            if (ownerAssignmentUpdated) {
                mpServiceLocal.sendNotification(
                        EmailType.MARKETPLACE_OWNER_ASSIGNED, mp, mp
                                .getOrganization().getKey());
            }

            return response;
        } finally {

        }
    }

}
