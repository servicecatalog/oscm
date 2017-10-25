/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.operation.adapter;

import java.util.List;

import org.oscm.applicationservice.adapter.OperationServiceAdapter;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;

public class OperationServiceAdapterV1_0 implements OperationServiceAdapter {
    OperationService svc;
    
    @Override
    public OperationResult executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters) {
        return svc.executeServiceOperation(userId, instanceId, transactionId,
                operationId, parameters);
    }
    
    @Override
    public void setOperationService(Object port) {
        svc = OperationService.class.cast(port);
    }
    
    @Override
    public List<OperationParameter> getParameterValues(String userId,
            String instanceId, String operationId) {
        return svc.getParameterValues(userId, instanceId, operationId);
    }
}
