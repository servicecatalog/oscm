/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import java.util.List;

import org.oscm.converter.api.Converter;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.types.enumtypes.ServiceAccessType;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
public class ToExtProductStrategy extends AbstractConversionStrategy implements ConversionStrategy<Product, VOService> {

    @Override
    public VOService convert(Product product) {
        if (product == null) {
            return null;
        }

        VOService voService = new VOService();

        voService.setKey(product.getKey());
        voService.setVersion(product.getVersion());
        voService.setServiceId(product.getProductId());
        voService.setConfiguratorUrl(product.getConfiguratorUrl());
        voService.setCustomTabUrl(product.getCustomTabUrl());
        VOPriceModel voPriceModel = Converter.convert(product.getPriceModel(), PriceModel.class, VOPriceModel.class, getDataService());
        voService.setPriceModel(voPriceModel);
        if (product.getParameterSet() != null) {
            List<VOParameter> voParameters = Converter.convertList(product.getParameterSet().getParameters(), Parameter.class, VOParameter.class, getDataService());
            voService.setParameters(voParameters);
        }
        if (product.getProductFeedback() != null) {
            voService.setAverageRating(product.getProductFeedback().getAverageRating());
            voService.setNumberOfReviews(product.getProductFeedback().getProductReviews().size());
        }
        voService.setBaseURL(product.getTechnicalProduct().getBaseURL());
        voService.setAccessType(ServiceAccessType.valueOf(product.getTechnicalProduct().getAccessType().name()));
        voService.setSellerId(product.getVendor().getOrganizationId());
        voService.setSellerKey(product.getVendorKey());
        voService.setSellerName(product.getVendor().getName());
        ServiceStatus serviceStatus = ServiceStatus.valueOf(product.getStatus().name());
        org.oscm.types.enumtypes.ServiceStatus voServicestatus = EnumConverter.convert(serviceStatus, org.oscm.types.enumtypes.ServiceStatus.class);
        voService.setStatus(voServicestatus);
        voService.setTechnicalId(product.getTechnicalProduct().getTechnicalProductId());
        voService.setServiceId(product.getCleanProductId());
        voService.setFeatureURL(product.getTechnicalProduct().getProvisioningURL());

        final List<LocalizedObjectTypes> localizedObjectTypes = product.getLocalizedObjectTypes();
        final String locale = getDataService().getCurrentUser().getLocale();
        final List<LocalizedResource> localizedResources = getLocalizedResource(localizedObjectTypes, Long.valueOf(product.getKey()), locale);

        for (LocalizedResource resource : localizedResources) {
            if (resource.getObjectType().equals(LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION)){
                voService.setShortDescription(resource.getValue());
            }
            else if (resource.getObjectType().equals(LocalizedObjectTypes.PRODUCT_MARKETING_DESC)) {
                voService.setDescription(resource.getValue());
            }
            else if (resource.getObjectType().equals(LocalizedObjectTypes.PRODUCT_MARKETING_NAME)) {
                voService.setName(resource.getValue());
            } else if (resource.getObjectType().equals(LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME)) {
                voService.setCustomTabName(resource.getValue());
            }

        }

        return voService;
    }
}
