/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;

/**
 * This is a stub implementation of the {@link OperationService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author weiser
 * 
 */
@WebService(serviceName = "OperationService", targetNamespace = "http://oscm.org/xsd", portName = "OperationServicePort", endpointInterface = "org.oscm.operation.intf.OperationService")
public class OperationServiceImpl implements OperationService {

    @Override
    public OperationResult executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<OperationParameter> getParameterValues(String userId,
            String instanceId, String operationId) {
        throw new UnsupportedOperationException();
    }

}
