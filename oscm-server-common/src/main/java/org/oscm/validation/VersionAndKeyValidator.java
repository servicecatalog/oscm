/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.DomainObject;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;

public class VersionAndKeyValidator {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(VersionAndKeyValidator.class);

    public static DomainObject<?> verify(DomainObject<?> toBeUpdated,
            DomainObject<?> template, int templateVersion)
            throws ConcurrentModificationException {
        return verify(toBeUpdated, template.getKey(), templateVersion);
    }

    public static DomainObject<?> verify(DomainObject<?> toBeUpdated,
            long templateKey, int templateVersion)
            throws ConcurrentModificationException {
        if (toBeUpdated.getVersion() > templateVersion) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    toBeUpdated.getClass().getSimpleName(), templateVersion);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION,
                    toBeUpdated.getClass().getSimpleName());
            throw cme;
        }

        if (toBeUpdated.getKey() != templateKey) {
            if (templateKey == 0) {
                // on save operations where create and update can be done, a new
                // VO may be passed which has already been created as DO and
                // read from e.g. a list of existing ones (and not by
                // getReference(long key))
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        toBeUpdated.getClass().getSimpleName(), templateVersion);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                        LogMessageIdentifier.WARN_OBJECT_CREATED_CONCURRENTLY,
                        toBeUpdated.getClass().getSimpleName());
                throw cme;
            } else {
                SaaSSystemException sse = new SaaSSystemException(
                        String.format("Different object keys for type %s",
                                DomainObject.getDomainClass(toBeUpdated)
                                        .getSimpleName()));
                logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_DIFFERENT_KEY_TYPE,
                        toBeUpdated.getClass().getSimpleName());
                throw sse;
            }
        }
        return toBeUpdated;
    }
}
