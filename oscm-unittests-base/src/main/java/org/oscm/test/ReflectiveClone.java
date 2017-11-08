/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainObject;

// ----------------------------------------------------------------------------
/**
 * This non-instantiable non-extendible class provides a static clone() method
 * suitable for cloning an instance of a class satisfying the following
 * constraints:
 * <UL>
 * <LI>no-arg constructor is available (not necessarily public)
 * <LI>neither the class nor any of its superclasses have any final fields
 * <LI>neither the class nor any of its superclasses have any inner classes
 * </UL>
 * 
 * This class requires sufficient security privileges to work. This
 * implementation is not industrial strength and is provided for demo purposes.
 * <P>
 * 
 * MT-safety: this class is safe for use from mutliple concurrent threads.
 * 
 * @author (C) <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>,
 *         2002
 */
public abstract class ReflectiveClone {

    /**
     * Makes a reflection-based deep clone of 'obj'.
     * 
     * @param obj
     *            input object to clone [null will cause a NullPointerException]
     * @return obj's deep clone [never null; can be == to 'obj']
     * 
     * @throws RuntimeException
     *             on any failure
     */
    public static Object clone(final Object obj) {
        return clone(obj, new IdentityHashMap<Object, Object>(),
                new HashMap<Class<?>, ClassMetadata>());
    }

    private ReflectiveClone() {
    } // prevent subclassing

    /*
     * Internal class used to cache class metadata information.
     */
    private static final class ClassMetadata {
        Constructor<?> m_noargConstructor; // cached no-arg constructor
        Field[] m_declaredFields; // cached declared fields
        boolean m_noargConstructorAccessible;
        boolean m_fieldsAccessible;

    } // end of nested class

    /**
     * The workhorse behind clone(Object). This method is mutually recursive
     * with {@link #setFields(Object, Object, Field[], boolean, Map, Map)}.
     * 
     * @param obj
     *            current source object being cloned
     * @param objMap
     *            maps a source object to its clone in the current traversal
     * @param metadataMap
     *            maps a Class object to its ClassMetadata.
     */
    private static Object clone(final Object obj,
            final Map<Object, Object> objMap,
            final Map<Class<?>, ClassMetadata> metadataMap) {
        if (DEBUG)
            System.out.println("traversing src obj [" + obj + "]");

        // return 'obj' clone if it has been instantiated already:
        if (objMap.containsKey(obj))
            return objMap.get(obj);

        final Class<?> objClass = obj.getClass();
        final Object result;

        if (objClass.isArray()) {
            final int arrayLength = Array.getLength(obj);

            if (arrayLength == 0) // empty arrays are immutable
            {
                objMap.put(obj, obj);
                return obj;
            } else {
                final Class<?> componentType = objClass.getComponentType();

                // even though arrays implicitly have a public clone(), it
                // cannot be invoked reflectively, so need to do copy
                // construction:

                result = Array.newInstance(componentType, arrayLength);
                objMap.put(obj, result);

                if (componentType.isPrimitive()
                        || FINAL_IMMUTABLE_CLASSES.contains(componentType)) {
                    System.arraycopy(obj, 0, result, 0, arrayLength);
                } else {
                    for (int i = 0; i < arrayLength; ++i) {
                        // recursively clone each array slot:
                        final Object slot = Array.get(obj, i);
                        if (slot != null) {
                            final Object slotClone = clone(slot, objMap,
                                    metadataMap);
                            Array.set(result, i, slotClone);
                        }
                    }
                }

                return result;
            }
        } else if (FINAL_IMMUTABLE_CLASSES.contains(objClass)) {
            objMap.put(obj, obj);
            return obj;
        }

        // fall through to reflectively populating an instance created
        // with a noarg constructor:

        ClassMetadata metadata = metadataMap.get(objClass);
        if (metadata == null) {
            metadata = new ClassMetadata();
            metadataMap.put(objClass, metadata);
        }

        { // clone = objClass.newInstance () can't handle private constructors

            Constructor<?> noarg = metadata.m_noargConstructor;
            if (noarg == null) {
                try {
                    noarg = objClass.getDeclaredConstructor(EMPTY_CLASS_ARRAY);
                    metadata.m_noargConstructor = noarg;
                } catch (Exception e) {
                    throw new RuntimeException("class [" + objClass.getName()
                            + "] has no noarg constructor: " + e.toString());
                }
            }

            if (!metadata.m_noargConstructorAccessible
                    && (Modifier.PUBLIC & noarg.getModifiers()) == 0) {
                try {
                    noarg.setAccessible(true);
                } catch (SecurityException e) {
                    throw new RuntimeException(
                            "cannot access noarg constructor [" + noarg
                                    + "] of class [" + objClass.getName()
                                    + "]: " + e.toString());
                }

                metadata.m_noargConstructorAccessible = true;
            }

            try // to create a clone via the no-arg constructor
            {
                result = noarg.newInstance(EMPTY_OBJECT_ARRAY);
                objMap.put(obj, result);
            } catch (Exception e) {
                throw new RuntimeException("cannot instantiate class ["
                        + objClass.getName() + "] using noarg constructor: "
                        + e.toString());
            }
        }

        for (Class<?> c = objClass; c != Object.class; c = c.getSuperclass()) {
            metadata = metadataMap.get(c);
            if (metadata == null) {
                metadata = new ClassMetadata();
                metadataMap.put(c, metadata);
            }

            Field[] declaredFields = metadata.m_declaredFields;
            if (declaredFields == null) {
                declaredFields = c.getDeclaredFields();
                metadata.m_declaredFields = declaredFields;
            }

            setFields(obj, result, declaredFields, metadata.m_fieldsAccessible,
                    objMap, metadataMap);
            metadata.m_fieldsAccessible = true;
        }

        return result;
    }

