/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.io.Externalizable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remote;

import org.oscm.test.EJBTestBase;

/**
 * Mapping from interfaces to arbitrary Values.
 * 
 * @author hoffmann
 */
class InterfaceMap<V> {

    private final Map<Class<?>, V> map = new HashMap<Class<?>, V>();

    /**
     * Adds the given Object with all its implementing interfaces to the
     * mapping.
     * 
     * @param type
     * @throws IllegalStateException
     *             if another instance already implements the same interface
     */
    public void put(Object type, V value) {
        putClass(type.getClass(), value);
    }

    private void putClass(Class<?> clazz, V instance) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (considerInterface(i)) {
                put(i, instance);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            putClass(superclass, instance);
        }
        put(clazz, instance);
    }

    private boolean considerInterface(Class<?> i) {
        if (i.getAnnotation(Local.class) != null) {
            return true;
        } else if (i.getAnnotation(Remote.class) != null) {
            return true;
        } else {
            return isExcplicitlyRequiredClass(i);
        }
    }

    private boolean isExcplicitlyRequiredClass(Class<?> i) {
        Set<Class<?>> requiredClasses = new HashSet<Class<?>>();
        requiredClasses.add(EJBTestBase.Caller.class);
        requiredClasses.add(Comparable.class);
        requiredClasses.add(Runnable.class);
        requiredClasses.add(Externalizable.class);
        requiredClasses.add(javax.enterprise.event.Event.class);
        requiredClasses.add(org.slf4j.Logger.class);
        return requiredClasses.contains(i);
    }

    public void put(Class<?> beanInterface, V value) {
        map.put(beanInterface, value);
    }

    /**
     * Looks up the implementation for the interface with the given type.
     * 
     * @param <T>
     *            interface type
     * @param beanInterface
     *            interface
     * @return implementation
     * @throws NoSuchElementException
     *             thrown if no instance implements the given interface
     */
    public V get(Class<?> beanInterface) throws NoSuchElementException {
        final V instance = map.get(beanInterface);
        if (instance == null) {
            throw new NoSuchElementException("No implementation for interface "
                    + beanInterface.getName());
        }
        return instance;
    }
}
