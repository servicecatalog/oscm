/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 16.04.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.internal.types.exception.SubscriptionStateException;

/**
 * @author goebel
 * 
 */
public class SubscriptionStateExceptionConvertTest {

    @Test
    public void SubscriptionStateException_ONLY_UPD_convertToApis() {
        // given
        SubscriptionStateException e = givenStateException(SubscriptionStateException.Reason.ONLY_UPD);

        // when
        org.oscm.types.exceptions.SubscriptionStateException apiEx = ExceptionConverter
                .convertToApi(e);

        // then
        assertEquals(
                org.oscm.types.exceptions.SubscriptionStateException.Reason.ONLY_UPD,
                apiEx.getReason());
    }

    @Test
    public void SubscriptionStateException_NOT_IN_UPDATING_convertToApis() {
        // given
        SubscriptionStateException e = givenStateException(SubscriptionStateException.Reason.NOT_IN_UPDATING);

        // when
        org.oscm.types.exceptions.SubscriptionStateException apiEx = ExceptionConverter
                .convertToApi(e);

        // then
        assertEquals(
                org.oscm.types.exceptions.SubscriptionStateException.Reason.NOT_IN_UPDATING,
                apiEx.getReason());
    }

    @Test
    public void SubscriptionStateException_NOT_IN_UPGRADING_convertToApis() {
        // given
        SubscriptionStateException e = givenStateException(SubscriptionStateException.Reason.NOT_IN_UPGRADING);

        // when
        org.oscm.types.exceptions.SubscriptionStateException apiEx = ExceptionConverter
                .convertToApi(e);

        // then
        assertEquals(
                org.oscm.types.exceptions.SubscriptionStateException.Reason.NOT_IN_UPGRADING,
                apiEx.getReason());
    }

    private SubscriptionStateException givenStateException(
            SubscriptionStateException.Reason reason) {
        return new SubscriptionStateException(reason, "member",
                new Object[] { "1" });
    }

}
