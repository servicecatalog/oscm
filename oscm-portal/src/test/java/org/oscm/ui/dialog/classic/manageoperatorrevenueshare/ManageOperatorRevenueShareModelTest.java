/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageoperatorrevenueshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.pricing.PORevenueShare;

/**
 * Unit tests for ManageOperatorRevenueShareModel.
 * 
 * @author barzu
 */
public class ManageOperatorRevenueShareModelTest {

    private ManageOperatorRevenueShareModel model;

    @Before
    public void setup() {
        model = new ManageOperatorRevenueShareModel();
    }

    @Test
    public void getTemplates() {
        // given
        List<SelectItem> items = new ArrayList<SelectItem>();
        model.setTemplates(items);

        // when, then
        assertEquals(items, model.getTemplates());
    }

    @Test
    public void getSelectedTemplateKey() {
        // given
        model.setSelectedTemplateKey(101L);

        // when, then
        assertEquals(101L, model.getSelectedTemplateKey());
    }

    @Test
    public void getOperatorRevenueShare() {
        // given
        PORevenueShare rs = new PORevenueShare();
        model.setOperatorRevenueShare(rs);

        // when, then
        assertEquals(rs, model.getOperatorRevenueShare());
    }

    @Test
    public void getDefaultOperatorRevenueShare() {
        // given
        PORevenueShare rs = new PORevenueShare();
        model.setDefaultOperatorRevenueShare(rs);

        // when, then
        assertEquals(rs, model.getDefaultOperatorRevenueShare());
    }

    @Test
    public void isServiceSelected_true() {
        // given
        model.setSelectedTemplateKey(101L);

        // when, then
        assertTrue(model.isServiceSelected());
    }

    @Test
    public void isServiceSelected_false() {
        // given
        model.setSelectedTemplateKey(0L);

        // when, then
        assertFalse(model.isServiceSelected());
    }

    @Test
    public void isSaveDisabled_true() {
        // given
        model.setSelectedTemplateKey(0L);

        // when, then
        assertTrue(model.isSaveDisabled());
    }

    @Test
    public void isSaveDisabled_false() {
        // given
        model.setSelectedTemplateKey(101L);

        // when, then
        assertFalse(model.isSaveDisabled());
    }

}
