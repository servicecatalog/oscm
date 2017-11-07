/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.DomainObjectWithVersioning;
import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Unit test for BasePOAssembler
 * 
 * @author Gao
 * 
 */
public class BasePOAssemblerTest {

    private DomainObject<?> domainObject;
    private BasePO template;
    private Field field;

    @Before
    public void setup() throws Exception {
        domainObject = new DomainObjectWithVersioning<DomainDataContainer>() {
            private static final long serialVersionUID = 1L;
        };
        template = new BasePO() {
            private static final long serialVersionUID = 1L;
        };
        field = DomainObjectWithVersioning.class.getDeclaredField("version");
        field.setAccessible(true);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void verifyVersionAndKey_ConcurrentModification() throws Exception {
        // given
        field.set(domainObject, Integer.valueOf(5));
        template.setVersion(4);
        // when
        BasePOAssembler.verifyVersionAndKey(domainObject, template);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void verifyVersionAndKey_ConcurrentModification_Creation()
            throws Exception {
        // given
        domainObject.setKey(5);
        template.setKey(0);
        // when
        BasePOAssembler.verifyVersionAndKey(domainObject, template);
    }

    @Test
    public void verifyVersionAndKey_Modification() throws Exception {
        // given
        field.set(domainObject, Integer.valueOf(5));
        template.setVersion(5);

        // when
        BasePOAssembler.verifyVersionAndKey(domainObject, template);
    }

    @Test(expected = SaaSSystemException.class)
    public void testNonMatchingKeys() throws Exception {
        // given
        domainObject.setKey(5);
        template.setKey(6);
        // when
        BasePOAssembler.verifyVersionAndKey(domainObject, template);
    }

    @Test
    public void trim_String() throws Exception {
        assertEquals("abc", BasePOAssembler.trim(" abc\n"));
        assertNull(BasePOAssembler.trim(null));
    }

    @Test
    public void updateValueObject() throws Exception {
        // given
        field.set(domainObject, Integer.valueOf(2));
        domainObject.setKey(5);
        template.setKey(6);
        template.setVersion(1);
        // when
        BasePOAssembler.updatePresentationObject(template, domainObject);
        // then
        assertEquals(5, template.getKey());
        assertEquals(2, template.getVersion());
    }
}
