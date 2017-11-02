/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.Calendar;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.model.Discount;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VODiscount;

/**
 * Backing bean for discount related actions.
 * 
 * @author tokoda
 */
@ViewScoped
@ManagedBean(name="discountBean")
public class DiscountBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -8252170979945981894L;

    /**
     * Returns the current logged-in customer discount, which is currently
     * active or starting in the future.
     * 
     * @param serviceKey
     *            the service key for which to get the discount
     * 
     * @return the Discount (ui model wrapper) of the logged-in customer
     */
    public Discount getDiscount(long serviceKey) {

        Discount discount = null;
        try {
            VODiscount voDiscount = getDiscountService().getDiscountForService(
                    serviceKey);
            if (voDiscount != null) {
                if (isDiscountActive(voDiscount)) {
                    discount = new Discount(voDiscount);
                }
            }
        } catch (ObjectNotFoundException ex) {
            ExceptionHandler.execute(ex);
        }

        return discount;
    }

    /**
     * Returns the customer discount of the current logged-in supplier, which is
     * currently active or starting in the future.
     * 
     * @param customerId
     *            the customer Id for which to get the discount
     * 
     * @return the Discount (ui model wrapper) of the customer of the logged-in
     *         supplier
     */
    public Discount getDiscountForCustomer(String customerId) {

        Discount discount = null;
        try {
            VODiscount voDiscount = getDiscountService()
                    .getDiscountForCustomer(customerId);
            if (voDiscount != null) {
                if (isDiscountActive(voDiscount)) {
                    discount = new Discount(voDiscount);
                }
            }
        } catch (ObjectNotFoundException ex) {
            ExceptionHandler.execute(ex);
        }

        return discount;
    }

    private boolean isDiscountActive(VODiscount discount) {
        if (discount == null) {
            return false;
        }

        long currentTimeMonthYear = getTimeInMillisForFirstDay(System
                .currentTimeMillis());
        if (discount.getStartTime() == null
                || discount.getStartTime().longValue() > currentTimeMonthYear
                || (discount.getEndTime() != null && discount.getEndTime()
                        .longValue() < currentTimeMonthYear)) {

            return false;
        } else {
            return true;
        }
    }

    /**
     * Getting millisecond of the first day in month.
     * 
     * @param timeInMilis
     *            Time of any day of month.
     * @return First millisecond of month.
     */
    private long getTimeInMillisForFirstDay(long timeInMilis) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(timeInMilis);

        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        return currentCalendar.getTimeInMillis();
    }
}
