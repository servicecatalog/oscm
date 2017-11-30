/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.TriggerProcess;
import org.oscm.ui.model.User;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.vo.VOTriggerProcess;

/**
 * Unit tests for <code>TriggerProcessBean</code>.
 * 
 * @author barzu
 */
public class TriggerProcessBeanTest {

    private TriggerProcessBean triggerProcessBean;

    private List<VOTriggerProcess> allActionsForOrganization;
    private List<VOTriggerProcess> allActions;
    private boolean isOrganizationAdmin;
    private UiDelegate ui;
    private SessionBean sessionBean;

    @Before
    public void setup() {

        // 2 actions started by users in the org of current user
        allActionsForOrganization = new ArrayList<VOTriggerProcess>();
        allActionsForOrganization.add(new VOTriggerProcess());
        allActionsForOrganization.add(new VOTriggerProcess());

        // 1 action started by current user
        allActions = new ArrayList<VOTriggerProcess>();
        VOTriggerProcess record = new VOTriggerProcess();
        record.setKey(1000L);
        allActions.add(record);

        triggerProcessBean = new TriggerProcessBean() {

            private static final long serialVersionUID = -3710613069523333859L;

            @Override
            protected TriggerService getTriggerService() {
                TriggerService triggerService = mock(TriggerService.class);
                when(triggerService.getAllActionsForOrganization()).thenReturn(
                        allActionsForOrganization);
                when(triggerService.getAllActions()).thenReturn(allActions);
                return triggerService;
            }

            @Override
            public User getUserFromSession() {
                User user = mock(User.class);
                when(Boolean.valueOf(user.isOrganizationAdmin())).thenReturn(
                        Boolean.valueOf(isOrganizationAdmin));
                return user;
            }

        };

        sessionBean = mock(SessionBean.class);
        ui = spy(new UiDelegate() {

            public SessionBean findSessionBean() {
                return sessionBean;
            }

        });
        triggerProcessBean.ui = ui;
    }

    @Test
    public void getTriggerProcessList_Admin_OwnActions() throws Exception {
        isOrganizationAdmin = true;
        doReturn(Boolean.TRUE).when(sessionBean).isMyProcessesOnly();
        // initial load of the page:
        triggerProcessBean.getTriggerProcessList();

        triggerProcessBean.setMyProcessesOnly(true);

        assertEquals("Only one action started by the current admin expected",
                1, triggerProcessBean.getTriggerProcessList().size());
    }

    @Test
    public void getTriggerProcessList_NonAdmin_OrgActions() throws Exception {
        isOrganizationAdmin = false;
        // initial load of the page:
        triggerProcessBean.getTriggerProcessList();

        triggerProcessBean.setMyProcessesOnly(false);

        assertEquals(
                "Only the one action started by the current non-admin expected",
                1, triggerProcessBean.getTriggerProcessList().size());
    }

    @Test
    public void getTriggerProcessList_NonAdmin_OwnActions() throws Exception {
        isOrganizationAdmin = false;
        // initial load of the page:
        triggerProcessBean.getTriggerProcessList();

        triggerProcessBean.setMyProcessesOnly(true);

        assertEquals(
                "Only the one action started by the current non-admin expected",
                1, triggerProcessBean.getTriggerProcessList().size());
    }

    @Test
    public void reLoadTriggerProcessList() throws Exception {
        // given
        triggerProcessBean.setTriggerProcessList(prepareTriggerProcesses());
        triggerProcessBean.setMyProcessesOnly(true);
        // when
        triggerProcessBean.reLoadTriggerProcessList(false);

        // then
        // then
        assertEquals(
                Boolean.FALSE,
                Boolean.valueOf(triggerProcessBean.getTriggerProcessList()
                        .get(0).isSelected()));
    }

    @Test
    public void reLoadTriggerProcessList_restoreSelections() throws Exception {
        // given
        triggerProcessBean.setTriggerProcessList(prepareTriggerProcesses());
        triggerProcessBean.setMyProcessesOnly(true);

        // when
        triggerProcessBean.reLoadTriggerProcessList(true);

        // then
        assertEquals(
                Boolean.TRUE,
                Boolean.valueOf(triggerProcessBean.getTriggerProcessList()
                        .get(0).isSelected()));
    }

    private List<TriggerProcess> prepareTriggerProcesses() {
        List<TriggerProcess> triggerRecords = new ArrayList<TriggerProcess>();
        VOTriggerProcess voRecord1 = new VOTriggerProcess();
        voRecord1.setKey(1000L);

        VOTriggerProcess voRecord2 = new VOTriggerProcess();
        voRecord2.setKey(1001L);

        TriggerProcess record1 = new TriggerProcess(voRecord1);
        record1.setSelected(true);

        TriggerProcess record2 = new TriggerProcess(voRecord2);
        record2.setSelected(false);

        triggerRecords.add(record1);
        triggerRecords.add(record2);
        return triggerRecords;
    }
}
