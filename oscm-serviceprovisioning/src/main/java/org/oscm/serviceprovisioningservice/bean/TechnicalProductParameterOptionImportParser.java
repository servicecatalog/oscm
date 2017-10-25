/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-2                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author baumann
 * 
 */
class TechnicalProductParameterOptionImportParser extends ImportParserBase {

    private final LocalizerServiceLocal localizer;

    private final ParameterDefinition parameterDef;
    private final String technicalProductId;
    private final List<ParameterOption> obsoleteOptions;
    private List<ParameterOption> existedOptions;
    private final Map<Long, List<VOLocalizedText>> obsoleteOptionDescriptions;

    private final Set<String> processedOptionIds = new HashSet<String>();
    private final Set<String> processedOptionLocales = new HashSet<String>();
    private ParameterOption parameterOption = null;

    public TechnicalProductParameterOptionImportParser(
            ParameterDefinition parameterDefinition, String techProductId,
            DataService dm, LocalizerServiceLocal localizer) {
        this.dm = dm;
        this.localizer = localizer;
        this.parameterDef = parameterDefinition;
        this.technicalProductId = techProductId;

        // Keep the options and localized option descriptions of the parameter,
        // that are currently stored in the database
        obsoleteOptions = new ArrayList<ParameterOption>(
                parameterDef.getOptionList());
        obsoleteOptionDescriptions = new HashMap<Long, List<VOLocalizedText>>();
        for (ParameterOption option : obsoleteOptions) {
            long optionKey = option.getKey();
            obsoleteOptionDescriptions.put(Long.valueOf(optionKey), localizer
                    .getLocalizedValues(optionKey,
                            LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
        }

        if (null != parameterDef.getOptionList()) {
            existedOptions = parameterDef.getOptionList();
        } else {
            existedOptions = new ArrayList<ParameterOption>();
        }
    }

    public boolean hasProcessed(String optionId) {
        return processedOptionIds.contains(optionId);
    }

    public boolean hasProcessedLocale(String locale) {
        return processedOptionLocales.contains(locale);
    }

    public String getCurrentOptionID() {
        if (parameterOption != null) {
            return parameterOption.getOptionId();
        } else {
            return null;
        }
    }

    /**
     * Check if there is already a parameter option with the specified ID. If
     * not, create a new parameter option.
     * 
     * @param optionID
     *            option identifier in the scope of Parameter definition
     * @return The parameter option.
     */
    public void getOrCreateOption(String optionID) {
        parameterOption = getOption(optionID);
        if (parameterOption == null) {
            parameterOption = new ParameterOption();
            parameterOption.setOptionId(optionID);
            parameterOption.setParameterDefinition(parameterDef);
            existedOptions.add(parameterOption);
            persist(parameterOption);
        }
    }

    private ParameterOption getOption(String optionID) {
        ParameterOption foundOption = null;
        for (ParameterOption option : existedOptions) {
            if (optionID.equals(option.getOptionId())) {
                foundOption = option;
                break;
            }
        }

        if (null != foundOption && obsoleteOptions.contains(foundOption)) {
            obsoleteOptions.remove(foundOption);
        }

        return foundOption;
    }

    /**
     * Store a parsed localized option
     * 
     * @param locale
     *            the locale
     * @param localizedText
     *            the localized text to store
     */
    public void processLocalizedOption(String locale, String localizedText) {
        List<VOLocalizedText> obsoleteLocalizedTexts = obsoleteOptionDescriptions
                .get(Long.valueOf(parameterOption.getKey()));

        // Check if the localized text is modified
        VOLocalizedText obsoleteLocalizedText = getLocalizedText(locale,
                obsoleteLocalizedTexts);
        if (obsoleteLocalizedText != null) {
            obsoleteLocalizedTexts.remove(obsoleteLocalizedText);
        }

        // Update the localized text or create a new localized resource
        localizer.storeLocalizedResource(locale, parameterOption.getKey(),
                LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC, localizedText);

        processedOptionLocales.add(locale);
    }

    private VOLocalizedText getLocalizedText(String locale,
            List<VOLocalizedText> localizedTexts) {
        if (localizedTexts != null) {
            for (VOLocalizedText lt : localizedTexts) {
                if (lt.getLocale().equals(locale)) {
                    return lt;
                }
            }
        }

        return null;
    }

    /**
     * Remove all obsolete localized option descriptions from the database if
     * all LocalizedOption Tag's have been processed
     * 
     * @throws UpdateConstraintException
     *             if the parameter definition is used by parameters of an
     *             undeleted product and some localized option descriptions are
     *             missed in the new XML file.
     */
    void cleanupObsoleteOptionDescriptions() throws UpdateConstraintException {
        List<VOLocalizedText> obsoleteLocalizedTexts = obsoleteOptionDescriptions
                .get(Long.valueOf(parameterOption.getKey()));

        if (obsoleteLocalizedTexts != null && obsoleteLocalizedTexts.size() > 0) {
            if (parameterDef.definesParametersOfUndeletedProduct()) {
                UpdateConstraintException e = new UpdateConstraintException(
                        ClassEnum.TECHNICAL_SERVICE, technicalProductId);
                throw e;
            } else {
                // Remove all obsolete localized resources from the database
                for (VOLocalizedText lt : obsoleteLocalizedTexts) {
                    localizer.removeLocalizedValue(parameterOption.getKey(),
                            LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC,
                            lt.getLocale());
                }

                obsoleteOptionDescriptions.remove(Long.valueOf(parameterOption
                        .getKey()));
            }
        }
    }

    /**
     * Finish the parsing of an option
     * 
     * @throws UpdateConstraintException
     */
    public void finishOption() throws UpdateConstraintException {
        cleanupObsoleteOptionDescriptions();
        processedOptionLocales.clear();
        processedOptionIds.add(parameterOption.getOptionId());
        parameterOption = null;
    }

    /**
     * Finish the parsing of the parameter options. All obsolete parameter
     * options are removed from the database.
     * 
     * @throws UpdateConstraintException
     *             if the parameter definition is used by parameters of an
     *             undeleted product and some option(s) are missed in the new
     *             XML file.
     * @throws ObjectNotFoundException
     */
    public void finishOptions() throws UpdateConstraintException {
        if (obsoleteOptions.size() > 0) {
            if (parameterDef.definesParametersOfUndeletedProduct()) {
                UpdateConstraintException e = new UpdateConstraintException(
                        ClassEnum.TECHNICAL_SERVICE, technicalProductId);
                throw e;
            } else {
                // Remove all obsolete options from the database
                for (ParameterOption option : obsoleteOptions) {
                    parameterDef.getOptionList().remove(option);
                    dm.remove(option);
                }
                dm.flush();

                obsoleteOptions.clear();
            }
        }
    }

    /**
     * Store the default option, if it's one of the processed options
     * 
     * @param defaultOptionID
     *            the ID of the default option
     * @return <code>false</code> if the default option was not processed;
     *         otherwise <code>true</code>
     */
    public boolean storeDefaultOption(String defaultOptionID) {
        if (defaultOptionID != null) {
            if (hasProcessed(defaultOptionID)) {
                parameterDef.setDefaultValue(defaultOptionID);
                dm.flush();
                dm.refresh(parameterDef);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

}
