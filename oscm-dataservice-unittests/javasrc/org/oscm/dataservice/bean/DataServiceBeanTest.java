/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.dataservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.SaaSSystemException;

public class DataServiceBeanTest {

    private DataServiceBean dataService;
    private EntityManager em;
    private Query namedQuery;
    private SessionContext sessionContext;
    private final Product domObject_withBusinessKey = new Product();
    private final Discount domObject_withoutBusinessKey = new Discount();

    @Before
    public void setup() {
        dataService = new DataServiceBean();
        em = mock(EntityManager.class);
        namedQuery = mock(Query.class);
        sessionContext = mock(SessionContext.class);
        dataService.em = em;
        dataService.sessionCtx = sessionContext;
        domObject_withBusinessKey.setProductId("productId");
        domObject_withBusinessKey.setVendorKey(1L);
    }

    @Test
    public void testClass2Enum() throws Exception {
        ClassEnum ce = dataService.class2Enum(Organization.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(PlatformUser.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Subscription.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Product.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(TechnicalProduct.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(ParameterDefinition.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(ParameterOption.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(OrganizationRole.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Report.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Event.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(TechnicalProductOperation.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(OperationParameter.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(RoleDefinition.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(PaymentType.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(PricedParameter.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(SupportedCurrency.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(UdaDefinition.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Uda.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(SupportedCountry.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(OrganizationToCountry.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(OrganizationReference.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Marketplace.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(Tag.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(TechnicalProductTag.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(UserRole.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(ProductReview.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(BillingContact.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(PaymentInfo.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(SupportedLanguage.class);
        assertNotNull(ce);
        ce = dataService.class2Enum(MarketplaceAccess.class);
        assertNotNull(ce);
    }

    @Test(expected = SaaSSystemException.class)
    public void testFindWithoutBusinessKey() throws Exception {
        try {
            doReturn(namedQuery).when(em).createNamedQuery(any(String.class));
            doReturn(namedQuery).when(namedQuery).setParameter(
                    any(String.class), any());
            dataService.find(domObject_withoutBusinessKey);
        } catch (SaaSSystemException e) {
            String msg = e.getMessage();
            assertTrue(msg.indexOf("No BusinessKey defined for") > 0);
            throw e;
        }
    }

    @Test(expected = SaaSSystemException.class)
    public void testFindWithoutQuery() throws Exception {
        try {
            dataService.find(domObject_withBusinessKey);
        } catch (SaaSSystemException e) {
            String msg = e.getMessage();
            assertTrue(msg.indexOf("Could not create query") > 0);
            throw e;
        }
    }

    @Test(expected = SaaSSystemException.class)
    public void testFindNonUniqueResult() throws Exception {
        doThrow(new NonUniqueResultException()).when(namedQuery)
                .getSingleResult();
        doReturn(namedQuery).when(em).createNamedQuery(any(String.class));
        doReturn(namedQuery).when(namedQuery).setParameter(any(String.class),
                any());
        try {
            dataService.find(domObject_withBusinessKey);
        } catch (SaaSSystemException e) {
            String msg = e.getMessage();
            assertTrue(msg.indexOf("Non-Unique Business Key Search for") > 0);
            throw e;
        }
    }

    @Test(expected = SaaSSystemException.class)
    public void testFindOtherException() throws Exception {
        doThrow(new UnsupportedOperationException()).when(namedQuery)
                .getSingleResult();
        doReturn(namedQuery).when(em).createNamedQuery(any(String.class));
        doReturn(namedQuery).when(namedQuery).setParameter(any(String.class),
                any());
        try {
            dataService.find(domObject_withBusinessKey);
        } catch (SaaSSystemException e) {
            Throwable c = e.getCause();
            assertTrue(c instanceof UnsupportedOperationException);
            throw e;
        }
    }

    @Test
    public void testFindHistoryNull() throws Exception {
        assertNull(dataService.findHistory(null));
    }

    @Test(expected = SaaSSystemException.class)
    public void testFindHistoryQueryReturnsNonDomainHistoryObject()
            throws Exception {
        doThrow(new NonUniqueResultException()).when(namedQuery)
                .getSingleResult();
        doReturn(namedQuery).when(em).createNamedQuery(any(String.class));
        doReturn(namedQuery).when(namedQuery).setParameter(any(String.class),
                any());
        List<Product> resultNoHistoryObject = Arrays
                .asList(domObject_withBusinessKey);
        doReturn(resultNoHistoryObject).when(namedQuery).getResultList();
        try {
            dataService.findHistory(domObject_withBusinessKey);
        } catch (SaaSSystemException e) {
            String msg = e.getMessage();
            assertTrue(msg.indexOf("findHistory loaded Non-History Object") > 0);
            throw e;
        }
    }

    @Test
    public void testContains() throws Exception {
        dataService.contains(domObject_withBusinessKey);
        verify(em, times(1)).contains(domObject_withBusinessKey);
    }

    @Test
    public void testCreateNativeQuery() throws Exception {
        dataService.createNativeQuery("test", Product.class);
        verify(em, times(1)).createNativeQuery("test", Product.class);
    }

    @Test
    public void testGetCurrentHistoryUserSessionContextNull() throws Exception {
        dataService.initCurrentUser();
        assertEquals("", DataServiceBean.getCurrentHistoryUser());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCurrentHistoryUserCallerPrincipalNull() throws Exception {
        doReturn(null).when(sessionContext).getCallerPrincipal();

        doReturn(null).when(em).find(any(Class.class), any());
        // initialize the ThreadLocal CURRENT_USER
        dataService.find(PlatformUser.class, 0);

        assertEquals("", DataServiceBean.getCurrentHistoryUser());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCurrentHistoryUserCallerPrincipal() throws Exception {
        final long userKey = 4711;
        Principal caller = new Principal() {
            @Override
            public String getName() {
                return String.valueOf(userKey);
            }
        };

        doReturn(caller).when(sessionContext).getCallerPrincipal();
        PlatformUser user = new PlatformUser();
        user.setKey(userKey);
        user.setOrganization(new Organization());
        doReturn(user).when(em).find(any(Class.class), any());

        // initialize the ThreadLocal CURRENT_USER
        dataService.find(PlatformUser.class, userKey);

        assertEquals(String.valueOf(userKey),
                DataServiceBean.getCurrentHistoryUser());
    }

    /**
     * Login as a temporal on-behalf user. The query for the current history
     * user must return the user that acts on behalf instead of the temporary
     * user.
     */
    @Test
    public void testGetCurrentHistoryUser_OnBehalfOf() throws Exception {

        // given user that acts on behalf or another user
        final long masterUserKey = 4711;
        final long slaveUserKey = 1;
        final PlatformUser masterUser = createUser(masterUserKey);
        final PlatformUser slaveUser = createOnBehalfUser(slaveUserKey,
                masterUser);

        // when logged in as on behalf user
        loginAs(slaveUserKey);
        createDataManagerStub(masterUser, slaveUser);

        // then the user that acts on behalf will be used for history objects
        assertEquals(String.valueOf(masterUserKey),
                DataServiceBean.getCurrentHistoryUser());
    }

    PlatformUser createUser(final long key) {
        Organization org = new Organization();
        final PlatformUser slaveUser = new PlatformUser();
        slaveUser.setKey(key);
        slaveUser.setOrganization(org);
        return slaveUser;
    }

    PlatformUser createOnBehalfUser(final long slaveUserKey,
            final PlatformUser masterUser) {
        final PlatformUser slaveUser = createUser(slaveUserKey);
        OnBehalfUserReference onBehalf = new OnBehalfUserReference();
        slaveUser.setMaster(onBehalf);
        onBehalf.setMasterUser(masterUser);
        return slaveUser;
    }

    void createDataManagerStub(final PlatformUser masterUser,
            final PlatformUser slaveUser) {
        doReturn(masterUser).when(em).find(PlatformUser.class,
                Long.valueOf(masterUser.getKey()));
        doReturn(slaveUser).when(em).find(PlatformUser.class,
                Long.valueOf(slaveUser.getKey()));
        // execute on find to initialize the local variables
        dataService.find(PlatformUser.class, slaveUser.getKey());
    }

    void loginAs(final long slaveUserKey) {

        Principal caller = new Principal() {
            @Override
            public String getName() {
                return String.valueOf(slaveUserKey);
            }
        };

        doReturn(caller).when(sessionContext).getCallerPrincipal();
    }

    @Test
    public void testDelegates() throws Exception {
        String TEST = "test";
        dataService.merge(TEST);
        verify(em, times(1)).merge(TEST);
    }

    @Test
    public void getSession() throws Exception {
        dataService.getSession();
        verify(em, times(1)).unwrap(Session.class);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testRemovePersistentDomainObject() {
        doReturn(true).when(em).contains(domObject_withBusinessKey);
        dataService.remove(domObject_withBusinessKey);
        verify(em, times(1)).remove(domObject_withBusinessKey);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testRemoveNonPersistentDomainObject() {
        doReturn(false).when(em).contains(domObject_withBusinessKey);
        dataService.remove(domObject_withBusinessKey);
        verify(em, times(0)).remove(domObject_withBusinessKey);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testRemovePersistentObject() {
        Object obj = new Object();
        doReturn(true).when(em).contains(obj);
        dataService.remove(obj);
        verify(em, times(1)).remove(obj);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testRemoveNonPersistentObject() {
        Object obj = new Object();
        doReturn(false).when(em).contains(obj);
        dataService.remove(obj);
        verify(em, times(0)).remove(obj);
    }

}
