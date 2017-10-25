/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Marc Hoffmann                                                      
 *                                                                              
 *  Creation Date: 14.01.2010                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of utility method to safely convert to parameterized types.
 * 
 * @author hoffmann
 */
public class ParameterizedTypes {

    /**
     * Adds all elements from the given source to the given target collection.
     * The elements are added to the target collection in the sequence of the
     * source iterator. All elements must be of the given type, otherwise a
     * {@link ClassCastException} is thrown.
     * 
     * @param <T>
     *            expected element type
     * @param source
     *            source of elements
     * @param target
     *            target collection
     * @param type
     *            expected element type
     */
    public static <T> void addAll(final Iterable<?> source,
            final Collection<T> target, final Class<T> type) {
        for (Object o : source) {
            target.add(type.cast(o));
        }
    }

    /**
     * Wraps a iterator to return elements of a specified types. All elements
     * returned by the source iterator must be of the given type, otherwise a
     * {@link ClassCastException} is thrown.
     * 
     * @param <T>
     *            expected element type
     * @param source
     *            source iterator
     * @param type
     *            expected element type
     * @return typed iterator backed by the source iterator
     */
    public static <T> Iterator<T> iterator(final Iterator<?> source,
            final Class<T> type) {
        return new Iterator<T>() {

            public boolean hasNext() {
                return source.hasNext();
            }

            public T next() {
                return type.cast(source.next());
            }

            public void remove() {
                source.remove();
            }
        };
    }

    /**
     * Creates an iterator from an {@link Iterable} to return elements of a
     * specified type. All elements returned by the source must be of the given
     * type, otherwise a {@link ClassCastException} is thrown.
     * 
     * @param <T>
     *            expected element type
     * @param source
     *            source of the iterator
     * @param type
     *            expected element type
     * @return
     * @return typed iterator backed by the source
     */
    public static <T> Iterator<T> iterator(final Iterable<?> source,
            final Class<T> type) {
        return iterator(source.iterator(), type);
    }

    /**
     * Wraps the given {@link Iterable} into a new {@link Iterable} that returns
     * elements of the specified type.All elements returned by the source must
     * be of the given type, otherwise
     * 
     * @param <T>
     *            expected element type
     * @param source
     *            source iterable
     * @param type
     *            expected element type
     * @return typed iterable
     */
    public static <T> Iterable<T> iterable(final Iterable<?> source,
            final Class<T> type) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return ParameterizedTypes.iterator(source.iterator(), type);
            }
        };
    }

    /**
     * Creates a parameterized copy of the given list. All elements in the
     * source list must be of the given type, otherwise a
     * {@link ClassCastException} is thrown.
     * 
     * @param <T>
     *            element type
     * @param source
     *            copy source
     * @param type
     *            element type
     * @return new list instance
     */
    public static <T> List<T> list(final List<?> source, final Class<T> type) {
        if (source == null) {
            return new ArrayList<T>();
        }
        List<T> target = new ArrayList<T>(source.size());
        addAll(source, target, type);
        return target;
    }

    /**
     * Creates a parameterized copy of the given set. All elements in the source
     * set must be of the given type, otherwise a {@link ClassCastException} is
     * thrown.
     * 
     * @param <T>
     *            element type
     * @param source
     *            copy source
     * @param type
     *            element type
     * @return new set instance
     */
    public static <T> Set<T> set(final Set<?> source, final Class<T> type) {
        if (source == null) {
            return Collections.emptySet();
        }
        Set<T> target = new HashSet<T>(source.size());
        addAll(source, target, type);
        return target;
    }

    /**
     * Creates a parameterized copy of the given map. All keys and values must
     * be of the given type, otherwise a {@link ClassCastException} is thrown.
     * 
     * @param <K>
     *            key type
     * @param <V>
     *            value type
     * @param source
     *            copy source
     * @param ktype
     *            key type
     * @param vtype
     *            value type
     * @return new Hashtable instance
     */
    public static <K, V> Hashtable<K, V> hashtable(final Map<?, ?> source,
            final Class<K> ktype, final Class<V> vtype) {
        final Hashtable<K, V> target = new Hashtable<K, V>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            K key = ktype.cast(entry.getKey());
            V value = vtype.cast(entry.getValue());
            target.put(key, value);
        }
        return target;
    }

    /**
     * Creates a parameterized copy of the given map. All keys and values must
     * be of the given type, otherwise a {@link ClassCastException} is thrown.
     * 
     * @param <K>
     *            key type
     * @param <V>
     *            value type
     * @param source
     *            copy source
     * @param ktype
     *            key type
     * @param vtype
     *            value type
     * @return new HashMap instance
     */
    public static <K, V> Map<K, V> hashmap(final Map<?, ?> source,
            final Class<K> ktype, final Class<V> vtype) {
        final Map<K, V> target = new HashMap<K, V>();
        if (source == null) {
            return target;
        }
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            K key = ktype.cast(entry.getKey());
            V value = null;
            if (vtype != null) {
                value = vtype.cast(entry.getValue());
            }
            target.put(key, value);
        }
        return target;
    }

}
