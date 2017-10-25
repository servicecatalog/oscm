/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-08-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an operation defined for a technical service.
 * 
 */
public class VOTechnicalServiceOperation extends BaseVO {

    private static final long serialVersionUID = 8416585233969041016L;

    /**
     * The identifier of the operation.
     */
    private String operationId;

    /**
     * The name of the operation.
     */
    private String operationName;

    /**
     * The description of the operation.
     */
    private String operationDescription;

    /**
     * The list of the operation parameters.
     */
    private List<VOServiceOperationParameter> operationParameters = new ArrayList<VOServiceOperationParameter>();

    /**
     * @return the operationParameters
     */
    public List<VOServiceOperationParameter> getOperationParameters() {
        return operationParameters;
    }

    /**
     * Sets the operation parameters.
     * 
     * @param operationParameters
     *            the operation parameters
     */
    public void setOperationParameters(
            List<VOServiceOperationParameter> operationParameters) {
        this.operationParameters = operationParameters;
    }

    /**
     * Retrieves the identifier of the operation.
     * 
     * @return the operation ID
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Sets the identifier of the operation.
     * 
     * @param operationId
     *            the operation ID
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Retrieves the name of the operation.
     * 
     * @return the operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Sets the name of the operation.
     * 
     * @param operationName
     *            the operation name
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Retrieves the text describing the operation.
     * 
     * @return the operation description
     */
    public String getOperationDescription() {
        return operationDescription;
    }

    /**
     * Sets the text describing the operation.
     * 
     * @param operationDescription
     *            the operation description
     */
    public void setOperationDescription(String operationDescription) {
        this.operationDescription = operationDescription;
    }

}
