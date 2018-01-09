/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationhelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Processes a <code>GET</code> or <code>POST</code> request.
 */
public class TokenhandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 1369077840252614627L;

    /**
     * Processes a <code>GET</code> or <code>POST</code> request. The method
     * calls <code>BssClient</code> for resolution of the user token.
     * 
     * @param request
     *            the HTTP servlet request
     * @param response
     *            the HTTP servlet response
     * @throws IOException
     *             if an input or output error occurs when the servlet handles
     *             the request
     * @throws ServletException
     *             if the request cannot be handled
     */
    protected void process(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        BssClient.processHTTPRequest(request, response, getServletContext());
    }

    /**
     * Called by the server (via the corresponding service method) to trigger
     * the servlet to handle a <code>GET</code> request.
     * 
     * @param request
     *            the HTTP servlet request
     * @param response
     *            the HTTP servlet response
     * @throws IOException
     *             if an input or output error occurs when the servlet handles
     *             the <code>GET</code> request
     * @throws ServletException
     *             if the request for the <code>GET</code> cannot be handled
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Called by the server (via the corresponding service method) to trigger
     * the servlet to handle a <code>POST</code> request.
     * 
     * @param request
     *            the HTTP servlet request
     * @param response
     *            the HTTP servlet response
     * @throws IOException
     *             if an input or output error occurs when the servlet handles
     *             the <code>POST</code> request
     * @throws ServletException
     *             if the request for the <code>POST</code> cannot be handled
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

}
