/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.model.Discount;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIViewRootStub;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VODiscount;

public class DiscountBeanTest {

    private static final long SERVICE_KEY_NOT_EXISTING = 1;
    private static final long SERVICE_KEY_DISCOUNT_NOT_EXISTING = 2;
    private static final long SERVICE_KEY_FOR_FUTURE_DISCOUNT = 3;
    private static final long SERVICE_KEY_FOR_ACTIVE_DISCOUNT = 4;
    private static final long SERVICE_KEY_FOR_EXPIRED_DISCOUNT = 5;

    private static final String CUSTOMER_KEY_NOT_EXISTING = "1";
    private static final String CUSTOMER_KEY_DISCOUNT_NOT_EXISTING = "2";
    private static final String CUSTOMER_KEY_FOR_FUTURE_DISCOUNT = "3";
    private static final String CUSTOMER_KEY_FOR_ACTIVE_DISCOUNT = "4";
    private static final String CUSTOMER_KEY_FOR_EXPIRED_DISCOUNT = "5";

    private DiscountBean discountBean;
    private VODiscount voDiscountFuture;
    private VODiscount voDiscountActive;
    private VODiscount voDiscountExpired;

    private long currentTime;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    @Before
    public void setup() throws Exception {
        currentTime = System.currentTimeMillis();

        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };
        UIViewRootStub vrStub = mock(UIViewRootStub.class);
        when(vrStub.getLocale()).thenReturn(Locale.ENGLISH);

        contextStub.setViewRoot(vrStub);

        voDiscountFuture = new VODiscount();
        voDiscountFuture.setStartTime(getFutureMonth());
        voDiscountFuture.setValue(BigDecimal.valueOf(3));

        voDiscountActive = new VODiscount();
        voDiscountActive.setStartTime(getCurrentMonth());
        voDiscountActive.setEndTime(getCurrentMonth());
        voDiscountActive.setValue(BigDecimal.valueOf(4));

        voDiscountExpired = new VODiscount();
        voDiscountExpired.setStartTime(getPastMonth());
        voDiscountExpired.setEndTime(getPastMonth());
        voDiscountExpired.setValue(BigDecimal.valueOf(5));

        final DiscountService discountServiceMock = mock(DiscountService.class);
        when(
                discountServiceMock
                        .getDiscountForService(SERVICE_KEY_NOT_EXISTING))
                .thenThrow(new ObjectNotFoundException());
        when(
                discountServiceMock
                        .getDiscountForService(SERVICE_KEY_DISCOUNT_NOT_EXISTING))
                .thenReturn(null);
        when(
                discountServiceMock
                        .getDiscountForService(SERVICE_KEY_FOR_FUTURE_DISCOUNT))
                .thenReturn(voDiscountFuture);
        when(
                discountServiceMock
                        .getDiscountForService(SERVICE_KEY_FOR_ACTIVE_DISCOUNT))
                .thenReturn(voDiscountActive);
        when(
                discountServiceMock
                        .getDiscountForService(SERVICE_KEY_FOR_EXPIRED_DISCOUNT))
                .thenReturn(voDiscountExpired);

        when(
                discountServiceMock
                        .getDiscountForCustomer(CUSTOMER_KEY_NOT_EXISTING))
                .thenThrow(new ObjectNotFoundException());
        when(
                discountServiceMock
                        .getDiscountForCustomer(CUSTOMER_KEY_DISCOUNT_NOT_EXISTING))
                .thenReturn(null);
        when(
                discountServiceMock
                        .getDiscountForCustomer(CUSTOMER_KEY_FOR_FUTURE_DISCOUNT))
                .thenReturn(voDiscountFuture);
        when(
                discountServiceMock
                        .getDiscountForCustomer(CUSTOMER_KEY_FOR_ACTIVE_DISCOUNT))
                .thenReturn(voDiscountActive);
        when(
                discountServiceMock
                        .getDiscountForCustomer(CUSTOMER_KEY_FOR_EXPIRED_DISCOUNT))
                .thenReturn(voDiscountExpired);

        discountBean = new DiscountBean() {
            private static final long serialVersionUID = 209223415782524273L;

            protected DiscountService getDiscountService() {
                return discountServiceMock;
            };
        };

    }

    @Test
    public void testGetDiscount_ObjectNotFoundException() {
        facesMessages = new ArrayList<FacesMessage>();
        Discount result = discountBean.getDiscount(SERVICE_KEY_NOT_EXISTING);
        assertEquals(1, facesMessages.size());
        assertNull(result);
    }

    @Test
    public void testGetDiscount_DiscountNotExisting() {
        Discount result = discountBean
                .getDiscount(SERVICE_KEY_DISCOUNT_NOT_EXISTING);
        assertNull(result);
    }

    @Test
    public void testGetDiscount_DiscountFuture() {
        Discount result = discountBean
                .getDiscount(SERVICE_KEY_FOR_FUTURE_DISCOUNT);
        assertNull(result);
    }

    @Test
    public void testGetDiscount_DiscountActive() {
        Discount result = discountBean
                .getDiscount(SERVICE_KEY_FOR_ACTIVE_DISCOUNT);
        assertEquals(BigDecimal.valueOf(4), result.getVO().getValue());
    }

    @Test
    public void testGetDiscount_DiscountExpired() {
        Discount result = discountBean
                .getDiscount(SERVICE_KEY_FOR_EXPIRED_DISCOUNT);
        assertNull(result);
    }

    @Test
    public void testGetDiscountForCustomer_ObjectNotFoundException() {
        facesMessages = new ArrayList<FacesMessage>();
        Discount result = discountBean
                .getDiscountForCustomer(CUSTOMER_KEY_NOT_EXISTING);
        assertEquals(1, facesMessages.size());
        assertNull(result);
    }

    @Test
    public void testGetDiscountForCustomer_DiscountNotExisting() {
        Discount result = discountBean
                .getDiscountForCustomer(CUSTOMER_KEY_DISCOUNT_NOT_EXISTING);
        assertNull(result);
    }

    @Test
    public void testGetDiscountForCustomer_DiscountFuture() {
        Discount result = discountBean
                .getDiscountForCustomer(CUSTOMER_KEY_FOR_FUTURE_DISCOUNT);
        assertNull(result);
    }

    @Test
    public void testGetDiscountForCustomer_DiscountActive() {
        Discount result = discountBean
                .getDiscountForCustomer(CUSTOMER_KEY_FOR_ACTIVE_DISCOUNT);
        assertEquals(BigDecimal.valueOf(4), result.getVO().getValue());
    }

    @Test
    public void testGetDiscountForCustomer_DiscountExpired() {
        Discount result = discountBean
                .getDiscountForCustomer(CUSTOMER_KEY_FOR_EXPIRED_DISCOUNT);
        assertNull(result);
    }

    private Long getPastMonth() {
        return Long.valueOf(getTimeInMillisForFirstDay(currentTime, -2));
    }

    private Long getCurrentMonth() {
        return Long.valueOf(getTimeInMillisForFirstDay(currentTime, 0));
    }

    private Long getFutureMonth() {
        return Long.valueOf(getTimeInMillisForFirstDay(currentTime, +2));
    }

    private long getTimeInMillisForFirstDay(long timeInMilis, int addedMonth) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(timeInMilis);

        currentCalendar.add(Calendar.MONTH, addedMonth);
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        return currentCalendar.getTimeInMillis();
    }
}
