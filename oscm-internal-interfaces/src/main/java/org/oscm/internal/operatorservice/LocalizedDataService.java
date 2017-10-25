/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.11.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import java.util.List;
import java.util.Properties;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PropertiesImportException;

/**
 * Remote interface for LocalizedData TLS.
 * 
 * @author goebel
 */
@Remote
public interface LocalizedDataService {

    /**
     * Import properties of localized data into the database.
     * 
     * @param propertiesMap
     *            - map of properties, key is the language code, value is
     *            properties
     * @param languageCode
     *            - the language code for import
     * @return - an outcome with <code>null</code> if the operation succeeded
     *         without warning. In case if operation succeeded with warning, a
     *         message key of that warning is returned. Only if the operation is
     *         failed with a serve error an according exception is thrown
     * @throws OperationNotPermittedException
     *             - Only operator allowed to import properties
     * @throws ObjectNotFoundException
     *             - throws when there is no default language in system
     * @throws PropertiesImportException
     *             - exception for import problem
     */
    public String importProperties(List<POLocalizedData> data,
            String languageCode) throws OperationNotPermittedException,
            PropertiesImportException, ObjectNotFoundException;

    /**
     * export properties from system
     * 
     * @param languageCode
     *            - which language's properties need to be export
     * @throws ObjectNotFoundException
     *             - throws if there is no default language in system
     */
    public List<POLocalizedData> exportProperties(String languageCode)
            throws ObjectNotFoundException;

    /**
     * get all the localized properties
     * 
     * @param languageCode
     *            - language code
     * @return
     */
    public Properties loadMessageProperties(String languageCode);

    /**
     * get mail properties properties from file
     * 
     * @param languageCode
     * @return Properties of mail
     */
    public Properties loadMailPropertiesFromFile(String languageCode);

    /**
     * get platform object properties from file
     * 
     * @param languageCode
     * @return Properties of PlatformObject
     */
    public Properties loadPlatformObjectsFromFile(String languageCode);

}
