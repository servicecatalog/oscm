/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                  
 *                                                                              
 *  Creation Date: 18.08.2010                                                      
 *                                                                              
 *  Completion Time: 18.08.2010                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.mockproduct;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.oscm.integrationtests.mockproduct.RequestLogEntry.RequestDirection;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;

/**
 * @author weiser
 * 
 */
@WebService(serviceName = "OperationService", targetNamespace = "http://oscm.org/xsd", portName = "OperationServicePort", endpointInterface = "org.oscm.operation.intf.OperationService", wsdlLocation = "WEB-INF/wsdl/OperationService.wsdl")
public class OperationServiceBean implements OperationService {

    private static final String CAUSE_ERROR = "CAUSE_ERROR";

    @Resource
    private WebServiceContext context;

    @Override
    public OperationResult executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters) {
        OperationResult result = new OperationResult();
        final RequestLogEntry entry = createLogEntry("executeServiceOperation");
        entry.addParameter("userId", userId);
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("transactionId", transactionId);
        entry.addParameter("operationId", operationId);
        entry.addParameter("parameters", parameters);
        if (CAUSE_ERROR.equalsIgnoreCase(operationId)) {
            String message = "User '%s' is not allowed to execute the operation for instance '%s'.";
            message = String.format(message, userId, instanceId);
            entry.setResult(message);
            result.setErrorMessage(message);
        }
        return result;
    }

    private RequestLogEntry createLogEntry(String title) {
        final ServletContext servletContext = (ServletContext) context
                .getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        final RequestLog log = (RequestLog) servletContext
                .getAttribute(InitServlet.REQUESTLOG);
        final RequestLogEntry entry = log.createEntry(
                OperationService.class.getSimpleName() + "." + title,
                RequestDirection.INBOUND);
        ServletRequest request = (ServletRequest) context.getMessageContext()
                .get(MessageContext.SERVLET_REQUEST);
        entry.setHost(request.getRemoteHost());
        return entry;
    }

    @Override
    public List<OperationParameter> getParameterValues(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "operationId") String operationId) {
        final RequestLogEntry entry = createLogEntry("getParameterValues");
        entry.addParameter("userId", userId);
        entry.addParameter("instanceId", instanceId);
        entry.addParameter("operationId", operationId);
        ArrayList<OperationParameter> result = new ArrayList<>();
        if ("SNAPSHOT".equals(operationId)) {
            OperationParameter op = new OperationParameter();
            op.setName("SERVER");
            op.setValue("Server 1");
            result.add(op);

            op = new OperationParameter();
            op.setName("SERVER");
            op.setValue("Server 2");
            result.add(op);

            op = new OperationParameter();
            op.setName("SERVER");
            op.setValue("Server 3");
            result.add(op);
        }
        entry.setResult(result);
        return result;
    }
}
