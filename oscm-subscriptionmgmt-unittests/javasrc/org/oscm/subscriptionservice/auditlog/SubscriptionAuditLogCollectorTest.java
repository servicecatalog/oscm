/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import static org.oscm.test.matchers.BesMatchers.containsInterceptor;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.oscm.interceptor.AuditLoggingEnabled;

public class SubscriptionAuditLogCollectorTest {
    @Test
    public void isAuditLoggingEnabledInterceptorDefined() {
        assertThat(SubscriptionAuditLogCollector.class,
                containsInterceptor(AuditLoggingEnabled.class));
    }
}
