/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.internal.vo.VOUserDetails;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author mutian
 * 
 */
public class SteppedPriceBeanTest {
    private SteppedPriceBean bean;
    private PriceModelBean priceModelBean;
    private VOPriceModel voPriceModel;
    private List<VOSteppedPrice> steps;
    private List<PricedEventRow> events;
    private List<PricedParameterRow> params;
    private VOPricedParameter parameter;

    private VOPricedEvent pricedEvent;
    private PricedEventRow eventRow;
    private PricedParameterRow parameterRow;
    private VOUserDetails voUserDetails;
    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
    private VOParameter voParam;
    private VOParameterDefinition voParamDef;

    @Before
    public void setup() {
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };

        bean = spy(new SteppedPriceBean());

        priceModelBean = mock(PriceModelBean.class);
        voPriceModel = mock(VOPriceModel.class);

        steps = new LinkedList<VOSteppedPrice>();
        when(priceModelBean.getSteppedPrices()).thenReturn(steps);

        eventRow = new PricedEventRow();
        events = new LinkedList<PricedEventRow>();
        pricedEvent = mock(VOPricedEvent.class);
        eventRow.setPricedEvent(pricedEvent);
        events.add(eventRow);
        when(priceModelBean.getPricedEvents()).thenReturn(events);

        parameterRow = spy(new PricedParameterRow());
        voParam = new VOParameter();
        voParamDef = new VOParameterDefinition();
        voParam.setParameterDefinition(voParamDef);
        when(parameterRow.getParameter()).thenReturn(voParam);
        params = new LinkedList<PricedParameterRow>();
        parameter = mock(VOPricedParameter.class);
        parameterRow.setPricedParameter(parameter);
        params.add(parameterRow);
        when(priceModelBean.getParameters()).thenReturn(params);

        doReturn(new BigDecimal(100)).when(voPriceModel)
                .getPricePerUserAssignment();
        when(priceModelBean.getPriceModel()).thenReturn(voPriceModel);
        bean.setPriceModelBean(priceModelBean);

        voUserDetails = mock(VOUserDetails.class);
        bean.setUserInSession(voUserDetails);
    }

    @Test
    public void add_addToPriceModel_init() {
        // given
        bean.setType("typePriceModel");
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // init create 2 steps
        assertEquals(2, steps.size());
    }

    @Test
    public void add_addToPriceModel_more() {
        // given
        bean.setType("typePriceModel");
        bean.add();
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // after init, every add() method create one step
        assertEquals(3, steps.size());
    }

    @Test
    public void remove_removeFromPriceModel_nothing() {
        // given
        bean.setType("typePriceModel");
        assertEquals(0, steps.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // no step removed
        assertEquals(0, steps.size());
        assertEquals(0, bean.getIndex());
    }

    @Test
    public void remove_removeFromPriceModel_last() {
        // given
        bean.setType("typePriceModel");
        bean.add();
        assertEquals(2, steps.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // last step removed
        assertEquals(0, steps.size());
        assertEquals(0, bean.getIndex());
    }

    @Test
    public void remove_removeFromPriceModel_more() {
        // given
        bean.setType("typePriceModel");
        bean.add();
        bean.add();
        assertEquals(3, steps.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(2, steps.size());
        assertEquals(1, bean.getIndex());
    }

    @Test
    public void add_addToEvents_nothing() {
        // given
        events.clear();
        bean.setType("typePricedEvent");
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(0, events.size());
    }

    @Test
    public void add_addToEvents_init() {
        // given
        bean.setType("typePricedEvent");
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(2, events.size());
    }

    @Test
    public void add_addToEvents_more() {
        // given
        bean.setType("typePricedEvent");
        bean.add();
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(3, events.size());
    }

    @Test
    public void remove_removeFromEvents_nothing() {
        // given
        events.clear();
        bean.setType("typePricedEvent");
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(0, events.size());
    }

    @Test
    public void remove_removeFromEvents_last() {
        // given
        bean.setType("typePricedEvent");
        assertEquals(1, events.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // cannot remove last Event
        assertEquals(1, events.size());
    }

    @Test
    public void remove_removeFromEvents_more() {
        // given
        bean.setType("typePricedEvent");
        bean.add();
        assertEquals(2, events.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(1, events.size());
    }

    @Test
    public void add_addToParameters_noParams() {
        // given
        params.clear();
        bean.setType("typePricedParameter");
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(0, params.size());
    }

    @Test
    public void add_addToParameters_init() {
        // given
        bean.setType("typePricedParameter");
        // when
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(2, params.size());
    }

    @Test
    public void add_addToParameters_more() {
        // given
        bean.setType("typePricedParameter");
        // when
        bean.add();
        String result = bean.add();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(3, params.size());
    }

    @Test
    public void remove_removeFromParameters_nothing() {
        // given
        params.clear();
        bean.setType("typePricedParameter");
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(0, params.size());
    }

    @Test
    public void remove_removeFromParameters_last() {
        // given
        bean.setType("typePricedParameter");
        assertEquals(1, params.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        // last Parameter should not be removed
        assertEquals(1, params.size());
    }

    @Test
    public void remove_removeFromParameters_more() {
        // given
        bean.setType("typePricedParameter");
        bean.add();
        assertEquals(2, params.size());
        // when
        String result = bean.remove();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(1, params.size());
    }
}
