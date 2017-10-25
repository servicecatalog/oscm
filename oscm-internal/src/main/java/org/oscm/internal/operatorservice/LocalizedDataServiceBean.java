/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.11.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;
import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PropertiesImportException;

/**
 * EJB implementation of Localized Data TLS.
 * 
 * @author goebel
 * 
 */
@Stateless
@Remote(LocalizedDataService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class LocalizedDataServiceBean implements LocalizedDataService {

    @Inject
    OperatorServiceLocalBean operatorService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @Override
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public String importProperties(List<POLocalizedData> localizedData,
            String languageCode) throws OperationNotPermittedException,
            PropertiesImportException, ObjectNotFoundException {
        ArgumentValidator.notNull("localizedData", localizedData);
        ArgumentValidator.notNull("languageCode", languageCode);
        List<Map<String, Properties>> propertiesMaps = new ArrayList<Map<String, Properties>>();
        for (POLocalizedData data : localizedData) {
            ArgumentValidator.notNull("properties", data.getPropertiesMap());
            ArgumentValidator.notNull("propertiesType", data.getType());
            operatorService.saveProperties(data.getPropertiesMap(),
                    languageCode, data.getType());
            propertiesMaps.add(data.getPropertiesMap());
        }
        return operatorService.checkAreAllItemsTranslated(propertiesMaps,
                languageCode);

    }

    @Override
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public List<POLocalizedData> exportProperties(String languageCode)
            throws ObjectNotFoundException {
        List<POLocalizedData> dataList = new ArrayList<POLocalizedData>();

        Map<String, Properties> messagePropertiesMap = operatorService
                .loadMessageProperties(languageCode);
        POLocalizedData messageData = toPOLocalizedData(messagePropertiesMap,
                LocalizedDataType.MessageProperties);
        Map<String, Properties> mailPropertiesMap = operatorService
                .loadMailProperties(languageCode);
        POLocalizedData mailData = toPOLocalizedData(mailPropertiesMap,
                LocalizedDataType.MailProperties);
        Map<String, Properties> platformObjectsMap = operatorService
                .loadPlatformObjects(languageCode);
        POLocalizedData platformObjectsData = toPOLocalizedData(
                platformObjectsMap, LocalizedDataType.PlatformObjects);

        dataList.add(messageData);
        dataList.add(mailData);
        dataList.add(platformObjectsData);
        return dataList;
    }

    POLocalizedData toPOLocalizedData(Map<String, Properties> propertiesMap,
            LocalizedDataType type) {
        POLocalizedData data = new POLocalizedData();
        data.setType(type);
        data.setPropertiesMap(propertiesMap);
        return data;
    }

    @Override
    public Properties loadMessageProperties(String languageCode) {
        ArgumentValidator.notNull("languageCode", languageCode);
        Properties properties = operatorService
                .loadPropertiesFromDB(languageCode);
        return properties;
    }

    @Override
    public Properties loadMailPropertiesFromFile(String languageCode) {
        ArgumentValidator.notNull("languageCode", languageCode);
        return localizer.loadLocalizedPropertiesFromFile(
                LocalizedObjectTypes.MAIL_CONTENT.getSourceLocation(),
                languageCode);
    }

    @Override
    public Properties loadPlatformObjectsFromFile(String languageCode) {
        ArgumentValidator.notNull("languageCode", languageCode);
        return operatorService.loadPlatformObjectsFromFile(languageCode);
    }
}
