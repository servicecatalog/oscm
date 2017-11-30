/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Locale;

import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.accountmgmt.AccountServiceManagement;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOOrganization;

public class ManageBillingBeanTest {

    private ManageBillingBean manageBillingBean;
    private int selectItemRange = 28;
    private int cutOffDay;
    private VOOrganization voOrg;
    private AccountServiceManagement asMgmt;
    private UiDelegate ui;

    @Before
    public void setup() throws Exception {
        new FacesContextStub(Locale.ENGLISH);
        voOrg = new VOOrganization();
        voOrg.setKey(1);
        manageBillingBean = spy(new ManageBillingBean());
        asMgmt = mock(AccountServiceManagement.class);
        ui = mock(UiDelegate.class);
        manageBillingBean.ui = ui;
        doReturn(asMgmt).when(manageBillingBean).getAccountServiceManagement();
        doReturn(voOrg).when(asMgmt).getOrganizationData();
    }

    @Test
    public void getDayInMonthRange_Ok() {
        // when
        List<SelectItem> result = manageBillingBean.getDayInMonthRange();
        // then
        assertEquals(selectItemRange, result.size());
        assertEquals("1", result.get(0).getLabel());
        assertEquals("28", result.get(27).getLabel());
    }

    @Test
    public void getAndSetCutOffDay_Ok() {
        // when
        cutOffDay = 12;
        manageBillingBean.setCutOffDay(cutOffDay);
        int result = manageBillingBean.getCutOffDay();
        // then
        assertEquals(12, result);
    }

    @Test
    public void save_Ok() throws Exception {
        // when
        cutOffDay = 12;
        manageBillingBean.getInitializeCutoffDay();
        manageBillingBean.setCutOffDay(cutOffDay);
        // when
        doNothing().when(asMgmt).setCutOffDayOfOrganization(cutOffDay, voOrg);
        manageBillingBean.save();
        // given
        verify(asMgmt, times(1)).setCutOffDayOfOrganization(eq(cutOffDay),
                eq(voOrg));
    }

    @Test
    public void save_ConcurrentModificationException() throws Exception {
        cutOffDay = 12;
        manageBillingBean.getInitializeCutoffDay();
        manageBillingBean.setCutOffDay(cutOffDay);
        doThrow(new ConcurrentModificationException())
                .when(asMgmt)
                .setCutOffDayOfOrganization(anyInt(), any(VOOrganization.class));
        doReturn(voOrg).when(asMgmt).getOrganizationData();
        String result = manageBillingBean.save();
        verify(asMgmt, times(1)).setCutOffDayOfOrganization(eq(cutOffDay),
                eq(voOrg));
        verify(ui, times(1)).handleException(
                any(ConcurrentModificationException.class));
        assertEquals(result, BaseBean.OUTCOME_ERROR);
    }

}
