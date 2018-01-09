/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 20.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;

import org.junit.Ignore;
import org.junit.Test;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.DomainObjectWithVersioning;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.BaseVO;
@Ignore
public class BaseAssemblerTest {

    @Test
    public void testConstructor() throws Exception {
        new BaseAssembler();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModification() throws Exception {
        DomainObject<?> object = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        final Field field = DomainObjectWithVersioning.class
                .getDeclaredField("version");
        field.setAccessible(true);
        field.set(object, Integer.valueOf(5));

        BaseVO template = new BaseVO() {
            private static final long serialVersionUID = 1L;
        };
        template.setVersion(4);

        BaseAssembler.verifyVersionAndKey(object, template);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModification_Creation() throws Exception {
        DomainObject<?> object = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        object.setKey(5);
        BaseVO template = new BaseVO() {
            private static final long serialVersionUID = 1L;
        };
        template.setKey(0);

        BaseAssembler.verifyVersionAndKey(object, template);
    }

    @Test
    public void testModification() throws Exception {
        DomainObject<?> object = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        final Field field = DomainObjectWithVersioning.class
                .getDeclaredField("version");
        field.setAccessible(true);
        field.set(object, Integer.valueOf(5));

        BaseVO template = new BaseVO() {
            private static final long serialVersionUID = 1L;
        };
        template.setVersion(5);

        BaseAssembler.verifyVersionAndKey(object, template);
    }

    @Test(expected = SaaSSystemException.class)
    public void testNonMatchingKeys() throws Exception {
        DomainObject<?> object = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        object.setKey(5);
        BaseVO template = new BaseVO() {
            private static final long serialVersionUID = 1L;
        };
        template.setKey(6);

        BaseAssembler.verifyVersionAndKey(object, template);
    }

    @Test
    public void testTrimString() throws Exception {
        assertEquals("abc", BaseAssembler.trim(" abc\n"));
        assertNull(BaseAssembler.trim(null));
    }

    @Test
    public void testUpdateValueObject() throws Exception {

        DomainObject<?> domObject = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        final Field field = DomainObjectWithVersioning.class
                .getDeclaredField("version");
        field.setAccessible(true);
        field.set(domObject, Integer.valueOf(2));

        domObject.setKey(5);

        BaseVO voObject = new BaseVO() {
            private static final long serialVersionUID = 1L;
        };
        voObject.setKey(6);
        voObject.setVersion(1);

        BaseAssembler.updateValueObject(voObject, domObject);
        assertEquals(5, voObject.getKey());
        assertEquals(2, voObject.getVersion());
    }
}
