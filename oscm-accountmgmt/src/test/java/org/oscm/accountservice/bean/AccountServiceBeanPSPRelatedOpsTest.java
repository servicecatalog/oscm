/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.Collections;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.test.data.Organizations;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

public class AccountServiceBeanPSPRelatedOpsTest {

    private AccountServiceBean am;
    private DataServiceStub dm;

    private DomainObject<?> objectToBeReturned;
    private boolean throwObjectNotFound;
    private PlatformUser user;
    private Organization org;
    private boolean dm_contains = false;
    private Organization theSupplier;

    @Before
    public void setUp() throws Exception {
        am = new AccountServiceBean();
        dm = new DataServiceStub() {
            @Override
            public PlatformUser getCurrentUser() {
                return user;
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.oscm.test.stubs.DataServiceStub#getReference(java
             * .lang.Class, long)
             */
            @Override
            public <T extends DomainObject<?>> T getReference(
                    Class<T> objclass, long key) throws ObjectNotFoundException {
                return null;
            }

            public DomainObject<?> getReferenceByBusinessKey(
                    DomainObject<?> findTemplate)
                    throws ObjectNotFoundException {
                if (throwObjectNotFound) {
                    throw new ObjectNotFoundException(ClassEnum.ORGANIZATION,
                            null);
                }
                return objectToBeReturned;
            }

            @Override
            public boolean contains(Object arg0) {
                return dm_contains;
            }

            @Override
            public void persist(DomainObject<?> obj)
                    throws NonUniqueBusinessKeyException {
            }

        };

        PaymentServiceStub ps = new PaymentServiceStub() {
            @Override
            public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
            }
        };

        user = new PlatformUser();
        org = new Organization();
        user.setOrganization(org);
        theSupplier = Organizations.createOrganization(dm);

        OrganizationRole or = new OrganizationRole();
        or.setRoleName(OrganizationRoleType.CUSTOMER);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganization(org);
        otr.setOrganizationRole(or);
        org.setGrantedRoles(Collections.singleton(otr));
        am.dm = dm;
        am.paymentService = ps;
    }

    @Test(expected = PaymentDataException.class)
    public void testSetOrganizationHandledPaymentInformationForCustomerNullPTId()
            throws Exception {
        VOPaymentInfo paymentInfo = new VOPaymentInfo();
        paymentInfo.setPaymentType(new VOPaymentType());
        am.savePaymentInfo(paymentInfo);
        Assert.fail("Must fail, as null is no valid payment type");
    }

    @Test(expected = PaymentDataException.class)
    public void testSetOrganizationHandledPaymentInformationForCustomerNullSupplierId()
            throws Exception {
        VOPaymentInfo paymentInfo = new VOPaymentInfo();
        paymentInfo.setPaymentType(new VOPaymentType());
        am.savePaymentInfo(paymentInfo);

        Assert.fail("Must fail, as null is no valid payment type");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSetOrganizationHandledPaymentInformationForCustomerUnsupportedINVType()
            throws Exception {

        OrganizationRefToPaymentType otpt = new OrganizationRefToPaymentType();
        otpt.setOrganizationReference(new OrganizationReference(theSupplier,
                org, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER));
        otpt.setOrganizationRole(org.getGrantedRoles().iterator().next()
                .getOrganizationRole());

        PaymentType type = new PaymentType();
        type.setPaymentTypeId("CREDIT_CARD");
        otpt.setPaymentType(type);

        throwObjectNotFound = true;

        VOPaymentType pt = new VOPaymentType();
        pt.setPaymentTypeId("INVOICE");
        VOPaymentInfo paymentInfo = new VOPaymentInfo();
        paymentInfo.setPaymentType(pt);
        am.savePaymentInfo(paymentInfo);
    }

}
