/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                  
 *                                                                              
 *  Creation Date: 07.12.2011                                                      
 *                                                                              
 *  Completion Time: 07.12.2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPaymentType;

/**
 * @author cheld
 * 
 */
public class PaymentTypesTest {

    PaymentTypes given;

    @Before
    public void before() {
        given = new CustomerPaymentTypes(new VOOrganization());
        List<SelectedPaymentType> selected = new ArrayList<SelectedPaymentType>();
        SelectedPaymentType first = new SelectedPaymentType(new VOPaymentType());
        first.getPaymentType().setName("INVOICE");
        first.setSelected(false);
        first.setPaymentTypeId("INVOICE");
        selected.add(first);
        SelectedPaymentType second = new SelectedPaymentType(
                new VOPaymentType());
        second.getPaymentType().setName("CREDIT CARD");
        second.setPaymentTypeId("CREDIT CARD");
        second.setSelected(true);
        selected.add(second);
        given.setPaymentTypes(selected);
    }

    @Test
    public void duplicate() {

        // when duplicating a given payment type
        PaymentTypes copy = given.duplicate();

        // then values must match the original
        SelectedPaymentType firstCopied = copy.getPaymentTypes().get(0);
        assertEquals("INVOICE", firstCopied.getPaymentType().getName());
        assertFalse(firstCopied.isSelected());
        SelectedPaymentType secondCopied = copy.getPaymentTypes().get(1);
        assertEquals("CREDIT CARD", secondCopied.getPaymentType().getName());
        assertTrue(secondCopied.isSelected());
    }

    @Test
    public void isSelectionIdentical_same() {

        // when duplicating a given payment type
        PaymentTypes copy = given.duplicate();

        // then selection must be identical
        assertTrue(copy.isSelectionIdentical(given));
    }

    @Test
    public void isSelectionIdentical_modified() {

        // when duplicating and modifying a given payment type
        PaymentTypes copy = given.duplicate();
        copy.getPaymentTypes().get(0).setSelected(true);

        // then selection must not be identical
        assertFalse(copy.isSelectionIdentical(given));
    }
}
