/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel.external;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.billing.application.bean.LocalizedBillingResourceAssembler;
import org.oscm.billing.application.bean.LocalizedBillingResourceDAO;
import org.oscm.billing.application.bean.PriceModelPluginBean;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.converter.LocaleHandler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.base.ContextBuilder;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;

/**
 * @author stavreva
 */
@Stateless
@Remote(ExternalPriceModelService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ExternalPriceModelServiceBean implements ExternalPriceModelService {

    @EJB
    PriceModelPluginBean priceModelPluginBean;

    @EJB
    LocalizedBillingResourceDAO billingResourceDAO;

    @EJB
    LocalizerServiceLocal localizerService;

    @Inject
    OperatorServiceLocalBean operatorService;

    @EJB
    private DataService dm;

    private static final int MAX_PRICE_LENGTH = 30;

    @Override
    public void updateCache(PriceModel externalPriceModel)
            throws ExternalPriceModelException {
        if (externalPriceModel != null) {
            try {
                LocalizedBillingResourceType localizedBillingResourceType = null;
                if (externalPriceModel.getContext() == null
                        && externalPriceModel.getId() != null) {
                    localizedBillingResourceType = localizerService
                            .getLocalizedPriceModelResource(
                                    getDm().getCurrentUser().getLocale(),
                                    externalPriceModel.getId())
                            .getResourceType();
                }
                List<LocalizedBillingResource> billingResources = convertToLocalizedBillingResource(
                        externalPriceModel, localizedBillingResourceType);

                billingResources = billingResourceDAO.update(billingResources);
            } catch (BillingApplicationException e) {
                throw new ExternalPriceModelException(e);
            }
        }
    }

    List<LocalizedBillingResource> convertToLocalizedBillingResource(
            PriceModel externalPriceModel,
            LocalizedBillingResourceType localizedBillingResourceType)
            throws BillingApplicationException {
        List<LocalizedBillingResource> billingResource = LocalizedBillingResourceAssembler
                .createLocalizedBillingResources(externalPriceModel,
                        localizedBillingResourceType);
        return billingResource;
    }

    @Override
    public PriceModelContent getCachedPriceModel(Locale locale,
            UUID priceModelId) throws ExternalPriceModelException {

        if (priceModelId == null) {
            throw new ExternalPriceModelException();
        }

        if (locale == null) {
            locale = localizerService.getDefaultLocale();
        }

        LocalizedBillingResource priceModelResource = getPriceModelContentFromCache(
                locale, priceModelId);

        PriceModelContent content = new PriceModelContent(
                priceModelResource.getDataType(), priceModelResource.getValue());

        return content;
    }

    LocalizedBillingResource getPriceModelContentFromCache(Locale locale,
            UUID priceModelId) {
        LocalizedBillingResource priceModelFromCache = new LocalizedBillingResource();
        priceModelFromCache.setLocale(locale.getLanguage());
        priceModelFromCache.setObjectId(priceModelId);
        priceModelFromCache = billingResourceDAO.get(priceModelFromCache);
        return priceModelFromCache;
    }

    @Override
    public String getCachedPriceModelTag(Locale locale, UUID priceModelId)
            throws ExternalPriceModelException {

        if (priceModelId == null) {
            throw new ExternalPriceModelException();
        }

        if (locale == null) {
            locale = localizerService.getDefaultLocale();
        }

        LocalizedBillingResource priceModelResource = getPriceModelTagFromCache(
                locale, priceModelId);

        if (priceModelResource == null) {
            return "";
        }
        try {
            return truncateExternalPrice(priceModelResource);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    LocalizedBillingResource getPriceModelTagFromCache(Locale locale,
            UUID priceModelId) {
        LocalizedBillingResource priceModelTagFromCache = new LocalizedBillingResource();
        priceModelTagFromCache
                .setResourceType(LocalizedBillingResourceType.PRICEMODEL_TAG);
        priceModelTagFromCache.setLocale(locale.getLanguage());
        priceModelTagFromCache.setObjectId(priceModelId);
        priceModelTagFromCache = billingResourceDAO.get(priceModelTagFromCache);
        return priceModelTagFromCache;
    }

    private String truncateExternalPrice(
            LocalizedBillingResource priceModelResource)
            throws UnsupportedEncodingException {
        String priceModelTag = new String(priceModelResource.getValue(),
                "UTF-8");
        if (priceModelTag.length() > MAX_PRICE_LENGTH) {
            priceModelTag = priceModelTag.substring(0, MAX_PRICE_LENGTH - 1);
        }
        return priceModelTag;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public PriceModel getExternalPriceModelForService(VOServiceDetails service)
            throws ExternalPriceModelException {

        PriceModel priceModel = getExternalPriceModel(service);
        return priceModel;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public PriceModel getExternalPriceModelForCustomer(
            VOServiceDetails service, VOOrganization customer)
            throws ExternalPriceModelException {

        PriceModel priceModel = getExternalPriceModel(service, customer);
        return priceModel;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public PriceModel getExternalPriceModelForSubscription(
            VOSubscriptionDetails subscription)
            throws ExternalPriceModelException {

        PriceModel priceModel = getExternalPriceModel(
                subscription.getSubscribedService(), subscription);
        return priceModel;
    }

    private ContextBuilder prepareContextBuilderParameters(VOService service) {
        List<VOParameter> parameters = service.getParameters();
        Map<String, String> parameterMap = new HashMap<String, String>();

        for (VOParameter param : parameters) {
            parameterMap.put(param.getParameterDefinition().getParameterId(),
                    param.getValue());
        }

        ContextBuilder contextBuilder = new ContextBuilder();
        contextBuilder.addServiceParameters(parameterMap);
        return contextBuilder;
    }

    private PriceModel getExternalPriceModel(VOService service,
            VOSubscriptionDetails subscription)
            throws ExternalPriceModelException {
        ContextBuilder contextBuilder = new ContextBuilder();
        if (subscription != null) {
            contextBuilder.addSubscription(subscription);
        }
        try {
            return priceModelPluginBean.getPriceModel(
                    service.getBillingIdentifier(), getLocales(),
                    contextBuilder.build());
        } catch (BillingApplicationException e) {
            throw new ExternalPriceModelException(e);
        }
    }

    private PriceModel getExternalPriceModel(VOServiceDetails service)
            throws ExternalPriceModelException {
        ContextBuilder contextBuilder = prepareContextBuilderParameters(service);
        try {
            return priceModelPluginBean.getPriceModel(
                    service.getBillingIdentifier(), getLocales(),
                    contextBuilder.build());
        } catch (BillingApplicationException e) {
            throw new ExternalPriceModelException(e);
        }
    }

    private PriceModel getExternalPriceModel(VOServiceDetails service,
            VOOrganization customer) throws ExternalPriceModelException {

        ContextBuilder contextBuilder = prepareContextBuilderParameters(service);
        if (customer != null) {
            contextBuilder.addCustomer(customer);
        }
        try {
            return priceModelPluginBean.getPriceModel(
                    service.getBillingIdentifier(), getLocales(),
                    contextBuilder.build());
        } catch (BillingApplicationException e) {
            throw new ExternalPriceModelException(e);
        }
    }

    private Set<Locale> getLocales() {
        List<SupportedLanguage> languageList = operatorService
                .getLanguages(false);
        Set<Locale> locales = new HashSet<Locale>();

        if (languageList != null) {
            for (SupportedLanguage language : languageList) {
                locales.add(LocaleHandler.getLocaleFromString(language
                        .getLanguageISOCode()));
            }
        }
        return locales;
    }

    /**
     * @return the dm
     */
    public DataService getDm() {
        return dm;
    }

    /**
     * @param dm
     *            the dm to set
     */
    public void setDm(DataService dm) {
        this.dm = dm;
    }
}
