/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.Invariants;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Helper class to retrieve or create required data to be used by the operator
 * service bean.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OperatorServiceDataHandler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OperatorServiceDataHandler.class);

    /**
     * Finds the organization reference of the given source organization to the
     * specified target organization. If it does not exist, it will be created.
     * 
     * @param source
     *            The organization that is stored as source for the organization
     *            reference.
     * @param target
     *            The organization to be stored as target for the organization
     *            reference.
     * @param refType
     *            The type of the reference to bet set.
     * @param dataService
     *            The reference to the data service used to retrieve or persist
     *            data.
     * @return The organization reference.
     */
    static OrganizationReference findOrCreateOrganizationReference(
            Organization source, Organization target,
            OrganizationReferenceType refType, DataService dataService) {
        Invariants
                .assertNotNull(refType, "organization reference type not set");
        OrganizationReference ref = new OrganizationReference(source, target,
                refType);
        OrganizationReference result = (OrganizationReference) dataService
                .find(ref);

        // handle case that object is not found
        if (result == null) {
            result = new OrganizationReference(source, target, refType);
            try {
                dataService.persist(result);
                source.getTargets().add(result);
                target.getSources().add(result);
                dataService.flush();
            } catch (NonUniqueBusinessKeyException e) {
                // has been created in the meantime, so return the existing one
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_ORGANIZATION_ALREADY_EXIST);
                result = (OrganizationReference) dataService.find(ref);
            }
        }

        return result;
    }

}
