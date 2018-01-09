/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: afschar
 *
 *  Creation Date: Jul 27, 2011
 *                                                                              
 *  Completion Time: Jul 28, 2011
 *
 *******************************************************************************/

package org.oscm.paypalprototype.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Paypal call for the Preapproval request
 * 
 * @author afschar
 */
public class PreapprovalServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Input is nothing, output is the preapproval key from Paypal. It will be
     * stored in the session as preapprovalKey.
     * 
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            final String preapprovalKey = request
                    .getParameter("preapprovalKey");
            final PaypalRequest paypalRequest = new PaypalRequest(
                    request.getRemoteAddr());
            final PaypalResponse paypalResponse = sendPaypalRequest(preapprovalKey == null ? paypalRequest
                    .buildPreapprovalRequest() : paypalRequest
                    .buildCancelPreapprovalRequest(request
                            .getParameter("preapprovalKey")));

            setDefaultAttributes(request, paypalRequest, paypalResponse);

            request.getSession().setAttribute("preapprovalKey",
                    paypalResponse.getPreapprovalKey());

            request.getRequestDispatcher(
                    preapprovalKey == null ? "/preapprovalLink.jsp"
                            : "/index.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
