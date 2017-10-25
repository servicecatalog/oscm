/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.dataservice.local.DataService;

/**
 * Base class for all converters. Converts in the init methods as much as
 * possible automatically, leaving open only the special layer cases.
 */
public abstract class AbstractVOConverter {

    protected final AbstractExceptionConverter exceptionConverter;
    protected final EnumConverter enumConverter;
    protected final VOCollectionConverter voCollectionConverter;
    protected final String version;
    protected final DataService ds;
    private static final String FUJITSU_PACKAGE_NAME = "org.oscm.";
    private static final String FUJITSU_INTERNAL_PACKAGE_NAME = FUJITSU_PACKAGE_NAME
            + "internal.";

    protected AbstractVOConverter(String version, DataService ds,
            AbstractExceptionConverter exceptionConverter) {
        this.version = version;
        this.ds = ds;
        enumConverter = new EnumConverter();
        voCollectionConverter = new VOCollectionConverter(version,
                enumConverter);
        this.exceptionConverter = exceptionConverter;
        exceptionConverter.enumConverter = enumConverter;
    }

    public AbstractExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Object oldVO, Class<T> type) {
        if (oldVO == null
                || type.getPackage().getName() == null
                || !type.getPackage().getName()
                        .startsWith(FUJITSU_PACKAGE_NAME)) {
            return (T) oldVO;
        }
        final T newVO;
        try {
            if (oldVO.getClass().isEnum()) {
                final Method m = enumConverter.getClass().getMethod("convert",
                        new Class[] { Enum.class, Class.class });
                return (T) m.invoke(enumConverter, oldVO, type);
            }
            newVO = type.newInstance();
            List<Method> methodsOldVO = new ArrayList<Method>(
                    Arrays.asList(oldVO.getClass().getMethods()));
            List<String> methodNamesNewVO = getMethodNames(newVO);
            for (Method method : methodsOldVO) {
                if (methodNamesNewVO.contains(method.getName())) {
                    setField(newVO, oldVO, method);
                }
            }
            cleanUp(oldVO, newVO);
            return newVO;
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    private void cleanUp(Object oldVO, Object newVO) {
        try {
            final Method m = getClass().getMethod("cleanUp", oldVO.getClass(),
                    newVO.getClass());
            m.invoke(this, oldVO, newVO);
        } catch (NoSuchMethodException ex) {
            // no clean up needed!
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> convertList(List<?> list, Class<T> type) {
        if (list == null) {
            return null;
        }
        final List<T> newList = new ArrayList<T>();
        try {
            for (Object item : list) {
                newList.add(convert(item, type));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return newList;
    }

    public <T> Set<T> convertSet(Set<?> set, Class<T> type) {
        if (set == null) {
            return null;
        }
        final Set<T> newSet = new HashSet<T>();
        try {
            for (Object item : set) {
                newSet.add(convert(item, type));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return newSet;

    }

    private boolean isGetter(Method method) {
        if (method.getName().equals("getClass")) {
            return false;
        }
        if (method.getParameterTypes().length > 0) {
            return false;
        }
        return method.getName().startsWith("get")
                || method.getName().startsWith("is");
    }

    private boolean isFujitsuPackage(Method method) {
        if (method.getReturnType().getPackage() == null) {
            return false;
        }
        return method.getReturnType().getPackage().getName()
                .startsWith(FUJITSU_PACKAGE_NAME);
    }

    private void setField(Object newVO, Object oldVO, Method method)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, SecurityException,
            ClassNotFoundException {
        if (isGetter(method)) {
            try {
                if (isFujitsuPackage(method)) {
                    setVOField(newVO, oldVO, method);
                } else if (List.class.isAssignableFrom(method.getReturnType())) {
                    setListField(newVO, oldVO, method);
                } else if (Set.class.isAssignableFrom(method.getReturnType())) {
                    setSetField(newVO, oldVO, method);
                } else {
                    setOtherField(newVO, oldVO, method);
                }
            } catch (NoSuchMethodException ex) {
                // method does not exist, it's new
            }
        }
    }

    private void setOtherField(Object newVO, Object oldVO, Method method)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        newVO.getClass().getMethod(getSetter(method), method.getReturnType())
                .invoke(newVO, method.invoke(oldVO));
    }

    private void setVOField(Object newVO, Object oldVO, Method method)
            throws ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        final Class<?> c = Class
                .forName(getNewClassName(method.getReturnType()));
        newVO.getClass().getMethod(getSetter(method), c)
                .invoke(newVO, convert(method.invoke(oldVO), c));
    }

    private void setSetField(Object newVO, Object oldVO, Method method)
            throws IllegalAccessException, InvocationTargetException,
            ClassNotFoundException, NoSuchMethodException {
        String className = method.toGenericString();
        className = className.substring(className.indexOf('<') + 1,
                className.indexOf('>'));
        final Set<?> set = convertSet((Set<?>) method.invoke(oldVO),
                Class.forName(getNewClassName(Class.forName(className))));
        newVO.getClass().getMethod(getSetter(method), method.getReturnType())
                .invoke(newVO, set);
    }

    private void setListField(Object newVO, Object oldVO, Method method)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException {
        String className = method.toGenericString();
        className = className.substring(className.indexOf('<') + 1,
                className.indexOf('>'));
        final List<?> list = convertList((List<?>) method.invoke(oldVO),
                Class.forName(getNewClassName(Class.forName(className))));
        newVO.getClass().getMethod(getSetter(method), method.getReturnType())
                .invoke(newVO, list);
    }

    private String getSetter(Method method) {
        return "set"
                + method.getName().substring(
                        method.getName().startsWith("is") ? 2 : 3);
    }

    private String getNewClassName(Class<?> oldClass) {
        String packageName = oldClass.getPackage().getName();
        if (packageName.startsWith(FUJITSU_PACKAGE_NAME)) {
            if (packageName.endsWith(".v" + version)) {
                packageName = FUJITSU_INTERNAL_PACKAGE_NAME
                        + packageName.substring(FUJITSU_PACKAGE_NAME.length(),
                                packageName.lastIndexOf('.'));
            } else {
                packageName = FUJITSU_PACKAGE_NAME
                        + packageName.substring(FUJITSU_INTERNAL_PACKAGE_NAME
                                .length()) + ".v" + version;
            }
            return packageName + '.' + oldClass.getSimpleName();
        }
        return oldClass.getName();
    }

    <T> List<String> getMethodNames(T vo) {
        List<Method> methods = new ArrayList<Method>(Arrays.asList(vo
                .getClass().getMethods()));
        List<String> list = new ArrayList<>();
        for (Method m : methods) {
            list.add(m.getName());
        }
        return list;
    }
}
