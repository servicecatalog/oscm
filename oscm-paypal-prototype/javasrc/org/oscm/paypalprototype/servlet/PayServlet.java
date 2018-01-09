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

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Servlet implementation class SendPreapprovalServlet. Makes a Preapproval call
 * to Paypal and dispatches to payResponse.jsp
 * 
 * @author afschar
 */
public class PayServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Sends the request to paypal. Input is preapprovalKey of buyer, email of
     * seller, and amount to be transferred.
     * 
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            final PaypalRequest paypalRequest = new PaypalRequest(
                    request.getRemoteAddr());
            final PostMethod post = paypalRequest.buildPayRequest(
                    request.getParameter("preapprovalKey"),
                    request.getParameter("email"),
                    request.getParameter("amount"));

            setDefaultAttributes(request, paypalRequest,
                    sendPaypalRequest(post));

            request.getRequestDispatcher("/payResponse.jsp").forward(request,
                    response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
