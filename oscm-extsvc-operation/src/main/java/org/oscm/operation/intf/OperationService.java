/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-08-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operation.intf;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;

/**
 * Interface defining the Web services which organizations must provide in order
 * to be able to receive technical service operation calls from the platform.
 * Such Web services and technical service operations can be used to access the
 * resources of an application and perform administrative tasks without actually
 * opening the application. The operations and the Web service must be specified
 * in the definition of the technical service for the application.
 */
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface OperationService {

    /**
     * Executes the operation identified by its ID on behalf of the given user
     * for the specified application instance.
     * 
     * @param userId
     *            the ID of the user who triggered the service operation. If
     *            user IDs of the platform are mapped to user IDs of the
     *            application, the application user ID is passed. Otherwise, the
     *            platform user ID is passed.
     * @param instanceId
     *            the ID of the application instance for which the operation is
     *            to be executed
     * @param transactionId
     *            the transaction ID of the service operation
     * @param operationId
     *            the ID of the operation to be executed as specified in the
     *            technical service definition
     * @param parameters
     *            the parameters of the operation with their values
     * @return <code>OperationResult</code>, if the operation was successful (
     *         <code>OperationResult.errorMessage</code> is <code>null</code> or
     *         empty; otherwise it contains details about the error that
     *         occurred
     */
    public OperationResult executeServiceOperation(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "transactionId") String transactionId,
            @WebParam(name = "operationId") String operationId,
            @WebParam(name = "parameters") List<OperationParameter> parameters);

    /**
     * Requests the given application instance to return the possible values for
     * all parameters with a predefined set of values for the specified service
     * operation on behalf of the given user.
     * 
     * @param userId
     *            the ID of the user who requests the operation parameter
     *            values. If user IDs of the platform are mapped to user IDs of
     *            the application, the application user ID is passed. Otherwise,
     *            the platform user ID is passed.
     * @param instanceId
     *            the ID of the application instance for which to get the
     *            operation parameter values
     * @param operationId
     *            the ID of the operation for which to get the parameter values
     *            as specified in the technical service definition
     * @return the possible values for all parameters with a predefined set of
     *         values for the specified service operation and application
     *         instance
     */
    public List<OperationParameter> getParameterValues(
            @WebParam(name = "userId") String userId,
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "operationId") String operationId);

}
