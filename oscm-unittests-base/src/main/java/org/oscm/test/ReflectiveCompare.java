/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.PersistenceReflection;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author schmid
 * 
 */
public class ReflectiveCompare {

    private static final boolean DEBUG = true;
    private static final String NEWLINE = "\n";

    public static boolean compare(DomainObject<?> first, DomainObject<?> second) {
        return (null == doCompare(first, second));
    }

    public static String showDiffs(DomainObject<?> first, DomainObject<?> second) {
        return doCompare(first, second);
    }

    public static String showDiffs(DomainObject<?> first,
            DomainHistoryObject<?> second) {
        return doCompare(first, second);
    }

    public static boolean compare(DomainObject<?> first,
            DomainHistoryObject<?> second) {
        return (null == doCompare(first, second));
    }

    private static String doCompare(DomainObject<?> first,
            DomainHistoryObject<?> second) {
        // Only compare data containers
        final String container = doCompare(first.getDataContainer(),
                second.getDataContainer());
        if (container == null) {
            return null;
        }
        final String header = "Comparison " + first + " - " + second;
        return header + NEWLINE + container;
    }

    private static String doCompare(DomainObject<?> first,
            DomainObject<?> second) {
        final String header = "Comparison " + first + " - " + second;
        if (first == second) {
            return null;
        }
        if (first == null || second == null) {
            return header;
        }
        if (DomainObject.getDomainClass(first) != DomainObject
                .getDomainClass(second)) {
            return header;
        }
        // Only compare data containers
        String container = doCompare(first.getDataContainer(), second);
        // If object is a InheritedDomainObject, we also have to inspect the
        // parentContainer
        if (container == null) {
            return null;
        }
        return header + NEWLINE + container;
    }

    private static String doCompare(DomainDataContainer first, Object second) {
        // reflective compare with getter-Methods
        String result = "";
        if (first == null || second == null)
            return "DataContainer: first=(" + first + "), second=(" + second
                    + ")";
        for (Method m : first.getClass().getMethods()) {
            if ((!m.getName().startsWith("get") && !m.getName()
                    .startsWith("is"))
                    || "getClass".equals(m.getName())
                    || m.getGenericParameterTypes().length > 0) {
                continue;
            }
            // If the return type of a field is Date, we have to take a look
            // to the @Temporal-annotation in order to find the date
            // precision to compare. The annotation may be found on the
            // corresponding
            // field or the getter method itself
            boolean isTemporal = Date.class.isAssignableFrom(m.getReturnType());
            TemporalType temptype = null;
            if (isTemporal) {
                String fieldName = PersistenceReflection.getFieldName(m
                        .getName());
                try {
                    Field field = first.getClass().getDeclaredField(fieldName);
                    Annotation ann = field.getAnnotation(Temporal.class);
                    if (ann != null) {
                        temptype = ((Temporal) ann).value();
                    }
                } catch (Exception e1) {
                    // ignore, go on with getter method
                    e1.printStackTrace();
                }
                if (temptype == null) {
                    // also look at the getter method
                    Annotation ann = m.getAnnotation(Temporal.class);
                    if (ann != null) {
                        temptype = ((Temporal) ann).value();
                    }
                }
            }
            try {
                Object firstObj = m.invoke(first, (Object[]) null);
                Object secondObj = second.getClass()
                        .getMethod(m.getName(), (Class[]) null)
                        .invoke(second, (Object[]) null);
                if (DEBUG) {
                    System.out.println("--> Compare "
                            + obj2String(m.getName(), firstObj, secondObj));
                }
                if (firstObj == null) {
                    if (secondObj == null)
                        continue;
                    // only one object null => not equal
                    result = result
                            + obj2String(m.getName(), firstObj, secondObj)
                            + NEWLINE;
                } else if ((firstObj instanceof byte[])) {
                    if (!Arrays.equals((byte[]) firstObj, (byte[]) secondObj)) {
                        result = result
                                + obj2String(m.getName(), firstObj, secondObj)
                                + NEWLINE;
                    }
                } else {
                    // only one object null => not equal
                    if (secondObj == null) {
                        result = result
                                + obj2String(m.getName(), firstObj, secondObj)
                                + NEWLINE;
                    } else if (isTemporal) {
                        // Inspect Date-Types: only compare on base of @Temporal
                        // annotation
                        boolean isIdentical = false;
                        java.util.Date firstDate = (java.util.Date) firstObj;
                        java.util.Date secondDate = (java.util.Date) secondObj;
                        String out1, out2;
                        if (temptype == TemporalType.DATE) {
                            java.sql.Date date1 = new java.sql.Date(
                                    firstDate.getTime());
                            java.sql.Date date2 = new java.sql.Date(
                                    secondDate.getTime());
                            out1 = new SimpleDateFormat("dd/MM/yyyy")
                                    .format(date1);
                            out2 = new SimpleDateFormat("dd/MM/yyyy")
                                    .format(date2);
                            isIdentical = out1.equals(out2);
                        } else if (temptype == TemporalType.TIME) {
                            java.sql.Time date1 = new java.sql.Time(
                                    firstDate.getTime());
                            java.sql.Time date2 = new java.sql.Time(
                                    secondDate.getTime());
                            out1 = date1.toString();
                            out2 = date2.toString();
                            isIdentical = date1.equals(date2);
                        } else {
                            java.sql.Timestamp date1 = new java.sql.Timestamp(
                                    firstDate.getTime());
                            java.sql.Timestamp date2 = new java.sql.Timestamp(
                                    secondDate.getTime());
                            out1 = date1.toString();
                            out2 = date2.toString();
                            isIdentical = date1.equals(date2);
                        }
                        if (!isIdentical) {
                            result = result
                                    + obj2String(m.getName(), out1, out2)
                                    + NEWLINE;
                        }
                    } else if (!firstObj.equals(secondObj)) {
                        result = result
                                + obj2String(m.getName(), firstObj, secondObj)
                                + NEWLINE;
                    }
                }
            } catch (Exception e) {
                throw new SaaSSystemException(
                        "Exception in compare DomainDataContainer", e);
            }
        }
        if ("".equals(result))
            return null;
        return result;
    }

    private static String obj2String(String name, Object firstObj,
            Object secondObj) {
        return name + ": (" + firstObj + ") <-> (" + secondObj + ")";
    }
}
