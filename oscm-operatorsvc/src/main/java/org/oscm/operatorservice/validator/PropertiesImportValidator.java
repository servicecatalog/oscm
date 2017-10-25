/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.validator;

import java.util.List;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.PropertiesImportException;

/**
 * Validator for import localized properties.
 * 
 * @author Gao
 */
public class PropertiesImportValidator {

    private static Log4jLogger logger = LoggerFactory
            .getLogger(PropertiesImportValidator.class);

    DataService ds;

    /**
     * Constructor of PropertiesImportValidator
     * 
     * @param ds
     */
    public PropertiesImportValidator(DataService ds) {
        this.ds = ds;
    }

    /**
     * check the imported language is not null
     * 
     * @param languageCode
     *            language code of imported language, extract from excel
     * @throws PropertiesImportException
     * 
     */
    public void checkLanguageCodeNotNull(String languageCode)
            throws PropertiesImportException {
        if (languageCode == null || languageCode.trim().length() == 0) {
            PropertiesImportException propertiesImportException = new PropertiesImportException(
                    PropertiesImportException.Reason.NONE_LANGUAGE_CODE);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    propertiesImportException,
                    LogMessageIdentifier.WARN_NO_LANGUAGE_CODE_FOR_IMPORT_PROPERTIES);
            throw propertiesImportException;
        }
    }

    /**
     * check the imported language is supported in system
     * 
     * @param languageCode
     *            language code of imported language, extract from excel
     * @throws PropertiesImportException
     * 
     */
    public void checkLanguageSupported(String languageCode)
            throws PropertiesImportException {
        Query q = ds.createNamedQuery("SupportedLanguage.findAll");
        List<SupportedLanguage> languageList = ParameterizedTypes.list(
                q.getResultList(), SupportedLanguage.class);
        for (SupportedLanguage language : languageList) {
            if (language.getLanguageISOCode().equalsIgnoreCase(languageCode)) {
                return;
            }
        }
        PropertiesImportException propertiesImportException = new PropertiesImportException(
                PropertiesImportException.Reason.LANGUAGE_NOT_SUPPORTED);
        logger.logWarn(
                Log4jLogger.SYSTEM_LOG,
                propertiesImportException,
                LogMessageIdentifier.WARN_LANGUAGE_NOT_SUPPORTED_FOR_IMPORT_PROPERTIES);
        throw propertiesImportException;
    }
}
