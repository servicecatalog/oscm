/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 24.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.DomainObject;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.BaseVO;

/**
 * Base assembler class that takes care that the version information is set when
 * transforming value object into domain objects and vice versa.
 * 
 * <p>
 * All subclasses should call the methods to update the domain or value objects
 * provided by this superclass.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BaseAssembler {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(BaseAssembler.class);

    /**
     * Updates the artificial object id and the current object version in the
     * value object according to the values in the domain object.
     * 
     * @param voToBeUpdated
     *            The value object to be modified.
     * @param template
     *            The domain object serving as template.
     * @return The modified value object.
     */
    protected static BaseVO updateValueObject(BaseVO voToBeUpdated,
            DomainObject<?> template) {
        voToBeUpdated.setKey(template.getKey());
        voToBeUpdated.setVersion(template.getVersion());
        return voToBeUpdated;
    }

    /**
     * Verifies that the domain object does not have a newer version than the VO
     * and that the objects have corresponding keys.
     * 
     * @param toBeUpdated
     *            The domain object to be modified.
     * @param template
     *            The value object serving as template.
     * @return The modified domain object.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static DomainObject<?> verifyVersionAndKey(
            DomainObject<?> toBeUpdated, BaseVO template)
            throws ConcurrentModificationException {
        if (toBeUpdated.getVersion() > template.getVersion()) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    template);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION, template
                            .getClass().getSimpleName());
            throw cme;
        }
        if (toBeUpdated.getKey() != template.getKey()) {
            if (template.getKey() == 0) {
                // on save operations where create and update can be done, a new
                // VO may be passed which has already been created as DO and
                // read from e.g. a list of existing ones (and not by
                // getReference(long key))
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        template);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                        LogMessageIdentifier.WARN_OBJECT_CREATED_CONCURRENTLY,
                        template.getClass().getSimpleName());
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

    /**
     * Trims the given string.
     * 
     * @param string
     *            string to trim or <code>null</code>
     * @return trimmed string or <code>null</code> if the input was
     *         <code>null</code>
     */
    public static String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

}
