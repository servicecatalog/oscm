/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.After;
import org.junit.Test;

/**
 * @author malhotra
 * 
 */
public class DateFactoryTest {

    @After
    public void after() {
        DateFactory.transactionTime.set(null);
        DateFactory.getInstance().stackTrace = new StackTraceAnalyzer();
    }

    @Test(expected = IllegalStateException.class)
    public void getTransactionDate_noTimeSetInProduction() {

        // given the date factory is executed in production and date is not set
        StackTraceAnalyzer info = mock(StackTraceAnalyzer.class);
        DateFactory.getInstance().stackTrace = mock(StackTraceAnalyzer.class);
        given(Boolean.valueOf(info.containsTestClass())).willReturn(
                Boolean.valueOf(false));

        // when fetching transaction time then exception is thrown
        DateFactory.getInstance().getTransactionDate();
    }

    @Test
    public void getTransactionDate_noTimeSetInTest() {

        // given the date factory is executed in production and date is not set

        // when fetching transaction time
        Date transactionTime = DateFactory.getInstance().getTransactionDate();

        // then
        assertNotNull(transactionTime);
    }

}
