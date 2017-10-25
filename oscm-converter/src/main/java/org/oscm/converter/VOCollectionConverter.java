/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

class VOCollectionConverter {
    private final Log4jLogger LOGGER = LoggerFactory
            .getLogger(VOCollectionConverter.class);
    protected String METHOD_CONVERT_UP = "convertToUp";
    protected String METHOD_CONVERT_DOWN;
    protected String VERSION;
    protected final EnumConverter enumConverter;

    public VOCollectionConverter(String version, EnumConverter enumConverter) {
        VERSION = "v" + version;
        this.enumConverter = enumConverter;
    }

    @SuppressWarnings("unchecked")
    public <T, U> T reflectiveConvert(U oldObj) {

        String methodName = "";
        if (oldObj.getClass().getPackage().getName().contains(VERSION)) {
            methodName = METHOD_CONVERT_UP;
        } else {
            methodName = METHOD_CONVERT_DOWN;
        }

        Method m;
        try {
            m = AbstractVOConverter.class.getDeclaredMethod(methodName,
                    oldObj.getClass());
            return (T) m.invoke(AbstractVOConverter.class, oldObj);
        } catch (Exception e) {
            SaaSSystemException exc = new SaaSSystemException(e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, exc,
                    LogMessageIdentifier.ERROR_VO_CONVERSION_1_6);
            throw exc;
        }
    }

    public <T, S> List<T> convertList(List<S> sourceList, Class<T> targetType) {

        if (sourceList == null) {
            return null;
        }

        List<T> targetList = new ArrayList<T>();
        for (S sourceVo : sourceList) {
            T targetVo = targetType.cast(reflectiveConvert(sourceVo));
            if (targetVo != null) {
                targetList.add(targetVo);
            }
        }

        return targetList;
    }

    public <T, S> Set<T> convertSet(Set<S> sourceSet, Class<T> targetType) {

        if (sourceSet == null) {
            return null;
        }

        Set<T> targetSet = new HashSet<T>();
        for (S sourceVo : sourceSet) {
            T targetVo = targetType.cast(reflectiveConvert(sourceVo));
            if (targetVo != null) {
                targetSet.add(targetVo);
            }
        }

        return targetSet;
    }
}
