/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.vo.VOPaymentType;

/**
 * @author weiser
 * 
 */
public class PaymentTypeAssemblerTest {
    private LocalizerFacade lf = new LocalizerFacade(null, "") {
        public String getText(long objkey, LocalizedObjectTypes objtype) {
            return objtype + "." + objkey;
        }
    };

    @Test
    public void testToVOPaymentType() throws Exception {
        PaymentType paymentType = new PaymentType();
        paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        paymentType.setKey(12345);
        paymentType.setPaymentTypeId("paymentTypeId");
        VOPaymentType voPaymentType = PaymentTypeAssembler.toVOPaymentType(
                paymentType, lf);
        Assert.assertNotNull("voPaymentType is null", voPaymentType);
        Assert.assertEquals(paymentType.getKey(), voPaymentType.getKey());
        Assert.assertEquals(paymentType.getPaymentTypeId(),
                voPaymentType.getPaymentTypeId());
        Assert.assertEquals(paymentType.getVersion(),
                voPaymentType.getVersion());
        Assert.assertEquals(paymentType.getCollectionType(),
                voPaymentType.getCollectionType());
    }

    @Test
    public void testToVOPaymentTypeNull() throws Exception {
        VOPaymentType voPaymentType = PaymentTypeAssembler.toVOPaymentType(
                null, lf);
        Assert.assertNull("voPaymentType is not null", voPaymentType);
    }

    @Test
    public void testToPaymentType() throws Exception {
        VOPaymentType voPaymentType = new VOPaymentType();
        voPaymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        voPaymentType.setPaymentTypeId("paymentTypeId");
        PaymentType paymentType = PaymentTypeAssembler
                .toPaymentType(voPaymentType);
        Assert.assertNotNull("paymentType is null", paymentType);
        Assert.assertEquals(0, paymentType.getKey());
        Assert.assertEquals(voPaymentType.getPaymentTypeId(),
                paymentType.getPaymentTypeId());
        Assert.assertEquals(0, paymentType.getVersion());
        Assert.assertEquals(voPaymentType.getCollectionType(),
                paymentType.getCollectionType());
    }

    @Test
    public void testToPaymentTypeNull() throws Exception {
        PaymentType paymentType = PaymentTypeAssembler.toPaymentType(null);
        Assert.assertNull("paymentType is not null", paymentType);
    }

    @Test
    public void testVOPaymentType_Identity() throws Exception {
        // test the equals and hasCode implementation of payment type
        VOPaymentType pt1 = new VOPaymentType();
        pt1.setPaymentTypeId(null);
        assertTrue(pt1.equals(pt1));
        VOPaymentType pt2 = new VOPaymentType();
        pt2.setPaymentTypeId(null);
        assertEquals(0, pt1.hashCode());
        assertFalse(pt1.equals(pt2));
        pt2.setPaymentTypeId("ID2");
        assertFalse(pt1.equals(pt2));
        VOPaymentType pt3 = new VOPaymentType();
        pt3.setPaymentTypeId("ID3");
        VOPaymentType pt2a = new VOPaymentType();
        pt2a.setPaymentTypeId("ID2");

        assertTrue(pt2.equals(pt2a));
        assertEquals(pt2.hashCode(), pt2a.hashCode());

        assertFalse(pt2.equals(pt3));
        assertFalse(pt2.hashCode() == pt3.hashCode());
        assertFalse(pt2.equals("HelloWorld"));
    }
}
