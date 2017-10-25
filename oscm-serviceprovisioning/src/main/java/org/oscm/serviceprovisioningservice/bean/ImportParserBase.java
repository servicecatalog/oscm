/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import org.xml.sax.helpers.DefaultHandler;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.PersistenceReflection;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * This is a super class for SAX based parsers providing general functionality
 * for parsing XML definition files used in BES.
 * 
 * @author goebel
 * 
 */
abstract class ImportParserBase extends DefaultHandler {
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TechnicalProductImportParser.class);

    protected DataService dm;

    /**
     * Persists an object which has no business key. If we catch an
     * SaasNonUniqueBusinessKeyException we convert it into a
     * SaaSSystemException.
     * 
     * @param obj
     *            the object to persist
     */
    protected void persist(DomainObject<?> obj) {
        try {
            dm.persist(obj);
            dm.flush();
            dm.refresh(obj);
        } catch (NonUniqueBusinessKeyException e) {
            String domainClassName = PersistenceReflection
                    .getDomainClassName(obj);
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_NOT_UNIQUE_BUSINESS_KEY,
                    domainClassName);
            throw se;
        }
    }
}
