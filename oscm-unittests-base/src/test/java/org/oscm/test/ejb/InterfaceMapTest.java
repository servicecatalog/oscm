/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.junit.Assert.assertSame;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NoSuchElementException;

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



    @Test(expected = NoSuchElementException.class)
    public void testGetNegative() {
        InterfaceMap<Object> map = new InterfaceMap<Object>();
        map.get(Runnable.class);
    }

    static class A implements Runnable {

        public void run() {
        }

    }

    static class B implements Externalizable, Comparable<Object> {

        public void readExternal(ObjectInput input) {
        }

        public void writeExternal(ObjectOutput output) {
        }

        public int compareTo(Object other) {
            return 0;
        }

    }

    static class C implements Runnable, Comparable<Object> {

        public void run() {
        }

        public int compareTo(Object other) {
            return 0;
        }

    }

    static class D extends A {

    }

}
