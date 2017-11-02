/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.serviceDetails;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.partnerservice.POVendorAddress;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.review.POServiceReview;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.BaseBean.Vo2ModelMapper;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelDisplayHandler;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceReview;

/**
 * this controller handles display of a service details data in the marketplace
 * 
 */

public class ServiceDetailsCtrl {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ServiceDetailsCtrl.class);
    private static final String ERROR_SERVICE_INVALID_KEY = "error.service.invalidKey";
    private static final String OUTCOME_SHOW_SERVICE_LIST = "showServiceList";

    // injected, see faces-config.xml
    ServiceDetailsModel model;

    UiDelegate ui = new UiDelegate();

    VOUserDetails currentUser;

    Service selectedService;

    UserGroupService userGroupService;

    public static final String ERROR_SERVICE_NOT_AVAILABLE_ANYMORE = "error.service.notAvailableAnymore";

    public String getInitialize() throws IllegalArgumentException, IOException,
            ObjectNotFoundException {

        selectService();
        if (model.getSelectedService() != null) {

            markReviewOwnership();

            model.setServiceEvents(SteppedPriceHandler
                    .buildPricedEvents(model.getSelectedService()
                            .getPriceModel().getConsideredEvents()));

            model.setServiceParameters(PricedParameterRow
                    .createPricedParameterRowListForService(model.getService()
                            .getVO()));
            
            PriceModelContent selectedPriceModelContent = new PriceModelContent(
                    model.getSelectedService().getPriceModel().getVo().getPresentationDataType(),
                    model.getSelectedService().getPriceModel().getVo().getPresentation());

            model.setPriceModelContent(selectedPriceModelContent);
        }
        return "";
    }

    /**
     * Gets a list of wrapped reviews of the selected service.
     * 
     * @return the reviews list.
     * @throws ObjectNotFoundException
     */
    void markReviewOwnership() {
        List<ServiceReview> reviews = new ArrayList<ServiceReview>();
        if (model.getSelectedServiceFeedback() != null) {
            for (POServiceReview poReview : model.getSelectedServiceFeedback()
                    .getReviews()) {
                ServiceReview review = new ServiceReview(poReview);
                review.setBelongsToLoggedInUser(ui
                        .getUserFromSessionWithoutException() != null
                        && ui.getUserFromSessionWithoutException().getUserId()
                                .equals(poReview.getUserId()));
                reviews.add(review);
            }
        }
        model.setSelectedServiceReviews(reviews);
    }

    public boolean isHasReviewForLoggedInUser() {
        if (model.getSelectedServiceReviews() == null
                || model.getSelectedServiceReviews().isEmpty())
            return false;
        for (ServiceReview review : model.getSelectedServiceReviews()) {
            if (review.isBelongsToLoggedInUser()) {
                return true;
            }
        }
        return false;
    }

    public static final Vo2ModelMapper<VOService, Service> DEFAULT_VOSERVICE_MAPPER = new Vo2ModelMapper<VOService, Service>() {
        @Override
        public Service createModel(final VOService vo) {
            return new Service(vo);
        }
    };

    public String redirectToServiceDetails() {
        logger.logDebug("entering redirectToServiceDetails");
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        String viewId = Marketplace.MARKETPLACE_ROOT + "/serviceDetails.jsf";

        try {
            viewId = extContext.getRequestContextPath()
                    + viewId
                    + ui.getSelectedServiceKeyQueryPart(String.valueOf(model
                            .getSelectedServiceKey()))
                    + ui.getMarketplaceIdQueryPart();
            String urlLink = context.getExternalContext().encodeActionURL(
                    viewId);
            JSFUtils.redirect(extContext, urlLink);
        } catch (IOException e) {
            extContext.log(
                    getClass().getName() + ".redirectToServiceDetails()", e);
        } finally {
            // reset requested key;
            model.setSelectedServiceKey(null);
        }
        return null;
    }

    /**
     * Indirectly redirects to the service list in case the service details page
     * was accessed with an invalid service key. The actual redirection will be
     * executed be a navigation rule.
     * 
     * @return the corresponding outcome to so the navigation rule redirects to
     *         the service list page.
     */
    public String redirectToServiceList() {
        model.setSelectedServiceKey(null);
        String errorKey = (String) ui.getRequest().getAttribute(
                Constants.REQ_ATTR_ERROR_KEY);
        if (errorKey == null) {
            logger.logDebug("patching errorKey...");
            // The only way that the errorKey is not set at this point is that
            // it was not possible to parse the passed key => add the
            // corresponding error message
            ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    ERROR_SERVICE_INVALID_KEY);
        }
        return OUTCOME_SHOW_SERVICE_LIST;
    }

    /**
     * The selectedServiceForDetails is empty if the passed serviceKey is not
     * set, invalid or the service is not active see former <c:if
     * test="#{serviceDetailsCtrl.serviceNotAvailable}">
     **/
    public boolean getServiceNotAvailable() {
        logger.logDebug("ServiceNotAvailable");
        if (model.getSelectedService() == null)
            return true;
        else
            return false;
    }

    public void selectService() throws IllegalArgumentException, IOException,
            ObjectNotFoundException {

        Long key = null;
        if (model.selectedServiceKey != null) {
            key = Long.valueOf(model.selectedServiceKey);
        } else {
            key = determineServiceKeyFromRequestParam();
        }
        initInvisibleProductKeysForUser();
        if (key != null && key.longValue() > 0) {
            getServiceFromServer(key.longValue());
            if (model.getSelectedService() == null) {
                setErrorAttribute(BaseBean.ERROR_SERVICE_NOT_AVAILABLE);
            } else
                storeServiceKeyInSessionBean(model.getSelectedService()
                        .getKey());
        }
    }

    /**
     * store the service key in session bean to be able to find the service
     * after a login
     * 
     * @param key
     */
    private void storeServiceKeyInSessionBean(long key) {
        ui.findSessionBean().setSelectedServiceKeyForCustomer(key);
    }

    /**
     * get the key for the service to load from the request parameter if it is
     * invalid register an error message and return null;
     */
    private Long determineServiceKeyFromRequestParam() {
        Map<String, String> params = ui.getExternalContext()
                .getRequestParameterMap();
        String keyFromUrlParam = params
                .get(Constants.REQ_PARAM_SELECTED_SERVICE_KEY);

        if (!ADMStringUtils.isBlank(keyFromUrlParam)) {
            try {
                return Long.valueOf(keyFromUrlParam);
            } catch (NumberFormatException e) {
                setErrorAttribute(BaseBean.ERROR_SERVICE_INVALID_KEY);
                return null;
            }
        } else
            return null;
    }

    /**
     * Sets the passed message key as error attribute in the current request.
     * 
     * @param errorMsgKey
     *            the error message key the be set.
     */
    private void setErrorAttribute(String errorMsgKey) {
        ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY, errorMsgKey);
    }

    /**
     * Load service from server: copied from SubscriptionDetailsCtrl.
     * 
     */
    void getServiceFromServer(long serviceKey) throws IllegalArgumentException,
            IOException {

        try {
            if (!isServiceAccessible(serviceKey)) {
                redirectToAccessDeniedPage();
                return;
            }
            PartnerService partnerService = ui
                    .findService(PartnerService.class);

            String mpl = ui.getMarketplaceId();

            Response response = partnerService
                    .getAllServiceDetailsForMarketplace(serviceKey, ui
                            .getViewLocale().getLanguage(), mpl);

            if (response.getReturnCodes().isEmpty()) {

                VOServiceEntry svc = response.getResult(VOServiceEntry.class);
                model.setSelectedService(new Service(svc));

                POServiceFeedback feedback = response
                        .getResult(POServiceFeedback.class);
                model.setSelectedServiceFeedback(feedback);

                VODiscount discount = response.getResult(VODiscount.class);
                if (discount != null) {
                    model.setDiscount(new Discount(discount));
                }

                List<VOService> relatedServicesForMarketplace = response
                        .getResultList(VOService.class);
                List<Service> relatedServices = DEFAULT_VOSERVICE_MAPPER
                        .map(relatedServicesForMarketplace);
                relatedServices.add(0, model.getSelectedService());
                relatedServices = removeInvisibleServices(relatedServices);
                model.setRelatedServices(relatedServices);

                @SuppressWarnings("unchecked")
                Map<String, POVendorAddress> addressMap = response
                        .getResult(HashMap.class);
                if (addressMap != null) {

                    if (addressMap.get(POVendorAddress.SERVICE_SELLER_SUPPLIER) != null) {
                        model.setServiceSupplier(addressMap
                                .get(POVendorAddress.SERVICE_SELLER_SUPPLIER));

                    }
                    if (addressMap.get(POVendorAddress.SERVICE_SERVICE_PARTNER) != null) {
                        model.setServicePartner(addressMap
                                .get(POVendorAddress.SERVICE_SERVICE_PARTNER));
                    }
                }
                generateWarning();
            }

        } catch (ObjectNotFoundException e) {
            System.out.println("ObjectNotFound");
            ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SERVICE_NOT_AVAILABLE);
        } catch (OperationNotPermittedException e) {
            System.out.println("OperationNotPermittedException");
            if (ui.isLoggedIn()) {
                System.out.println("loggedIn"
                        + BaseBean.ERROR_SERVICE_NOT_AVAILABLE_LOGGED_IN);
                ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_SERVICE_NOT_AVAILABLE_LOGGED_IN);
            } else {
                System.out.println("NOT loggedIn"
                        + BaseBean.ERROR_SERVICE_NOT_AVAILABLE);
                ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_SERVICE_NOT_AVAILABLE);
            }

        }

    }

    /**
     * Get the selected service object for showing the service details. When the
     * service is not subscribable, a warning is generated.
     * 
     * @return the selected service object
     */
    public void generateWarning() {
        logger.logDebug("generateWarning");

        if (!ui.isLoggedIn()){
            return;
        }

        if (!checkIfUserCanSubscribeToService()) {
            ui.getRequest().setAttribute(Constants.REQ_ATTR_WARNING_KEY,
                    BaseBean.WARNING_SUBSCRIBE_ONLY_BY_ADMIN);
        }

        if (model.getSelectedService() != null
                && !model.getSelectedService().isSubscribable()) {
            ui.getRequest().setAttribute(Constants.REQ_ATTR_WARNING_KEY,
                    BaseBean.WARNING_SUBSCRIBE_ONLY_ONCE);
        }
    }

    private boolean checkIfUserCanSubscribeToService() {
        VOUserDetails userDetails = ui.getUserFromSessionWithoutException();
        if (!ui.isLoggedIn()) {
            return false;
        }
        if (userDetails.hasAdminRole()) {
            return true;
        }
        if (userDetails.getUserRoles().contains(UserRoleType.SUBSCRIPTION_MANAGER)) {
            return true;
        }
        if (userDetails.getUserRoles().contains(UserRoleType.UNIT_ADMINISTRATOR)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the base URL of a service with new parameter
     * <code>serviceid</code>.
     * 
     * @return the base URL with parameter <code>serviceid</code>
     */
    public String getServiceUrl() {
        Service selectedService = model.getSelectedService();
        if (selectedService == null || selectedService.getBaseURL() == null
                || !selectedService.isExternal()) {
            return "";
        }

        try {
            String serviceUrl = ADMStringUtils
                    .removeEndingSlash(selectedService.getBaseURL().trim());

            StringBuffer result = new StringBuffer(serviceUrl);
            int pos = result.indexOf("?");
            if (pos > -1) {
                if (serviceUrl.length() - 1 > pos) {
                    result.append("&");
                }
            } else {
                result.append("?");
            }

            result.append("serviceid="
                    + URLEncoder.encode(selectedService.getServiceId(), "UTF-8"));
            return result.toString();

        } catch (UnsupportedEncodingException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_UNSUPPORTED_ENCODING);
            return selectedService.getBaseURL();
        }
    }

    public ServiceDetailsModel getModel() {
        return model;
    }

    public void setModel(ServiceDetailsModel model) {
        this.model = model;
    }

    boolean isServiceAccessible(long serviceKey) {
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        if (null == voUserDetails || voUserDetails.hasAdminRole()) {
            return true;
        }
        if (null != model.getInvisibleProductKeys()
                && model.getInvisibleProductKeys().contains(
                        Long.valueOf(serviceKey))) {
            return false;
        }
        return true;
    }

    private UserGroupService getUserGroupService() {
        if (userGroupService == null) {
            userGroupService = ui.findService(UserGroupService.class);
        }
        return userGroupService;
    }

    void redirectToAccessDeniedPage() throws IllegalArgumentException,
            IOException {
        HttpServletRequest request = JSFUtils.getRequest();
        HttpServletResponse response = JSFUtils.getResponse();
        String relativePath = BaseBean.MARKETPLACE_ACCESS_DENY_PAGE;

        JSFUtils.sendRedirect(response, request.getContextPath() + relativePath);
    }

    List<Service> removeInvisibleServices(List<Service> services) {
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        if (null == voUserDetails || voUserDetails.hasAdminRole()) {
            return services;
        }

        if (services == null || services.isEmpty()) {
            return services;
        }

        Set<Long> invisibleProductKeySet = new HashSet<Long>();
        invisibleProductKeySet.addAll(model.getInvisibleProductKeys());

        List<Service> visibleServices = new ArrayList<Service>();

        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            if (!invisibleProductKeySet
                    .contains(Long.valueOf(service.getKey()))) {
                visibleServices.add(service);
            }
        }
        return visibleServices;
    }

    private void initInvisibleProductKeysForUser()
            throws ObjectNotFoundException {
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        if (null == voUserDetails || voUserDetails.hasAdminRole()) {
            model.setInvisibleProductKeys(null);
        } else {
            List<Long> invisibleProductKeys = getUserGroupService()
                    .getInvisibleProductKeysForUser(voUserDetails.getKey());
            model.setInvisibleProductKeys(invisibleProductKeys);
        }
    }

    /**
     * Method is used in UI to show external price model details.
     *
     * @throws IOException
     */
    public void display() throws IOException {
        ExternalPriceModelDisplayHandler displayHandler = new ExternalPriceModelDisplayHandler();
        displayHandler.setContent(model.getPriceModelContent().getContent());
        displayHandler.setContentType(model.getPriceModelContent().getContentType());
        displayHandler.setFilename(model.getPriceModelContent().getFilename());
        displayHandler.display();
    }
}
