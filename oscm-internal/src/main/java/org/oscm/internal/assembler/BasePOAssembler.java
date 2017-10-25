/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.DomainObject;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Base assembler class that takes care that the version information is set when
 * transforming presentation object into domain objects and vice versa.
 * 
 * <p>
 * All subclasses should call the methods to update the domain or presentation
 * objects provided by this superclass.
 * </p>
 * 
 * @author Gao
 * 
 */
public class BasePOAssembler {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(BaseAssembler.class);

    /**
     * Updates the artificial object id and the current object version in the
     * presentation object according to the values in the domain object.
     * 
     * @param poToBeUpdated
     *            The presentation object to be modified.
     * @param template
     *            The domain object serving as template.
     * @return The modified presentation object.
     */
    protected static BasePO updatePresentationObject(BasePO poToBeUpdated,
            DomainObject<?> template) {
        poToBeUpdated.setKey(template.getKey());
        poToBeUpdated.setVersion(template.getVersion());
        return poToBeUpdated;
    }

    /**
     * Verifies that the domain object does not have a newer version than the PO
     * and that the objects have corresponding keys.
     * 
     * @param toBeUpdated
     *            The domain object to be modified.
     * @param template
     *            The presentation object serving as template.
     * @return The modified domain object.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static DomainObject<?> verifyVersionAndKey(
            DomainObject<?> toBeUpdated, BasePO template)
            throws ConcurrentModificationException {
        if (toBeUpdated.getVersion() > template.getVersion()) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    template.getClass().getSimpleName(), template.getVersion());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION, template
                            .getClass().getSimpleName());
            throw cme;
        }
        if (toBeUpdated.getKey() != template.getKey()) {
            if (template.getKey() == 0) {
                // on save operations where create and update can be done, a new
                // PO may be passed which has already been created as DO and
                // read from e.g. a list of existing ones (and not by
                // getReference(long key))
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        template.getClass().getSimpleName(),
                        template.getVersion());
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
