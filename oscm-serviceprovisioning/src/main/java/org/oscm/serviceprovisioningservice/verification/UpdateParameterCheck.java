/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-1                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.verification;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;

/**
 * Class to ensure contraints to modificationType parameter are met objects to
 * be updated.
 * 
 * @author yuyin
 * 
 */
public class UpdateParameterCheck {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PricedParameterChecks.class);

    /**
     * Verifies that the modificationType attribute of parameterDefinition can
     * be updated.
     * 
     * @param param
     *            The parameter should be updated.
     * @param techProd
     *            The current technical product which should be update.
     * @param modificationType
     *            The value of modificationType which based on import file.
     * @throws UpdateConstraintException
     *             Thrown in case modificationType of Parameter Definition
     *             should be changed, and exist Marketplace Service for current
     *             technical product
     * @throws ValidationException
     */
    public static void updateParameterDefinition(ParameterDefinition param,
            TechnicalProduct techProd, String modificationType)
            throws UpdateConstraintException {
        if (param == null || techProd == null || techProd.getProducts() == null) {
            return;
        }

        // check update action and Marketplace Service exists
        for (Product product : techProd.getProducts()) {
            if (!product.isDeleted()
                    && !param.getModificationType().name()
                            .equals(modificationType)) {
                UpdateConstraintException uce = new UpdateConstraintException(
                        ClassEnum.TECHNICAL_SERVICE,
                        techProd.getTechnicalProductId());
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        uce,
                        LogMessageIdentifier.WARN_TECH_SERVICE_PARAMETER_DEFINITION_FAILED);
                throw uce;
            }
        }
    }

}
