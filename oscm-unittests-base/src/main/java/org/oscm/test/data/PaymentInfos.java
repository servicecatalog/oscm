/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import org.mockito.Mockito;

import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.accountservice.assembler.PaymentInfoAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;

public class PaymentInfos {

    public static VOPaymentInfo createVOPaymentInfo(Organization customer,
            DataService mgr, PaymentType paymentType)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        String paymentName = paymentType.getPaymentTypeId()
                + System.currentTimeMillis();
        return PaymentInfoAssembler.toVOPaymentInfo(
                createPaymentInfo(customer, mgr, paymentType, paymentName),
                new LocalizerFacade(Mockito.mock(LocalizerServiceLocal.class),
                        "en"));
    }

    public static PaymentInfo createPaymentInfo(Organization customer,
            DataService mgr, PaymentType paymentType)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        String paymentName = paymentType.getPaymentTypeId()
                + System.currentTimeMillis();
        return createPaymentInfo(customer, mgr, paymentType, paymentName);
    }

    public static PaymentInfo createPaymentInfo(Organization customer,
            DataService mgr, PaymentType paymentType, String paymentName)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setExternalIdentifier("pspId");
        paymentInfo.setPaymentInfoId(paymentName);
        Organization organization = mgr.getReference(Organization.class,
                customer.getKey());

        paymentInfo.setOrganization(organization);
        paymentInfo.setPaymentType(paymentType);
        mgr.persist(paymentInfo);
        return paymentInfo;
    }

    public static BillingContact createBillingContact(DataService mgr,
            Organization organization) throws NonUniqueBusinessKeyException {
        String billingName = organization.getOrganizationId()
                + System.currentTimeMillis();
        return createBillingContact(mgr, organization, billingName);
    }

    public static BillingContact createBillingContact(DataService mgr,
            Organization organization, String billingName)
            throws NonUniqueBusinessKeyException {
        BillingContact billingContact = new BillingContact();
        billingContact.setEmail("setEmail@setEmail.de");
        billingContact.setAddress("adress");
        billingContact.setCompanyName("companyname");
        billingContact.setOrganization(organization);
        billingContact.setBillingContactId(billingName);
        mgr.persist(billingContact);
        return billingContact;
    }

    public static VOBillingContact createBillingContact(
            Organization organization, DataService mgr)
            throws NonUniqueBusinessKeyException {
        String billingName = organization.getOrganizationId()
                + System.currentTimeMillis();
        BillingContact bc = createBillingContact(mgr, organization, billingName);
        return BillingContactAssembler.toVOBillingContact(bc);
    }

    public static PaymentType findPaymentType(String paymentTypeName,
            DataService mgr) throws ObjectNotFoundException {

        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(paymentTypeName);
        return (PaymentType) mgr.getReferenceByBusinessKey(pt);
    }
}
