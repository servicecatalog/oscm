/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;

public class VersionAndKeyValidatorTest {

    @Test(expected = ConcurrentModificationException.class)
    public void verify_versionMismatch() throws Exception {
        // given
        Organization o = new Organization();
        Organization t = new Organization();

        // when
        VersionAndKeyValidator.verify(o, t, -1);
    }

    @Test(expected = SaaSSystemException.class)
    public void verify_differentObjects() throws Exception {
        // given
        Organization o = new Organization();
        o.setKey(1);
        Organization t = new Organization();
        t.setKey(2);

        // when
        VersionAndKeyValidator.verify(o, t, 0);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void verify_newObjectAlreadySaved() throws Exception {
        // given
        Organization o = new Organization();
        o.setKey(1);
        Organization t = new Organization();
        t.setKey(0);

        // when
        VersionAndKeyValidator.verify(o, t, 0);
    }

    @Test
    public void verify() throws Exception {
        // given
        Organization o = new Organization();
        o.setKey(0);
        Organization t = new Organization();
        t.setKey(0);

        // when
        VersionAndKeyValidator.verify(o, t, 0);
    }

    @Test
    public void versionAndKeyValidator() {
        assertNotNull(new VersionAndKeyValidator());
    }

    @Test
    public void verify_wrongTemplateVersionInErrorMessage() throws Exception {
        // given
        Organization o = new Organization();
        o.setKey(1);
        Organization t = new Organization();
        t.setKey(0);

        // when
        try {
            VersionAndKeyValidator.verify(o, t, 1);
        } catch (ConcurrentModificationException e) {
            // then
            String versionInMessage = e.getMessage().substring(
                    e.getMessage().length() - 1, e.getMessage().length());
            assertEquals("1", versionInMessage);
        }
    }

}
