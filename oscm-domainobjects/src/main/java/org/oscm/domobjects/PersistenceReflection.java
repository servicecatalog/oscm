/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                   
 *                                                                              
 *  Creation Date: 28.01.2009                                                      
 *                                                                              
 *  Completion Time:                     
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Helper class to inspect persistence structure of DomainObject classes
 * 
 * @author schmid
 */
public class PersistenceReflection {

    /**
     * Returns the name of the getter method for a given field name (following
     * JavaBeans notation)
     * 
     * @param fieldname
     * @return
     */
    public static String getGetterName(String fieldname) {
        return "get" + fieldname.substring(0, 1).toUpperCase()
                + fieldname.substring(1);
    }

    /**
     * Returns the field name of a given getter method name (following JavaBeans
     * notation)
     * 
     * @param gettername
     * @return
     */
    public static String getFieldName(String gettername) {
        if (!gettername.startsWith("get"))
            return null;
        String fieldname = gettername.substring(3);
        return fieldname.substring(0, 1).toLowerCase() + fieldname.substring(1);
    }

    /**
     * Returns the attributes of a DomainObject class business key
     * 
     * @param clazz
     * @return array of business key attribute names
     */
    public static String[] getBusinessKey(Class<?> clazz) {
        BusinessKey ann = clazz.getAnnotation(BusinessKey.class);
        if (ann == null) {
            return null;
        }
        return ann.attributes();
    }

    /**
     * Returns the value of a given field for a given DomainObject instance by
     * invoking it's getter method. Throws a SaaSSystemException if the method
     * cannot be invoked or another exception occurs.
     * 
     * @param idobj
     *            the DomainObject instance
     * @param par
     *            the field name to be evaluated
     * @return the result of the invocation of the getter method
     */
    public static Object getValue(DomainObject<?> idobj, String par) {
        String getterName = getGetterName(par);
        try {
            Method m = idobj.getClass()
                    .getMethod(getterName, (Class<?>[]) null);
            Object res = m.invoke(idobj, (Object[]) null);
            return res;
        } catch (Exception e) {
            throw new SaaSSystemException(
                    "Exception in PersistenceReflection.getValue(" + idobj
                            + "," + par + ")", e);
        }
    }

    /**
     * Returns a String representing the business key value of a given
     * DomainObject
     * 
     * @param obj
     * @return
     */
    public static Map<String, String> getBusinessKeys(DomainObject<?> obj) {
        String[] keyAttributes = getBusinessKey(obj.getClass());
        if (keyAttributes == null) {
            return null;
        }
        HashMap<String, String> businessKeyMap = new HashMap<String, String>();
        for (String attName : keyAttributes) {
            String toSting = null;
            Object value = getValue(obj, attName);
            if (value != null) {
                toSting = value.toString();
            }
            businessKeyMap.put(attName, toSting);
        }
        return businessKeyMap;
    }

    /**
     * Returns the non-qualified name of a DomainObject class (without package
     * information)
     * 
     * @param obj
     * @return
     */
    public static String getDomainClassName(DomainObject<?> obj) {
        String className = obj.getClass().getName();
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
