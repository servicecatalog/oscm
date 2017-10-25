/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import java.util.Date;

/**
 * Factory for providing dates. Subclasses could implement different ways to
 * provide the dates.
 * 
 * @author barzu
 */
public class DateFactory {

    /**
     * Cache for the transaction date.
     */
    protected static final ThreadLocal<Date> transactionTime = new ThreadLocal<Date>();

    static volatile DateFactory instance = new DateFactory();

    StackTraceAnalyzer stackTrace = new StackTraceAnalyzer();

    public void takeCurrentTime() {
        transactionTime.set(new Date());
    }

    public long getTransactionTime() {
        return getTransactionDate().getTime();
    }

    public Date getTransactionDate() {
        Date transactionDate = transactionTime.get();
        if (transactionDate == null) {
            throwException();
        }
        return transactionTime.get();
    }

    /**
     * Throw exception if this class is executed in production. This method call
     * will NOT throw an exception if it executed in a test environment. In this
     * case we set the time implicitly.
     */
    private void throwException() {
        if (stackTrace.containsTestClass()) {
            takeCurrentTime();
        } else {
            throw new IllegalStateException(
                    "No transactime defined. Please call takeCurrentTime() before this method. Maybe the class InvocationDateContainer must be added as interceptor to the bean (the EJB business implementation)");
        }
    }

    protected boolean isTransactionTimeSet() {
        return transactionTime.get() != null;
    }

    public static DateFactory getInstance() {
        return instance;
    }

    public static void setInstance(DateFactory factoryToBeUsed) {
        instance = factoryToBeUsed;
    }
}
