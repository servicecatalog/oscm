/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.util.Date;

import org.oscm.interceptor.DateFactory;

/**
 * Subclass of {@link DateFactory} that allows manipulation of dates for test
 * purposes. This class is useful for example for billing tests, where we need
 * to create objects on specific dates.
 * 
 * @author barzu
 */
public class TestDateFactory extends DateFactory {

	public TestDateFactory() {
	}

	/**
	 * @param transactionDate
	 *            allows setting of the current date in the past or future.
	 */
	public TestDateFactory(Date transactionDate) {
		transactionTime.set(transactionDate);
	}

	@Override
	public void takeCurrentTime() {

	}

	/**
	 * @param transactionDate
	 *            allows setting of the current date in the past or future.
	 */
	public void setTransactionTime(Date transactionDate) {
		transactionTime.set(transactionDate);
	}

    /**
     * The class DateFactory is used to create the history modification
     * timestamps. Sometimes test cases redefine the behavior to set artificial
     * data in the history tables. This method restores the normal date factory.
     */
    public static void restoreDefault() {
        DateFactory.setInstance(new DateFactory());
    }

}
