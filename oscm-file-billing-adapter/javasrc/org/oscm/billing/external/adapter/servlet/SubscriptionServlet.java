/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.billing.external.adapter.bean.SubscriptionAgent;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;

/**
 * Servlet for testing of the subscription price model push mechanism
 * 
 */
public class SubscriptionServlet extends BillingAdapterServlet {

    private static final long serialVersionUID = -4689541009012302089L;

    private static final String REQUEST_PARAM_SUBSCRIPTION_ID = "subscriptionId";
    private static final String REQUEST_PARAM_TENANT_ID = "tenantId";
    private static final String REQUEST_PARAM_BUTTON_SHOW_SUB_PM = "buttonShowSubPm";
    private static final List<String> BILLING_APPLICATION_LOCALES = Arrays
            .asList(new String[] { "en", "de" });

    @EJB
    protected SubscriptionAgent subAgent;

    /**
     * Process a HTTP request to push a subscription price model
     * 
     * @param request
     *            a HTTP servlet request
     * @param response
     *            a HTTP servlet response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void process(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
        }

        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        final String subscriptionId = request
                .getParameter(REQUEST_PARAM_SUBSCRIPTION_ID).trim();
        final String tenantId = request.getParameter(REQUEST_PARAM_TENANT_ID)
                .trim();
        final boolean showSubscriptionPm = !emptyString(
                request.getParameter(REQUEST_PARAM_BUTTON_SHOW_SUB_PM));

        Map<ContextKey, ContextValue<?>> context = createSubscriptionPmContext(
                subscriptionId, tenantId);

        if (context != null) {

            PriceModel priceModel = getPriceModel(context,
                    createLocaleSet(BILLING_APPLICATION_LOCALES));
            if (priceModel == null) {
                errorForward(request, response, "Price model for subscription "
                        + subscriptionId + " not found.");
                return;
            }

            if (showSubscriptionPm) {
                PriceModelContent priceModelContent = getPriceModelContent(
                        priceModel, DEFAULT_LOCALE);
                if (priceModelContent != null) {
                    if (!generatePdfResponse(priceModelContent,
                            generateSubscriptionPmFileName(subscriptionId),
                            response)) {
                        errorForward(request, response,
                                "Price model found but its file type was not supported.");
                    }
                } else {
                    errorForward(request, response, "Price model not found.");
                }
            } else {
                pushPriceModel(priceModel, subscriptionId, request, response);
            }
        } else {
            errorForward(request, response,
                    "Please enter a valid subscription ID!");
        }
    }

    /**
     * Create subscription price model context
     * 
     * @param subscriptionId
     * @return context
     */
    private Map<ContextKey, ContextValue<?>> createSubscriptionPmContext(
            String subscriptionId, String tenantId) {

        if (!emptyString(subscriptionId) && !emptyString(tenantId)) {

            Map<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
            context.put(ContextKey.SUBSCRIPTION_ID,
                    new ContextValueString(subscriptionId));
            context.put(ContextKey.TENANT_ID, new ContextValueString(tenantId));
            return context;
        }

        return null;
    }

    /**
     * Push the price model to BSS task queue
     */
    private void pushPriceModel(PriceModel pm, String subscriptionId,
            HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        if (subAgent.pushPriceModel(pm)) {
            successForward(request, response,
                    "Subscription price model for subscription '"
                            + subscriptionId + "' sent to OSCM Task Queue!");
        } else {
            errorForward(request, response,
                    "Couldn't send message to OSCM Task Queue");
        }

    }

    /**
     * Generate filename for storing subscription price model
     */
    private String generateSubscriptionPmFileName(String subscriptionId) {

        return subscriptionId + "_" + DEFAULT_LOCALE.getLanguage();
    }

}