    /**
     * This method sets clones all declared 'fields' from 'src' to 'dest' and
     * updates the object and metadata maps accordingly.
     * 
     * @param src
     *            source object
     * @param dest
     *            src's clone [not fully populated yet]
     * @param fields
     *            fields to be populated
     * @param accessible
     *            'true' if all 'fields' have been made accessible during this
     *            traversal
     */
    private static void setFields(final Object src, final Object dest,
            final Field[] fields, final boolean accessible,
            final Map<Object, Object> objMap,
            final Map<Class<?>, ClassMetadata> metadataMap) {
        for (int f = 0, fieldsLength = fields.length; f < fieldsLength; ++f) {
            final Field field = fields[f];
            final int modifiers = field.getModifiers();

            if (DEBUG)
                System.out.println("dest object [" + dest + "]: field #" + f
                        + ", [" + field + "]");

            if ((Modifier.STATIC & modifiers) != 0)
                continue;

            // can also skip transient fields here if you want reflective
            // cloning
            // to be more like serialization

            if ((Modifier.FINAL & modifiers) != 0)
                throw new RuntimeException("cannot set final field ["
                        + field.getName() + "] of class ["
                        + src.getClass().getName() + "]");

            if (!accessible && ((Modifier.PUBLIC & modifiers) == 0)) {
                try {
                    field.setAccessible(true);
                } catch (SecurityException e) {
                    throw new RuntimeException("cannot access field ["
                            + field.getName() + "] of class ["
                            + src.getClass().getName() + "]: " + e.toString());
                }
            }

            try // to clone and set the field value:
            {
                Object value = field.get(src);

                if (value == null) {
                    field.set(dest, null); // can't assume that the constructor
                    // left this as null
                    if (DEBUG)
                        System.out.println("set field #" + f + ", [" + field
                                + "] of object [" + dest + "]: NULL");
                } else {
                    final Class<?> valueType = value.getClass();

                    // handle enum
                    if (valueType.getSuperclass() == Enum.class) {
                        field.set(dest, value);
                    } else {
                        if (!valueType.isPrimitive()
                                && !FINAL_IMMUTABLE_CLASSES.contains(valueType)) {
                            // value is an object reference and it could be
                            // either
                            // an array
                            // or of some mutable type: try to clone it deeply
                            // to be
                            // on the safe side

                            // Cloning of associations (i.e. members of
                            // non-primitive classes)
                            // shall
                            // follow the following rules:
                            // 1) Follow if type is DomainDataContainer
                            // 2) Follow if member is JPA-annotated with
                            // cascade-option ALL,
                            // PERSIST, DELETE or MERGE
                            if (DomainDataContainer.class
                                    .isAssignableFrom(field.getType())
                                    || needsToCascade(field)
                                    || value.getClass().isArray()) {
                                value = clone(value, objMap, metadataMap);
                            }
                        }

                        field.set(dest, value);
                    }
                    if (DEBUG)
                        System.out.println("set field #" + f + ", [" + field
                                + "] of object [" + dest + "]: " + value);
                }
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace(System.out);
                throw new RuntimeException("cannot set field ["
                        + field.getName() + "] of class ["
                        + src.getClass().getName() + "]: " + e.toString());
            }
        }
    }

    private static boolean needsToCascade(Field field) {
        Class<?> fieldtype = field.getType();
        if (!DomainObject.class.isAssignableFrom(fieldtype))
            return false;
        Annotation ann;
        CascadeType[] cascades = null;
        ann = field.getAnnotation(OneToOne.class);
        if (ann != null) {
            cascades = ((OneToOne) ann).cascade();
        } else {
            ann = field.getAnnotation(OneToMany.class);
            if (ann != null) {
                cascades = ((OneToMany) ann).cascade();
            } else {
                ann = field.getAnnotation(ManyToOne.class);
                if (ann != null) {
                    cascades = ((ManyToOne) ann).cascade();
                } else {
                    ann = field.getAnnotation(ManyToMany.class);
                    if (ann != null) {
                        cascades = ((ManyToMany) ann).cascade();
                    }
                }
            }
        }
        if (cascades == null)
            return false;
        for (CascadeType cas : cascades) {
            if ((cas == CascadeType.ALL) || (cas == CascadeType.MERGE)
                    || (cas == CascadeType.PERSIST)
                    || (cas == CascadeType.REMOVE)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean DEBUG = false;

    private static final Set<Class<?>> FINAL_IMMUTABLE_CLASSES; // set in
    // <clinit>
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    static {
        FINAL_IMMUTABLE_CLASSES = new HashSet<Class<?>>(17);

        // add some common final/immutable classes:
        FINAL_IMMUTABLE_CLASSES.add(String.class);
        FINAL_IMMUTABLE_CLASSES.add(Byte.class);
        FINAL_IMMUTABLE_CLASSES.add(Short.class);
        FINAL_IMMUTABLE_CLASSES.add(Integer.class);
        FINAL_IMMUTABLE_CLASSES.add(Long.class);
        FINAL_IMMUTABLE_CLASSES.add(Float.class);
        FINAL_IMMUTABLE_CLASSES.add(Double.class);
        FINAL_IMMUTABLE_CLASSES.add(Character.class);
        FINAL_IMMUTABLE_CLASSES.add(Boolean.class);
    }

} // end of class
// ----------------------------------------------------------------------------
