/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.junit.Assert.assertSame;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NoSuchElementException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for {@link InterfaceMap}.
 * 
 * @author hoffmann
 */
public class InterfaceMapTest {

    @Test
    public void testGet() {
        InterfaceMap<Object> map = new InterfaceMap<Object>();
        A a = new A();
        B b = new B();
        map.put(a, a);
        map.put(b, b);
        assertSame(a, map.get(Runnable.class));
        assertSame(b, map.get(Externalizable.class));
        assertSame(b, map.get(Comparable.class));
    }

    @Test
    public void testPutSuperclassInterface() {
        InterfaceMap<Object> map = new InterfaceMap<Object>();
        D d = new D();
        map.put(d, d);
        assertSame(d, map.get(Runnable.class));
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    // TODO Enes
    public void testPutNegative() {
        InterfaceMap<Object> map = new InterfaceMap<Object>();
        A a = new A();
        map.put(a, a);
        C c = new C();
        map.put(c, c);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNegative() {
        InterfaceMap<Object> map = new InterfaceMap<Object>();
        map.get(Runnable.class);
    }

    static class A implements Runnable {

        @Override
        public void run() {
        }

    }

    static class B implements Externalizable, Comparable<Object> {

        @Override
        public void readExternal(ObjectInput input) {
        }

        @Override
        public void writeExternal(ObjectOutput output) {
        }

        @Override
        public int compareTo(Object other) {
            return 0;
        }

    }

    static class C implements Runnable, Comparable<Object> {

        @Override
        public void run() {
        }

        @Override
        public int compareTo(Object other) {
            return 0;
        }

    }

    static class D extends A {

    }

}
