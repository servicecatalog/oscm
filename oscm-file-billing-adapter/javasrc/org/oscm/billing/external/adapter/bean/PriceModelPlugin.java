/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.client.WebTarget;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.exception.BillingException;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.billing.external.pricemodel.service.PriceModelPluginService;

/**
 * The implementation of the price model interface
 *
 */
@Stateless
@Remote({ PriceModelPluginService.class })
public class PriceModelPlugin implements PriceModelPluginService {

    public static final String ID = "FILE_BILLING";
    public static final String PRICEMODEL_URL = "priceModelURL";
    public static final String PRICEMODEL_FILE_URL = "priceModelFileURL";
    public static final String FILENAME_PARAMETER = "FILENAME";

    ConfigProperties properties = new ConfigProperties(ID);
    RestDAO restDao = new RestDAO();

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public PriceModel getPriceModel(Map<ContextKey, ContextValue<?>> context,
            Set<Locale> locales) throws BillingException {

        QueryParamMultiValuedMap queryParams = new QueryParamMultiValuedMap();
        queryParams.add(locales);
        queryParams.add(context);

        WebTarget webResource = restDao.createMultiValueWebResource(
                properties.getConfigProperty(PRICEMODEL_URL), queryParams);
        List<String> priceModelData = restDao.getPriceModelData(webResource);

        if (priceModelData != null && !priceModelData.isEmpty()) {
            return convertToPriceModel(priceModelData, context);
        } else {
            return null;
        }
    }

    /**
     * Convert the price model data from the file billing application to a
     * PriceModel object. The contents of the price model file are read from the
     * file billing application with an own REST call.
     * 
     * @param priceModelData
     *            a List of String's containing the price model data
     * @param context
     *            the OSCM price model context
     * @return a PriceModelObject
     * @throws BillingException
     *             if the call to the file billing application is not successful
     */
    PriceModel convertToPriceModel(List<String> priceModelData,
            Map<ContextKey, ContextValue<?>> context) throws BillingException {

        PriceModel priceModel = new PriceModel(UUID.fromString(priceModelData
                .get(0)));
        priceModel.setContext(context);
        for (int i = 1; i <= priceModelData.size() - 4; i++) {
            String localeText = priceModelData.get(i);
            String fileType = priceModelData.get(++i);
            String fileName = priceModelData.get(++i);
            String tag = priceModelData.get(++i);

            if (localeText != null && fileType != null && fileName != null
                    && tag != null) {
                byte[] priceModelFile = getPriceModelFile(fileName);
                if (priceModelFile != null) {
                    Locale locale = new Locale(localeText);
                    PriceModelContent content = new PriceModelContent();
                    content.setContentType(fileType);
                    content.setTag(tag);
                    content.setContent(priceModelFile);
                    content.setFilename(fileName);
                    priceModel.put(locale, content);
                }
            }
        }

        if (priceModel.getLocales().size() > 0) {
            return priceModel;
        } else {
            return null;
        }
    }

    /**
     * Get the contents of the given price model file from the file billing
     * application
     * 
     * @param fileName
     *            the file name
     * @return the contents of the price model file
     * @throws BillingException
     *             if the call to the file billing application is not successful
     */
    byte[] getPriceModelFile(String fileName) throws BillingException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put(FILENAME_PARAMETER, fileName);
        WebTarget webResource = restDao.createWebResource(
                properties.getConfigProperty(PRICEMODEL_FILE_URL), queryParams);
        return restDao.getFileResponse(webResource);
    }
}
