/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.dialog.mp.userGroups;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.usergroupmgmt.POUserGroup;

/**
 * This class contains jUnit tests for SubscriptionUnitCtrl.
 */
public class SubscriptionUnitCtrlTest {
    
    private SubscriptionUnitCtrl subscriptionUnitCtrl;
    private SubscriptionUnitModel subscriptionUnitModel;

    @Before
    public void setup() {
        subscriptionUnitModel = new SubscriptionUnitModel();
        subscriptionUnitCtrl = spy(new SubscriptionUnitCtrl());
        subscriptionUnitCtrl.setModel(subscriptionUnitModel);
    }

    @Test
    public void assignUnitToSubscription() {
        // given
        setUnitsList();
        ValueChangeEvent valueChangeEvent =
                initChangeSelectedUnitEvent(new Long(1));
        // when
        subscriptionUnitCtrl.changeSelectedUnit(valueChangeEvent);
        // then
        for (POUserGroup unit : subscriptionUnitModel.getUnits()) {
            if (unit.isUnitSelected()) {
                assertEquals(Boolean.TRUE, Boolean.valueOf(unit.getKey() == 1));
            }
        }
    }

    @Test
    public void deassignUnit() {
        // given
        setUnitsList();
        ValueChangeEvent valueChangeEvent =
                initChangeSelectedUnitEvent(new Long(0));
        // when
        subscriptionUnitCtrl.changeSelectedUnit(valueChangeEvent);
        // then
        for (POUserGroup unit : subscriptionUnitModel.getUnits()) {
            assertEquals(Boolean.FALSE, Boolean.valueOf(unit.isUnitSelected()));
        }
    }

    private void setUnitsList() {
        List<POUserGroup> units = new ArrayList<POUserGroup>();
        units.add(prepareUnit(new Long(1)));
        units.add(prepareUnit(new Long(2)));
        subscriptionUnitModel.setUnits(units);
    }

    private POUserGroup prepareUnit(Long unitId) {
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setKey((long) unitId);
        return poUserGroup;
    }

    private ValueChangeEvent initChangeSelectedUnitEvent(Long unitId) {
        ValueChangeEvent event = mock(ValueChangeEvent.class);
        when(event.getNewValue()).thenReturn("true");
        HtmlSelectOneRadio radio = mock(HtmlSelectOneRadio.class);
        when(event.getComponent()).thenReturn(radio);
        when(radio.getSubmittedValue()).thenReturn("true");
        HtmlInputHidden input = mock(HtmlInputHidden.class);
        when(input.getValue()).thenReturn(unitId);
        when(input.getRendererType()).thenReturn("javax.faces.Hidden");
        List<UIComponent> componentList = new ArrayList<UIComponent>();
        componentList.add(input);
        when(radio.getChildren()).thenReturn(componentList);
        return event;
    }
}
