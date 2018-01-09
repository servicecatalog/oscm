/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.dataservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObjectWithVersioning;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.exception.SaaSSystemException;

public class HistoryObjectFactoryTest {

    @Test
    public void testCreate() throws Exception {
        Organization org = new Organization();
        org.setKey(10000);
        setVersion(org, 123);
        final DomainHistoryObject<?> hist = HistoryObjectFactory.create(org,
                ModificationType.MODIFY, "me");
        assertEquals(10000, hist.getObjKey());
        assertEquals(123, hist.getObjVersion());
        assertEquals(ModificationType.MODIFY, hist.getModtype());
        assertEquals("me", hist.getModuser());
    }

    @Test
    public void testCreateWithModDate() throws Exception {
        Organization org = new Organization();
        org.setHistoryModificationTime(Long.valueOf(123456789));
        final DomainHistoryObject<?> hist = HistoryObjectFactory.create(org,
                ModificationType.MODIFY, "me");
        assertEquals(new Date(123456789), hist.getModdate());
    }

    @Test
    public void testCreateWithInvocationDate() throws Exception {
        Organization org = new Organization();
        long currentDate = System.currentTimeMillis();
        org.setHistoryModificationTime(Long.valueOf(currentDate));

        final DomainHistoryObject<?> hist = HistoryObjectFactory.create(org,
                ModificationType.MODIFY, "me");

        assertTrue(hist.getModdate() != hist.getInvocationDate());
        // Dates are almost the same if difference in milliseconds is small.
        long dateDiff = hist.getInvocationDate().getTime()
                - (new Date(currentDate)).getTime();
        assertTrue(dateDiff <= 1200);
    }

    @Test
    public void testCreateWithoutInvocationDate() throws Exception {
        Organization org = new Organization();

        final DomainHistoryObject<?> hist = HistoryObjectFactory.create(org,
                ModificationType.MODIFY, "me");
        // "modDate" and "invocationDate" must be equal.
        assertEquals(hist.getModdate(), hist.getInvocationDate());
    }

    @Test
    public void testGetVersionAdd() throws Exception {
        Organization org = new Organization();
        setVersion(org, 555);
        final int version = HistoryObjectFactory.getVersion(org,
                ModificationType.ADD);
        assertEquals(0, version);
    }

    @Test
    public void testGetVersionModify() throws Exception {
        Organization org = new Organization();
        setVersion(org, 5);
        final int version = HistoryObjectFactory.getVersion(org,
                ModificationType.MODIFY);
        assertEquals(5, version);
    }

    @Test
    public void testGetVersionDelete() throws Exception {
        Organization org = new Organization();
        setVersion(org, 99);
        final int version = HistoryObjectFactory.getVersion(org,
                ModificationType.DELETE);
        assertEquals(100, version);
    }

    private void setVersion(
            DomainObjectWithVersioning<? extends DomainDataContainer> obj,
            int version) throws Exception {
        final Field field = DomainObjectWithVersioning.class
                .getDeclaredField("version");
        field.setAccessible(true);
        field.set(obj, Integer.valueOf(version));
    }

    @Test
    public void testCreateHistoryObject() {
        final Organization org = new Organization();
        final DomainHistoryObject<?> hist = HistoryObjectFactory
                .createHistoryObject(org);
        assertEquals(OrganizationHistory.class, hist.getClass());
    }

    @Test(expected = SaaSSystemException.class)
    public void testCreateHistoryObjectNegative() {
        class HasNoHistory extends
                DomainObjectWithVersioning<DomainDataContainer> {
            private static final long serialVersionUID = 1L;
        }
        final HasNoHistory noHistory = new HasNoHistory();
        HistoryObjectFactory.createHistoryObject(noHistory);
    }

    @Test(expected = SaaSSystemException.class)
    public void testGetClassLoader_Negative() {
        HistoryObjectFactory.getClassLoader(String.class);
    }

    @Test
    public void testGetClassLoader() {
        ClassLoader classLoader = HistoryObjectFactory
                .getClassLoader(Organization.class);
        Assert.assertNotNull(classLoader);
    }
}
