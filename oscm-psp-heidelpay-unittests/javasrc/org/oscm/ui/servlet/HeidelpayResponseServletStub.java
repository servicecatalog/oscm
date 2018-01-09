/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                          
 *                                                                              
 *  Creation Date: 19.10.2011                                                      
 *                                                                              
 *  Completion Time: 19.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.servlet;

import java.net.MalformedURLException;

import org.oscm.ui.stub.PaymentRegistrationServiceStub;
import org.oscm.psp.data.RegistrationData;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * At some point the heidelpay response servlet has to call the
 * <code>register(RegistrationData result)</code> method of
 * <code>PaymentRegistrationService</code> webservice. Therefore the servlet
 * makes a static call to a static java factory class. This class cannot be
 * mocked easily. But we can extend the actual servlet and mock the method which
 * makes this static call.
 * 
 * */
public class HeidelpayResponseServletStub extends HeidelpayResponseServlet {

    private static final long serialVersionUID = -7603666692300330563L;

    private PaymentRegistrationServiceStub registrationMock = new PaymentRegistrationServiceStub();

    @Override
    String register(String wsdlUrl, RegistrationData registrationData,
            String wsUrl) throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException, MalformedURLException {
        return registrationMock.register(registrationData);
    }

    public PaymentRegistrationServiceStub getRegistrationMock() {
        return registrationMock;
    }

    public void setRegistrationMock(
            PaymentRegistrationServiceStub registrationMock) {
        this.registrationMock = registrationMock;
    }

}
