/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
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

    @Test
    public void testInjectMethod() throws Exception {
        class Bean {
            private String foo;

            @SuppressWarnings("unused")
            private void setFoo(String foo) {
                this.foo = foo;
            }
        }
        Reference r = new Reference(String.class, "foo", Bean.class
                .getDeclaredMethod("setFoo", String.class));
        Bean target = new Bean();
        r.inject(target, "Hello");
        assertEquals("Hello", target.foo);
    }

    @Test
    public void testCreateForEJBField1() throws Exception {
        class Bean {
            @EJB
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        EJB ejb = field.getAnnotation(EJB.class);
        Reference r = Reference.createFor(ejb, field);
        assertEquals(Object.class, r.getInterfaceOrClass());
        assertEquals(Bean.class.getName() + "/foo", r.getName());
    }

    @Test
    public void testCreateForEJBField2() throws Exception {
        class Bean {
            @EJB(name = "other", beanInterface = Runnable.class)
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        EJB ejb = field.getAnnotation(EJB.class);
        Reference r = Reference.createFor(ejb, field);
        assertEquals(Runnable.class, r.getInterfaceOrClass());
        assertEquals("other", r.getName());
    }

    @Test
    public void testCreateForResourceField1() throws Exception {
        class Bean {
            @Resource
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        Resource resource = field.getAnnotation(Resource.class);
        Reference r = Reference.createFor(resource, field);
        assertEquals(Object.class, r.getInterfaceOrClass());
        assertEquals(Bean.class.getName() + "/foo", r.getName());
    }

    @Test
    public void testCreateForResourceField2() throws Exception {
        class Bean {
            @Resource(name = "other", type = Runnable.class)
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        Resource resource = field.getAnnotation(Resource.class);
        Reference r = Reference.createFor(resource, field);
        assertEquals(Runnable.class, r.getInterfaceOrClass());
        assertEquals("other", r.getName());
    }

    @Test
    public void testCreateForPersistenceContextField1() throws Exception {
        class Bean {
            @PersistenceContext
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        PersistenceContext ctx = field.getAnnotation(PersistenceContext.class);
        Reference r = Reference.createFor(ctx, field);
        assertEquals(EntityManager.class, r.getInterfaceOrClass());
        assertEquals(Bean.class.getName() + "/foo", r.getName());
    }

    @Test
    public void testCreateForPersistenceContextField2() throws Exception {
        class Bean {
            @PersistenceContext(name = "other")
            private Object foo;
        }
        Field field = Bean.class.getDeclaredField("foo");
        PersistenceContext ctx = field.getAnnotation(PersistenceContext.class);
        Reference r = Reference.createFor(ctx, field);
        assertEquals(EntityManager.class, r.getInterfaceOrClass());
        assertEquals("other", r.getName());
    }

}
