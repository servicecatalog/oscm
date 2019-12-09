/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;

/**
 * Unit tests for {@link Reference}.
 * 
 * @author hoffmann
 */
public class ReferenceTest {

    @Test
    public void testGetBeanInterface() {
        Reference r = new Reference(Runnable.class, "foo");
        assertEquals(Runnable.class, r.getInterfaceOrClass());
    }

    @Test
    public void testGetName() {
        Reference r = new Reference(Runnable.class, "foo");
        assertEquals("foo", r.getName());
    }

    @Test
    public void testInjectField() throws Exception {
        class Bean {
            private String foo;
        }
        Reference r = new Reference(String.class, "foo", Bean.class
                .getDeclaredField("foo"));
        Bean target = new Bean();
        r.inject(target, "Hello");
        assertEquals("Hello", target.foo);
    }


}
