/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Reference of a session bean to other objects.
 * 
 * @author hoffmann
 */
final class Reference {

    private final Class<?> type;

    private final String name;

    private final Field field;

    private final Method method;

    private Reference(Class<?> type, String name, Field field, Method method) {
        this.type = type;
        this.name = name;
        this.field = field;
        this.method = method;
    }

    public Reference(Class<?> type, String name) {
        this(type, name, null, null);
    }

    public Reference(Class<?> type, String name, Field field) {
        this(type, name, field, null);
    }

    public Reference(Class<?> type, String name, Method method) {
        this(type, name, null, method);
    }

    /**
     * Expected interface or class of the target object.
     * 
     * @return
     */
    public Class<?> getInterfaceOrClass() {
        return type;
    }

    /**
     * @return logical name of the EJB reference within the declaring
     *         component's (e.g., java:comp/env) environment
     */
    public String getName() {
        return name;
    }

    /**
     * Injects the given value if the reference is declared with a field or
     * setter method.
     * 
     * @param target
     * @param value
     * @throws Exception
     */
    public void inject(Object target, Object value) throws Exception {
        if (field != null) {
            field.setAccessible(true);
            field.set(target, value);
        }
        if (method != null) {
            method.setAccessible(true);
            method.invoke(target, value);
        }
    }

    public static Reference createFor(EJB ejb, Field field) {
        final Class<?> type;
        if (!Object.class.equals(ejb.beanInterface())) {
            type = ejb.beanInterface();
        } else {
            type = field.getType();
        }
        final String name;
        if (ejb.name().length() > 0) {
            name = ejb.name();
        } else {
            name = field.getDeclaringClass().getName() + "/" + field.getName();
        }
        return new Reference(type, name, field);
    }

    public static Reference createFor(Resource resource, Field field) {
        final Class<?> type;
        if (!Object.class.equals(resource.type())) {
            type = resource.type();
        } else {
            type = field.getType();
        }
        final String name;
        if (resource.name().length() > 0) {
            name = resource.name();
        } else {
            name = field.getDeclaringClass().getName() + "/" + field.getName();
        }
        return new Reference(type, name, field);
    }

    public static Reference createFor(PersistenceContext persistenceContext,
            Field field) {
        final String name;
        if (persistenceContext.name().length() > 0) {
            name = persistenceContext.name();
        } else {
            name = field.getDeclaringClass().getName() + "/" + field.getName();
        }
        return new Reference(EntityManager.class, name, field);
    }

    public static Reference createFor(Field field) {
        final String name = field.getDeclaringClass().getName() + "/"
                + field.getName();
        return new Reference(field.getType(), name, field);
    }
}
