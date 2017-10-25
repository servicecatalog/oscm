/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.11.2013
 *  
 *  Author cmin
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.operatorservice.POSupportedLanguage;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Assembler to convert the POSupportedLanguage to the according domain object
 * and vice versa.
 * 
 * @author cmin
 */
public class SupportedLanguageAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SupportedLanguageAssembler.class);

    /**
     * Creates new POSupportedLanguage objects and fills the fields with the
     * corresponding data from the domain objects.
     * 
     * @param languages
     *            The SupportedLanguage to be returned in PO representation.
     * @return The po representation of the domain objects.
     */
    public static List<POSupportedLanguage> toPOLanguages(
            List<SupportedLanguage> languages, Locale currentUserLocale) {
        if (languages == null || languages.isEmpty()) {
            return null;
        }
        List<POSupportedLanguage> result = new ArrayList<POSupportedLanguage>();
        for (SupportedLanguage supportedLanguage : languages) {
            result.add(toPOLanguage(supportedLanguage, currentUserLocale));
        }
        return result;
    }

    /**
     * Create a new POSupportedLanguage object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param language
     *            The domain object containing the values to be set.
     * @return The created po or null if the domain object was null.
     */
    public static POSupportedLanguage toPOLanguage(SupportedLanguage language,
            Locale currentUserLocale) {
        if (language == null || language.getLanguageISOCode() == null) {
            return null;
        }
        POSupportedLanguage poLanguage = new POSupportedLanguage();
        fillPOLanguage(poLanguage, language, currentUserLocale);
        return poLanguage;
    }

    /**
     * Create SupportedLanguage objects and fills the fields with the
     * corresponding data from the PO.
     * 
     * @param polanguages
     *            The SupportedLanguage to be returned in value object
     *            representation.
     * @return the domain objects.
     */
    public static List<SupportedLanguage> toLanguages(
            List<POSupportedLanguage> poLanguages) {
        if (poLanguages == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Parameter must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }
        List<SupportedLanguage> result = new ArrayList<SupportedLanguage>();
        for (POSupportedLanguage poLanguage : poLanguages) {
            result.add(toLanguage(poLanguage));
        }
        return result;
    }

    /**
     * Create a SupportedLanguage object and fills the fields with the
     * corresponding data from the PO.
     * 
     * @param poLanguage
     *            The PO converted to DO.
     * @return The domain object.
     */
    public static SupportedLanguage toLanguage(POSupportedLanguage poLanguage) {
        if (poLanguage == null || poLanguage.getLanguageISOCode() == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "POSupportedLanguage and Language ISO code must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }
        SupportedLanguage language = new SupportedLanguage();
        copyAttributes(language, poLanguage);
        return language;
    }

    private static void fillPOLanguage(POSupportedLanguage poLanguage,
            SupportedLanguage language, Locale currentUserLocale) {
        poLanguage.setKey(language.getKey());
        poLanguage.setLanguageISOCode(language.getLanguageISOCode());
        poLanguage.setActive(language.getActiveStatus());
        poLanguage.setDefaultLanguageStatus(language.getDefaultStatus());

        Locale locale = new Locale(language.getLanguageISOCode());
        String languageName = locale.getDisplayLanguage(currentUserLocale);
        poLanguage.setLanguageName(languageName);
    }

    private static void copyAttributes(SupportedLanguage language,
            POSupportedLanguage poLanguage) {
        language.setKey(poLanguage.getKey());
        language.setLanguageISOCode(poLanguage.getLanguageISOCode());
        language.setActiveStatus(poLanguage.isActive());
        language.setDefaultStatus(poLanguage.isDefaultLanguageStatus());
    }
}
