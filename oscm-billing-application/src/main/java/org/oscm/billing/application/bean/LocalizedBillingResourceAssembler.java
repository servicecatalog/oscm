/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * This class is responsible for assembling the domain objects for localized
 * billing resources. During the assembling, some validations are done.
 * 
 * @author baumann
 * 
 */
public class LocalizedBillingResourceAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LocalizedBillingResourceAssembler.class);

    public static List<LocalizedBillingResource> createLocalizedBillingResources(
            PriceModel localizedPriceModel,
            LocalizedBillingResourceType localizedBillingResourceType)
            throws BillingApplicationException {
        List<LocalizedBillingResource> localizedBillingresources = new ArrayList<LocalizedBillingResource>();

        LocalizedBillingResourceType lrt = localizedBillingResourceType;
        if (localizedPriceModel.getContext() != null) {
            if (localizedPriceModel.getContext().containsKey(
                    ContextKey.SUBSCRIPTION_ID)) {
                lrt = LocalizedBillingResourceType.PRICEMODEL_SUBSCRIPTION;
            } else if (localizedPriceModel.getContext().containsKey(
                    ContextKey.CUSTOMER_ID)) {
                lrt = LocalizedBillingResourceType.PRICEMODEL_CUSTOMER;
            } else {
                lrt = LocalizedBillingResourceType.PRICEMODEL_SERVICE;
            }
        }

        for (Locale locale : localizedPriceModel.getLocales()) {
            PriceModelContent pmc = localizedPriceModel.get(locale);
            localizedBillingresources.add(createPriceModel(
                    localizedPriceModel.getId(), locale, pmc, lrt));
            localizedBillingresources.add(createPriceModelTag(locale,
                    localizedPriceModel.getId(), pmc.getTag()));

        }

        return localizedBillingresources;
    }

    public static LocalizedBillingResource createPriceModel(UUID objectID,
            Locale locale, PriceModelContent priceModelDescription,
            LocalizedBillingResourceType localizedBillingResourceType)
            throws BillingApplicationException {

        validatePriceModelContent(priceModelDescription);

        LocalizedBillingResource localizedBillingresource = new LocalizedBillingResource(
                objectID, locale.getLanguage(), localizedBillingResourceType);
        localizedBillingresource.setDataType(priceModelDescription
                .getContentType());
        localizedBillingresource.setValue(priceModelDescription.getContent());

        return localizedBillingresource;
    }

    static void validatePriceModelContent(PriceModelContent priceModelContent)
            throws BillingApplicationException {

        if (MediaType.APPLICATION_JSON.equals(priceModelContent
                .getContentType())) {

            if (!isValidJSON(new String(priceModelContent.getContent()))) {
                throw new BillingApplicationException(
                        "The external price model description is not a valid JSON string.");
            }

        }
    }

    static boolean isValidJSON(String jsonString)
            throws BillingApplicationException {

        ObjectMapper om = new ObjectMapper();
        try {
            JsonParser parser = om.getFactory().createParser(jsonString);
            while (parser.nextToken() != null) {
            }
            return true;
        } catch (JsonParseException jpe) {
            logger.logError(Log4jLogger.SYSTEM_LOG, jpe,
                    LogMessageIdentifier.ERROR_INVALID_JSON);
            return false;
        } catch (IOException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IO_VALIDITY_EXTERNAL_JSON);
            throw new BillingApplicationException(
                    "IO Error when checking JSON validity of external price model description.");
        }
    }

    public static LocalizedBillingResource createPriceModelTag(Locale locale,
            UUID objectID, String priceModelTag) {
        LocalizedBillingResource localizedBillingresource = new LocalizedBillingResource(
                objectID, locale.getLanguage(),
                LocalizedBillingResourceType.PRICEMODEL_TAG);
        localizedBillingresource.setDataType(MediaType.TEXT_PLAIN);
        localizedBillingresource.setValue(priceModelTag.getBytes());
        if (priceModelTag.length() > 30) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_TOO_MANY_CHARACTERS_FOR_PRICE_FROM_TAG);
        }
        return localizedBillingresource;
    }
}