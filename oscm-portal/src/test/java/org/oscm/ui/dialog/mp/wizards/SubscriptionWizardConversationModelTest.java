/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 15.05.15 10:18
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.wizards;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversationModel;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSteppedPrice;
import com.google.common.collect.Lists;

/**
 * Created on 2015-05-12.
 */
public class SubscriptionWizardConversationModelTest {

    public static final String URL_MOCK = "url";

    private SubscriptionWizardConversationModel model;

    private void setExternalConfiguratorUrl(String url) {
        model.getService().getVO().setConfiguratorUrl(url);
    }

    private void mockPricedParamererRows() {
        List<PricedParameterRow> serviceParameters = new ArrayList<>();
        PricedParameterRow row = new PricedParameterRow();
        serviceParameters.add(row);
        model.setServiceParameters(serviceParameters);
    }

    private void mockSteppedPricedParametersRows() {
        List<PricedParameterRow> serviceParameters = new ArrayList<>();
        PricedParameterRow row = new PricedParameterRow();
        row.setSteppedPrice(new VOSteppedPrice());
        serviceParameters.add(row);
        model.setServiceParameters(serviceParameters);
    }

    private void mockSteppedPriceServiceEvents() {
        List<PricedEventRow> serviceEvents = new ArrayList<>();
        PricedEventRow row = new PricedEventRow();
        row.setSteppedPrice(new VOSteppedPrice());
        serviceEvents.add(row);
        model.setServiceEvents(serviceEvents);
    }

    @Before
    public void setup() {
        model = new SubscriptionWizardConversationModel();

        VOService service = new VOService();
        model.setService(new Service(service));
    }

    @Test
    public void isConfigurationChanged_changed() {
        // when
        model.setConfigurationChanged(true);

        // then
        assertTrue(model.isConfigurationChanged());
    }

    @Test
    public void isConfigurationChanged_notChanged() {
        // when
        model.setConfigurationChanged(false);

        // then
        assertFalse(model.isConfigurationChanged());
    }

    @Test
    public void getUseExternalConfigurator() {
        // given
        setExternalConfiguratorUrl(URL_MOCK);
        mockPricedParamererRows();

        // when
        boolean usesExternalConfigurator = model.getUseExternalConfigurator();

        // then
        assertTrue(usesExternalConfigurator);
        assertFalse(model.getUseInternalConfigurator());
    }

    @Test
    public void getUseExternalConfigurator_nullParams() {
        // given
        setExternalConfiguratorUrl(URL_MOCK);
        model.setServiceParameters(null);

        // when
        boolean useExternalConfigurator = model.getUseExternalConfigurator();

        // then
        assertFalse(useExternalConfigurator);
        assertFalse(model.getUseInternalConfigurator());
    }

    @Test
    public void getUseExternalConfigurator_emptyParamList() {
        // given
        setExternalConfiguratorUrl(URL_MOCK);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());

        // when
        boolean useExternalConfigurator = model.getUseExternalConfigurator();

        // then
        assertFalse(useExternalConfigurator);
        assertFalse(model.getUseInternalConfigurator());
    }

    @Test
    public void getUseExternalConfigurator_nullUrl() {
        // given
        setExternalConfiguratorUrl(null);
        mockPricedParamererRows();

        // when
        boolean useExternalConfigurator = model.getUseExternalConfigurator();

        // then
        assertFalse(useExternalConfigurator);
        assertTrue(model.getUseInternalConfigurator());
    }

    @Test
    public void getUseExternalConfigurator_emptyUrl() {
        // given
        setExternalConfiguratorUrl("");
        mockPricedParamererRows();

        // when
        boolean useExternalConfigurator = model.getUseExternalConfigurator();

        // then
        assertFalse(useExternalConfigurator);
        assertTrue(model.getUseInternalConfigurator());
    }

    @Test
    public void getUseInternalConfigurator_nullParameters() {
        // given
        setExternalConfiguratorUrl(null);
        model.setServiceParameters(null);

        // when
        boolean useInternalConfigurator = model.getUseInternalConfigurator();

        // then
        assertFalse(useInternalConfigurator);
        assertFalse(model.getUseExternalConfigurator());
    }

    @Test
    public void getUseInternalConfigurator_emptyParameterList() {
        // given
        setExternalConfiguratorUrl(null);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());

        // when
        boolean useInternalConfigurator = model.getUseInternalConfigurator();

        // then
        assertFalse(useInternalConfigurator);
        assertFalse(model.getUseExternalConfigurator());
    }

    @Test
    public void getUseInternalConfigurator() {
        // given
        setExternalConfiguratorUrl(null);
        mockPricedParamererRows();

        // when
        boolean useInternalConfigurator = model.getUseInternalConfigurator();

        // then
        assertTrue(useInternalConfigurator);
        assertFalse(model.getUseExternalConfigurator());
    }

    @Test
    public void isParametersWithSteppedPrices() throws Exception {
        // given
        mockSteppedPricedParametersRows();

        // when
        boolean isSteppedPrice = model.isParametersWithSteppedPrices();

        // then
        assertTrue(isSteppedPrice);

    }

    @Test
    public void isParametersWithSteppedPrices_Empty() throws Exception {
        // given
        model.setServiceParameters(Lists.<PricedParameterRow>newArrayList());

        // when
        boolean isSteppedPrice = model.isParametersWithSteppedPrices();

        // then
        assertFalse(isSteppedPrice);
    }

    @Test
    public void isParametersWithSteppedPrices_Null() throws Exception {
        // given
        model.setServiceParameters(null);

        // when
        boolean isSteppedPrice = model.isParametersWithSteppedPrices();

        // then
        assertFalse(isSteppedPrice);
    }

    @Test
    public void isPricedEventsWithSteppedPrices() throws Exception {
        // given
        mockSteppedPriceServiceEvents();

        // when
        boolean isEventSteppedPrice = model.isPricedEventsWithSteppedPrices();

        // then
        assertTrue(isEventSteppedPrice);
    }

    @Test
    public void isPricedEventWithSteppedPrices_Empty() throws Exception {
        // given
        model.setServiceEvents(Lists.<PricedEventRow>newArrayList());

        // when
        boolean isEventSteppedPrice = model.isPricedEventsWithSteppedPrices();

        // then
        assertFalse(isEventSteppedPrice);
    }

    @Test
    public void isPricedEventWithSteppedPrices_Null() throws Exception {
        // given
        model.setServiceEvents(null);

        // when
        boolean isEventSteppedPrice = model.isPricedEventsWithSteppedPrices();

        // then
        assertFalse(isEventSteppedPrice);
    }
}
