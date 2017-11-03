/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.03.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.lang.reflect.Modifier;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Class for filtering the results of the package class reader.
 * 
 * @author Mike J&auml;ger
 * 
 */
public abstract class ClassFilter {

    /**
     * Default filter to ignore interfaces, enumerations and abstract classes.
     */
    public static final ClassFilter CLASSES_ONLY = new ClassFilter() {
        @Override
        public boolean isNeglectableClass(Class<?> clazz) {
            return clazz.isEnum() || clazz.isInterface()
                    || Modifier.isAbstract(clazz.getModifiers());
        }
    };
    /**
     * Default filter to only accept stateless session beans.
     */
    public static final ClassFilter STATELESS_BEANS_ONLY = new ClassFilter() {
        @Override
        public boolean isNeglectableClass(Class<?> clazz) {
            boolean isNoClass = clazz.isEnum() || clazz.isInterface()
                    || Modifier.isAbstract(clazz.getModifiers());
            Stateless annotation = clazz.getAnnotation(Stateless.class);
            return !(annotation != null && !isNoClass);
        }
    };

    /**
     * Default filter to only accept local interfaces.
     */
    public static final ClassFilter LOCAL_INTERFACES_ONLY = new ClassFilter() {
        @Override
        public boolean isNeglectableClass(Class<?> clazz) {
            boolean isInterface = clazz.isInterface();
            Local annotation = clazz.getAnnotation(Local.class);
            return !(isInterface && annotation != null);
        }
    };

    public abstract boolean isNeglectableClass(Class<?> clazz);

}
