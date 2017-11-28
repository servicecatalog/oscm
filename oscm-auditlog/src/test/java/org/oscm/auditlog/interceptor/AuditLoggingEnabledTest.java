/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.interceptor;

import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.types.constants.Configuration;

import javax.interceptor.InvocationContext;
import javax.persistence.TypedQuery;

import static org.mockito.Mockito.*;

public class AuditLoggingEnabledTest {
    private AuditLoggingEnabled auditLoggingEnabled;

    @Test
    public void isLoggingEnabled_true() throws Exception {
        // given
        AuditLoggingEnabled auditLoggingEnabled = mockAuditLoggingIsEnabled(
                true);
        InvocationContext context = mock(InvocationContext.class);

        // when
        auditLoggingEnabled.isLoggingEnabled(context);

        // then
        verify(context, times(1)).proceed();
    }

    private AuditLoggingEnabled mockAuditLoggingIsEnabled(boolean isEnabled) {
        auditLoggingEnabled = spy(new AuditLoggingEnabled());
        auditLoggingEnabled.dm = mock(DataService.class);

        @SuppressWarnings("unchecked")
        TypedQuery<ConfigurationSetting> typedQuery = mock(TypedQuery.class);

        doReturn(typedQuery).when(auditLoggingEnabled.dm).createNamedQuery(
                "ConfigurationSetting.findByInfoAndContext",
                ConfigurationSetting.class);

        doReturn(auditLogIsEnabled(isEnabled)).when(typedQuery)
                .getSingleResult();

        return auditLoggingEnabled;
    }

    private ConfigurationSetting auditLogIsEnabled(boolean isEnabled) {
        return new ConfigurationSetting(ConfigurationKey.AUDIT_LOG_ENABLED,
                Configuration.GLOBAL_CONTEXT, Boolean.toString(isEnabled));
    }

    @Test
    public void isLoggingEnabled_false() throws Exception {
        // given
        AuditLoggingEnabled auditLoggingEnabled = mockAuditLoggingIsEnabled(
                false);
        InvocationContext context = mock(InvocationContext.class);

        // when
        auditLoggingEnabled.isLoggingEnabled(context);

        // then
        verify(context, times(0)).proceed();
    }
}
