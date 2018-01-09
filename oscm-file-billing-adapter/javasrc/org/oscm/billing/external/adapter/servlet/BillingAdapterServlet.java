/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.09.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.exception.BillingException;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.billing.external.pricemodel.service.PriceModelPluginService;

/**
 * A Billing Adapter Servlet
 */
public abstract class BillingAdapterServlet extends HttpServlet {

    private static final long serialVersionUID = -4689541009012302089L;

    private static final String FILE_TYPE_PDF = "application/pdf";
    protected static final Locale DEFAULT_LOCALE = new Locale("en");

    @EJB(beanInterface = PriceModelPluginService.class)
    protected PriceModelPluginService pmService;

    protected abstract void process(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Set error message and forward
     */
    protected void errorForward(HttpServletRequest request,
            HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", errorMessage);
        forward(request, response);
    }

    /**
     * Set success message and forward
     */
    protected void successForward(HttpServletRequest request,
            HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("successMessage", errorMessage);
        forward(request, response);
    }

    /**
     * Forward request to index page
     */
    protected void forward(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
        rd.forward(request, response);
    }

    /**
     * Create a set of locales for the given list of languages
     */
    protected Set<Locale> createLocaleSet(List<String> languages) {

        Set<Locale> locales = new HashSet<Locale>();
        for (String language : languages) {
            locales.add(new Locale(language));
        }
        return locales;
    }

    /**
     * Call the price model plugin to get a price model
     * 
     * @param context
     *            the price model context
     * @param locales
     *            a set of locales
     * @return a price model object
     */
    protected PriceModel getPriceModel(
            Map<ContextKey, ContextValue<?>> context, Set<Locale> locales) {

        PriceModel pm = null;
        try {
            pm = pmService.getPriceModel(context, locales);
        } catch (BillingException e) {
            e.printStackTrace();
        }

        return pm;
    }

    /**
     * Get price model content for a specific locale. If the locale is not
     * found, the content for the default locale is returned.
     * 
     * @param priceModel
     *            a price model object
     * @param locale
     *            a locale
     * @return the price model content
     */
    protected PriceModelContent getPriceModelContent(PriceModel priceModel,
            Locale locale) {

        PriceModelContent content = null;
        Set<Locale> locales = priceModel.getLocales();
        if (locales.contains(locale)) {
            content = priceModel.get(locale);
        } else if (locales.contains(DEFAULT_LOCALE)) {
            content = priceModel.get(DEFAULT_LOCALE);
        }

        if (content != null && content.getContent() != null
                && content.getContent().length > 0
                && !emptyString(content.getContentType())) {
            return content;
        } else {
            return null;
        }
    }

    /**
     * Generate response for showing pdf price model
     */
    protected boolean generatePdfResponse(PriceModelContent priceModelContent,
            String filename, HttpServletResponse response) throws IOException {
        if (priceModelContent.getContentType().equals(FILE_TYPE_PDF)) {
            byte[] content = priceModelContent.getContent();
            response.setContentType(FILE_TYPE_PDF);
            response.setContentLength(content.length);
            response.addHeader("Content-Disposition", "inline; filename="
                    + filename + ".pdf");
            response.addHeader("Accept-Ranges", "bytes");
            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(content);
            responseOutputStream.flush();
            return true;
        } else {
            return false;
        }
    }

    protected boolean emptyString(String s) {
        return (s == null || s.length() == 0);
    }

}
