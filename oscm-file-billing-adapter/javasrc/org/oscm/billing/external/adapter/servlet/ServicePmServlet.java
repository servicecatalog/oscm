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
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.billing.external.billing.service.BillingPluginService;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueParameterMap;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;

/**
 * Servlet for testing of service price models
 * 
 */
public class ServicePmServlet extends BillingAdapterServlet {

    private static final long serialVersionUID = -9063796890535659677L;

    private static final String REQUEST_PARAM_INSTANCE_TYPE = "instanceType";
    private static final String REQUEST_PARAM_REGION = "region";
    private static final String REQUEST_PARAM_OS = "os";
    private static final String REQUEST_PARAM_LOCALE = "locale";
    private static final String REQUEST_PARAM_CUSTOMER_ID = "customerId";

    private static final String SERVICE_PARAMETER_INSTANCE_TYPE = "INSTANCE_TYPE";
    private static final String SERVICE_PARAMETER_REGION = "REGION";
    private static final String SERVICE_PARAMETER_OS = "OS";

    private static final List<String> SUPPORTED_LOCALES = Arrays
            .asList(new String[] { "en", "de", "ja" });

    @EJB(beanInterface = BillingPluginService.class)
    protected BillingPluginService bpService;

    /**
     * Process a HTTP request for a service price model
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

        final String locale = request.getParameter(REQUEST_PARAM_LOCALE);
        if (emptyString(locale)) {
            errorForward(request, response, "Please enter a valid locale.");
            return;
        }

        Map<ContextKey, ContextValue<?>> context = evaluateServicePmContext(request);
        if (context != null) {

            PriceModel priceModel = getPriceModel(context,
                    createLocaleSet(SUPPORTED_LOCALES));
            if (priceModel == null) {
                errorForward(request, response, "Price model not found.");
                return;
            }

            PriceModelContent priceModelContent = getPriceModelContent(
                    priceModel, new Locale(locale));
            if (priceModelContent != null) {
                if (!generatePdfResponse(priceModelContent,
                        generateServicePmFileName(context), response)) {
                    errorForward(request, response,
                            "Price model found but its file type was not supported.");
                }
            } else {
                errorForward(request, response,
                        "Price model neither found for specified locale nor for default locale.");
            }
        } else {
            errorForward(request, response, "Please enter all required fields.");
        }

    }

    /**
     * Evaluate input parameters and create service price model context
     * 
     * @param request
     *            HTTP servlet request
     * @return context
     */
    private Map<ContextKey, ContextValue<?>> evaluateServicePmContext(
            HttpServletRequest request) {

        final String instanceType = request.getParameter(
                REQUEST_PARAM_INSTANCE_TYPE).trim();
        final String region = request.getParameter(REQUEST_PARAM_REGION).trim();
        final String os = request.getParameter(REQUEST_PARAM_OS).trim();
        final String customerId = request.getParameter(
                REQUEST_PARAM_CUSTOMER_ID).trim();

        if (!emptyString(instanceType) && !emptyString(region)
                && !emptyString(os)) {

            Map<String, String> parameterMap = new HashMap<String, String>();
            parameterMap.put(SERVICE_PARAMETER_INSTANCE_TYPE, instanceType);
            parameterMap.put(SERVICE_PARAMETER_REGION, region);
            parameterMap.put(SERVICE_PARAMETER_OS, os);

            Map<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
            context.put(ContextKey.SERVICE_PARAMETERS,
                    new ContextValueParameterMap(parameterMap));

            if (!emptyString(customerId)) {
                context.put(ContextKey.CUSTOMER_ID, new ContextValueString(
                        customerId));
            }

            return context;
        }

        return null;
    }

    /**
     * Generate filename for storing service price model
     */
    private String generateServicePmFileName(
            Map<ContextKey, ContextValue<?>> context) {

        Map<String, String> parameterMap = ((ContextValueParameterMap) context
                .get(ContextKey.SERVICE_PARAMETERS)).getValue();
        String instanceType = parameterMap.get(SERVICE_PARAMETER_INSTANCE_TYPE);
        String region = parameterMap.get(SERVICE_PARAMETER_REGION);
        String os = parameterMap.get(SERVICE_PARAMETER_OS);
        return instanceType.replace('.', '_') + "_" + region + "_" + os;
    }

}
