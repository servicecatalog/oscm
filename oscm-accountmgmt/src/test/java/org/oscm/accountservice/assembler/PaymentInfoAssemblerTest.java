/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: walker
 *                                                                              
 *  Creation Date: 18.03.2011                                                      
 *                                                                              
 *  Completion Time: 21.03.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.accountservice.assembler;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.DomainDataContainer;
import org.oscm.domobjects.DomainObjectWithVersioning;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPaymentInfo;

public class PaymentInfoAssemblerTest {
    private LocalizerFacade lf = new LocalizerFacade(null, "") {
        public String getText(long objkey, LocalizedObjectTypes objtype) {
            return objtype + "." + objkey;
        }
    };

    @Test
    public void testToVOPaymentInfo() throws Exception {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setKey(123);
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId("pspid");
        paymentInfo.setExternalIdentifier("exid");
        paymentInfo.setPaymentType(paymentType);
        VOPaymentInfo voPaymentInfo = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfo, lf);

        Assert.assertNotNull("paymentInfo is not null", voPaymentInfo);
        Assert.assertEquals(123, voPaymentInfo.getKey());
        Assert.assertEquals(
                paymentType.getPaymentTypeId(),
                PaymentTypeAssembler.toPaymentType(
                        voPaymentInfo.getPaymentType()).getPaymentTypeId());
        Assert.assertEquals(paymentInfo.getVersion(),
                voPaymentInfo.getVersion());
    }

    @Test
    public void testToVOPaymentInfo_bug10160() throws Exception {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentInfoId("paymentInfoId");
        PaymentType paymentType = new PaymentType();
        paymentType.setKey(123);
        setVersion(paymentInfo, 1);
        paymentInfo.setPaymentType(paymentType);
        VOPaymentInfo voPaymentInfo = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfo, lf);
        Assert.assertEquals("paymentInfoId", voPaymentInfo.getId());
    }

    @Test
    public void testToVOPaymentInfo_bug10160_WithOriginalValueInvoice()
            throws Exception {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentInfoId("paymentInfoId");
        PaymentType paymentType = new PaymentType();
        paymentType.setKey(3);
        setVersion(paymentInfo, 0);
        paymentInfo.setPaymentType(paymentType);
        VOPaymentInfo voPaymentInfo = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfo, lf);
        Assert.assertEquals("PAYMENT_TYPE_NAME.3", voPaymentInfo.getId());
    }

    @Test
    public void testToVOPaymentInfo_bug10160_WithOriginalValueNotInvoice()
            throws Exception {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentInfoId("paymentInfoId");
        PaymentType paymentType = new PaymentType();
        paymentType.setKey(1);
        setVersion(paymentInfo, 0);
        paymentInfo.setPaymentType(paymentType);
        VOPaymentInfo voPaymentInfo = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfo, lf);
        Assert.assertEquals("paymentInfoId", voPaymentInfo.getId());
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
    public void testToVOPaymentInfo_PaymentInfoNull() throws Exception {
        VOPaymentInfo paymentInfo = PaymentInfoAssembler.toVOPaymentInfo(null,
                lf);
        Assert.assertNull("paymentInfo is not null", paymentInfo);
    }

    @Test
    public void testToVOPaymentInfoNullPaymentType() throws Exception {
        PaymentInfo paymentInfo = new PaymentInfo();
        VOPaymentInfo voPaymentInfo = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfo, lf);

        Assert.assertNull("paymenttype is not null",
                voPaymentInfo.getPaymentType());
    }

    @Test
    public void testUpdatePaymentInfo() throws Exception {
        VOPaymentInfo pi = new VOPaymentInfo();
        pi.setId("name");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo = PaymentInfoAssembler.updatePaymentInfo(paymentInfo, pi);
        Assert.assertEquals(pi.getId(), paymentInfo.getPaymentInfoId());
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePaymentInfo_NameNull() throws Exception {
        VOPaymentInfo pi = new VOPaymentInfo();
        pi.setId(null);
        PaymentInfo paymentInfo = new PaymentInfo();
        PaymentInfoAssembler.updatePaymentInfo(paymentInfo, pi);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePaymentInfo_NameEmpty() throws Exception {
        VOPaymentInfo pi = new VOPaymentInfo();
        pi.setId("   ");
        PaymentInfo paymentInfo = new PaymentInfo();
        PaymentInfoAssembler.updatePaymentInfo(paymentInfo, pi);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePaymentInfo_ToLongName() throws Exception {
        VOPaymentInfo pi = new VOPaymentInfo();
        pi.setId(BaseAdmUmTest.TOO_LONG_ID);
        PaymentInfo paymentInfo = new PaymentInfo();
        PaymentInfoAssembler.updatePaymentInfo(paymentInfo, pi);
    }
}
