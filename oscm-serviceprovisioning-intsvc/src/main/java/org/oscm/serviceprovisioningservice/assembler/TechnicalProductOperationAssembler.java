/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                  
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *  Completion Time: 16.08.2010                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class TechnicalProductOperationAssembler extends BaseAssembler {

    /**
     * Converts a list of {@link TechnicalProductOperation}s into a list of
     * {@link VOTechnicalServiceOperation}s. If <code>null</code> is passed in,
     * <code>null</code> is returned.
     * 
     * @param technicalProductOperations
     *            the {@link TechnicalProductOperation}s to convert
     * @param facade
     *            the {@link LocalizerFacade} to get the localizable name and
     *            description from.
     * @return the list of {@link VOTechnicalServiceOperation}.
     */
    public static List<VOTechnicalServiceOperation> toVOTechnicalServiceOperations(
            List<TechnicalProductOperation> technicalProductOperations,
            LocalizerFacade facade) {
        if (technicalProductOperations == null) {
            return null;
        }
        List<VOTechnicalServiceOperation> result = new ArrayList<VOTechnicalServiceOperation>();
        for (TechnicalProductOperation technicalProductOperation : technicalProductOperations) {
            result.add(toVOTechnicalServiceOperation(technicalProductOperation,
                    facade));
        }
        return result;
    }

    /**
     * Converts a {@link TechnicalProductOperation} into a
     * {@link VOTechnicalServiceOperation}. If <code>null</code> is passed in,
     * <code>null</code> is returned.
     * 
     * @param technicalProductOperation
     *            the {@link TechnicalProductOperation} to convert
     * @param facade
     *            the {@link LocalizerFacade} to get the localizable name and
     *            description from.
     * @return the {@link VOTechnicalServiceOperation}.
     */
    public static VOTechnicalServiceOperation toVOTechnicalServiceOperation(
            TechnicalProductOperation technicalProductOperation,
            LocalizerFacade facade) {
        if (technicalProductOperation == null) {
            return null;
        }
        VOTechnicalServiceOperation operation = new VOTechnicalServiceOperation();
        operation.setOperationId(technicalProductOperation.getOperationId());
        operation.setOperationDescription(facade.getText(
                technicalProductOperation.getKey(),
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION));
        operation.setOperationName(facade.getText(technicalProductOperation
                .getKey(),
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME));
        updateOperationParameters(operation, technicalProductOperation, facade);
        updateValueObject(operation, technicalProductOperation);
        return operation;
    }

    /**
     * Converts a {@link OperationParameter} into a
     * {@link VOServiceOperationParameter}, then set to
     * {@link VOTechnicalServiceOperation}. If <code>null</code> is passed in,
     * <code>null</code> is returned.
     */
    private static void updateOperationParameters(
            VOTechnicalServiceOperation operation,
            TechnicalProductOperation technicalProductOperation,
            LocalizerFacade facade) {
        for (OperationParameter operationParameter : technicalProductOperation
                .getParameters()) {
            operation.getOperationParameters().add(
                    TechServiceOperationParameterAssembler
                            .toVOServiceOperationParameter(operationParameter,
                                    facade));
        }

    }
}
