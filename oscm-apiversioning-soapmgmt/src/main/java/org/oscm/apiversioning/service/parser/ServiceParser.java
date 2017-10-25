/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年2月3日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.service.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author gaowenxin
 * 
 */
public class ServiceParser {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(ServiceParser.class);

    public static Map<String, Class<?>> getParametersForMethod(
            String serviceName, String methodName) {
        Map<String, Class<?>> parameterMap = new HashMap<String, Class<?>>();
        try {
            Class<?> service = Class.forName(serviceName);
            Method method = getTargetMethodByName(service, methodName);
            if (method != null) {
                Class<?>[] parameterTypes = getParameterTypes(method);
                List<String> parameterNames = getParameterNames(method);
                if (parameterNames.size() == parameterTypes.length) {
                    for (int i = 0; i < parameterNames.size(); i++) {
                        parameterMap.put(parameterNames.get(i),
                                parameterTypes[i]);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_SERVICE_NOT_FOUND, serviceName);
        }
        return parameterMap;
    }

    private static Method getTargetMethodByName(Class<?> service,
            String methodName) {
        Method[] methods = service.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private static List<String> getParameterNames(Method method) {
        List<String> parameterNames = new ArrayList<String>();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] annotation = annotations[i];
            for (Annotation a : annotation) {
                parameterNames.add(((WebParam) a).name());
            }
        }
        return parameterNames;
    }

    private static Class<?>[] getParameterTypes(Method method) {
        return method.getParameterTypes();
    }
}
