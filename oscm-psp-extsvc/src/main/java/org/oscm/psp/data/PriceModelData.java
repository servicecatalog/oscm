/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Provides the billing data for the price model of an individual subscription
 * which is covered by a customer charging request to a payment service provider
 * (PSP). A price model data object thus corresponds to one position of the
 * invoice sent to the customer.
 */
public class PriceModelData implements Serializable {

    private static final long serialVersionUID = -2540863847060861795L;

    private int position;
    private long startDate;
    private long endDate;
    private BigDecimal netAmount;

    /**
     * Default constructor.
     */
    public PriceModelData() {

    }

    /**
     * Constructs a new price model data object with the given parameters.
     * 
     * @param position
     *            the position number to be used for the price model data on the
     *            invoice sent to the customer
     * @param startDate
     *            the start time of the usage period the customer is charged for
     * @param endDate
     *            the end time of the usage period the customer is charged for
     * @param netAmount
     *            the net amount for the price model to be payed by the customer
     */
    public PriceModelData(int position, long startDate, long endDate,
            BigDecimal netAmount) {
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.netAmount = netAmount;
    }

    /**
     * Returns the position number to be used for the price model data on the
     * invoice sent to the customer
     * 
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position number to be used for the price model data on the
     * invoice sent to the customer
     * 
     * @param position
     *            the position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the start time of the usage period the customer is charged for.
     * 
     * @return the start time
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * Sets the start time of the usage period the customer is charged for.
     * 
     * @param startDate
     *            the start time
     */
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end time of the usage period the customer is charged for.
     * 
     * @return the end time
     */
    public long getEndDate() {
        return endDate;
    }

    /**
     * Sets the end time of the usage period the customer is charged for.
     * 
     * @param endDate
     *            the end time
     */
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the net amount for the price model to be payed by the customer.
     * 
     * @return the net amount
     */
    public BigDecimal getNetAmount() {
        return netAmount;
    }

    /**
     * Sets the net amount for the price model to be payed by the customer.
     * 
     * @param netAmount
     *            the net amount
     */
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

}
