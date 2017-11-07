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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ParameterizedTypesTest {

    @Test
    public void ctors() {
        new ParameterizedTypes();
    }

    @Test
    public void testAddAll() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        List<String> target = new ArrayList<String>();
        ParameterizedTypes.addAll(source, target, String.class);

        assertEquals(Arrays.asList("hello", "world", "!"), target);
    }

    @Test(expected = ClassCastException.class)
    public void testAddAllNegative() {
        List<Object> source = new ArrayList<Object>();
        source.add(new Object());

        List<String> target = new ArrayList<String>();
        ParameterizedTypes.addAll(source, target, String.class);
    }

    @Test
    public void testList() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        List<String> target = ParameterizedTypes.list(source, String.class);

        assertEquals(Arrays.asList("hello", "world", "!"), target);
    }

    @Test
    public void testListNullInput() {
        List<String> target = ParameterizedTypes.list(null, String.class);
        assertNotNull(target);
    }

    @Test(expected = ClassCastException.class)
    public void testListNegative() {
        List<Object> source = new ArrayList<Object>();
        source.add(new Object());

        ParameterizedTypes.list(source, String.class);
    }

    @Test
    public void testIteratorFromIterator() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterator(source.iterator(),
                String.class);

        assertTrue(i.hasNext());
        assertEquals("hello", i.next());
        assertTrue(i.hasNext());
        assertEquals("world", i.next());
        assertTrue(i.hasNext());
        assertEquals("!", i.next());
        assertFalse(i.hasNext());
    }

    @Test
    public void testIteratorFromIteratorRemove() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterator(source.iterator(),
                String.class);

        i.next();
        i.remove();
        i.next();
        i.remove();

        assertEquals(Collections.singletonList("!"), source);
    }

    @Test(expected = ClassCastException.class)
    public void testIteratorFromIteratorNegative() {
        List<Object> source = new ArrayList<Object>();
        source.add(new Object());

        Iterator<String> i = ParameterizedTypes.iterator(source.iterator(),
                String.class);
        i.next();
    }

    @Test
    public void testIteratorFromIterable() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterator(source, String.class);

        assertTrue(i.hasNext());
        assertEquals("hello", i.next());
        assertTrue(i.hasNext());
        assertEquals("world", i.next());
        assertTrue(i.hasNext());
        assertEquals("!", i.next());
        assertFalse(i.hasNext());
    }

    @Test
    public void testIteratorFromIterableRemove() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterator(source, String.class);

        i.next();
        i.remove();
        i.next();
        i.remove();

        assertEquals(Collections.singletonList("!"), source);
    }

    @Test(expected = ClassCastException.class)
    public void testIteratorFromIterableNegative() {
        List<Object> source = new ArrayList<Object>();
        source.add(new Object());

        Iterator<String> i = ParameterizedTypes.iterator(source, String.class);
        i.next();
    }

    @Test
    public void testIterable() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterable(source, String.class)
                .iterator();

        assertTrue(i.hasNext());
        assertEquals("hello", i.next());
        assertTrue(i.hasNext());
        assertEquals("world", i.next());
        assertTrue(i.hasNext());
        assertEquals("!", i.next());
        assertFalse(i.hasNext());
    }

    @Test
    public void testIterableRemove() {
        List<Object> source = new ArrayList<Object>();
        source.add("hello");
        source.add("world");
        source.add("!");

        Iterator<String> i = ParameterizedTypes.iterable(source, String.class)
                .iterator();

        i.next();
        i.remove();
        i.next();
        i.remove();

        assertEquals(Collections.singletonList("!"), source);
    }

    @Test(expected = ClassCastException.class)
    public void testIterableNegative() {
        List<Object> source = new ArrayList<Object>();
        source.add(new Object());

        Iterator<String> i = ParameterizedTypes.iterable(source, String.class)
                .iterator();
        i.next();
    }

    @Test
    public void testHashtable() {
        Map<?, ?> source = Collections.singletonMap("hello", "world");

        Hashtable<String, String> target = ParameterizedTypes.hashtable(source,
                String.class, String.class);

        assertEquals(Collections.singletonMap("hello", "world"), target);
    }

    @Test(expected = ClassCastException.class)
    public void testHashtableNegative() {
        Map<?, ?> source = Collections.singletonMap(new Object(),
                Integer.valueOf(5));
        ParameterizedTypes.hashtable(source, String.class, String.class);
    }

    @Test
    public void testSet() {
        Set<String> source = new HashSet<String>();
        source.add("String1");
        source.add("String2");

        Set<String> target = ParameterizedTypes.set(source, String.class);
        assertEquals(2, target.size());
        assertTrue(target.contains("String1"));
        assertTrue(target.contains("String2"));
    }

    @Test
    public void testSetNullInput() {
        Set<String> target = ParameterizedTypes.set(null, String.class);
        assertNotNull(target);
    }

    @Test(expected = ClassCastException.class)
    public void testSetInvalidTargetType() {
        Set<?> source = Collections.singleton(new Long(5));
        ParameterizedTypes.set(source, String.class);
    }
}
