/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.05.2013
 *  
 *  Author cmin
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Remote interface for SupportedLanguageData TLS.
 * 
 * @author cmin
 */
@Remote
public interface ManageLanguageService {
    /**
     * Get ISO code for default language
     * 
     * @return default language ISO code
     */
    public String getDefaultLanguage() throws ObjectNotFoundException;

    /**
     * get all or active languages
     * 
     * @param isOnlyActive
     *            if true, only find all active language, if false, get all
     *            supported languages
     * @return POSupportLanguage list
     */
    public List<POSupportedLanguage> getLanguages(boolean isOnlyActive);

    /**
     * save existed supported language and persist new supported language
     * 
     * @param languages
     * @throws IllegalArgumentException
     * @throws ValidationException
     */
    public void saveLanguages(List<POSupportedLanguage> languages)
            throws IllegalArgumentException, ValidationException;
}
