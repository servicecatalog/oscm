/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.auditlog.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.oscm.auditlog.util.AuditLogFactory.createAuditLogEntries;
import static org.oscm.internal.types.enumtypes.ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED;
import static org.oscm.types.constants.Configuration.GLOBAL_CONTEXT;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

import org.junit.Test;
import org.oscm.auditlog.dao.AuditLogDao;
import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntries;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.auditlog.util.AuditLogSerializer;
import org.oscm.configurationservice.bean.ConfigurationServiceBean;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.exception.AuditLogTooManyRowsException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.cdi.ContextManager;
import org.oscm.test.cdi.ObserverMethod;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

@SuppressWarnings("boxing")
public class AuditLogServiceBeanIT extends EJBTestBase {
    private AuditLogServiceBean logService;
    private ContextManager contextManager;
    private final List<String> operatorIds = new ArrayList<>();

    @Override
    protected void setup(TestContainer beanManager) throws Exception {
        beanManager.addBean(new ConfigurationServiceStub());
        beanManager.addBean(new DataServiceBean());
        beanManager.addBean(new ConfigurationServiceBean());
        beanManager.addBean(spy(new AuditLogDao()));
        beanManager.addBean(new AuditLogServiceBean());
        logService = spy(beanManager.get(AuditLogServiceBean.class));
        this.contextManager = beanManager.getContextManager();
    }

    @Test
    public void loadAuditLogs() throws Exception {
        // given
        List<AuditLog> auditLogs = new ArrayList<>();
        AuditLogServiceBean logService = mockAuditLogServiceBean(auditLogs);

        // when
        logService.loadAuditLogs(anyListOf(String.class), anyLong(), anyLong());

        // then
        verify(logService.createAuditLogSerializer(), times(1))
                .serialize(eq(auditLogs));
    }

    private AuditLogServiceBean mockAuditLogServiceBean(
            List<AuditLog> auditLogs) {
        doReturn(mock(AuditLogSerializer.class)).when(logService)
                .createAuditLogSerializer();
        logService.dao = mock(AuditLogDao.class);
        doReturn(auditLogs).when(logService.dao)
                .loadAuditLogs(anyListOf(String.class), anyLong(), anyLong());
        doReturn(100L).when(logService).getMaxAuditlogs();
        return logService;
    }

    @Test(expected = AuditLogTooManyRowsException.class)
    public void loadAuditLogs_tooManyRows() throws Exception {
        // given
        operatorIds.add("30000");
        doReturn(2L).when(logService).getMaxAuditlogs();
        givenAuditLogEntries("opName1", 3);

        // when
        loadAuditLogsInTx(operatorIds, 0, Long.MAX_VALUE);
    }

    private List<AuditLogEntry> givenAuditLogEntries(String operationName,
            int numberLogEntries) throws Exception {
        List<AuditLogEntry> logEntries = createAuditLogEntries(operationName,
                numberLogEntries);
        saveAuditLogInTx(logEntries);
        return logEntries;
    }

    private void saveAuditLogInTx(final List<AuditLogEntry> logEntries)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                logService.dao.saveAuditLog(logEntries);
                return null;
            }
        });
    }

    private void logInTx(final List<AuditLogEntry> logEntries)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                logService.log(logEntries);
                return null;
            }
        });
    }

    private byte[] loadAuditLogsInTx(final List<String> operationIds,
            final long startTime, final long endTime) throws Exception {
        return runTX(new Callable<byte[]>() {
            @Override
            public byte[] call() throws AuditLogTooManyRowsException {
                return logService.loadAuditLogs(operationIds, startTime,
                        endTime);
            }
        });
    }

    @Test
    public void loadAuditLogs_givenLogEntriesSameAsMaxAuditLogs()
            throws Exception {
        // given
        operatorIds.add("30000");
        doReturn(2L).when(logService).getMaxAuditlogs();
        givenAuditLogEntries("opName1", 2);

        // when
        loadAuditLogsInTx(operatorIds, 0, Long.MAX_VALUE);

        // then no exception
    }

    @Test
    public void loadAuditLogs_givenLogEntriesIsLessThanMaxAuditLogs()
            throws Exception {
        // given
        operatorIds.add("30000");
        doReturn(3L).when(logService).getMaxAuditlogs();
        givenAuditLogEntries("opName1", 2);

        // when
        loadAuditLogsInTx(operatorIds, 0, Long.MAX_VALUE);

        // then no exception
    }

    @Test
    public void log() throws Exception {
        // given
        List<AuditLogEntry> logEntries = createAuditLogEntries("opName1", 2);

        // when
        logInTx(logEntries);

        // then
        verify(logService.dao, times(1)).saveAuditLog(eq(logEntries));
    }

    public void log_isAsynchronous() throws Exception {
        // given
        Method method = AuditLogServiceBean.class.getMethod("log");

        // when
        boolean isAsynchronous = method.isAnnotationPresent(Asynchronous.class);

        // then
        assertTrue(isAsynchronous);
    }

    @Test
    public void saveAuditLogEntries_hasObserverMethodAfterSuccessAnnotation() {
        // given
        AuditLogEntries auditLogEntries = new AuditLogEntries(
                new ArrayList<AuditLogEntry>());
        List<ObserverMethod> observerMethods = contextManager
                .findObserverMethods(auditLogEntries);
        Method method = observerMethods.get(0).getMethod();

        // when
        Observes observes = (Observes) contextManager.searchAnnotation(method,
                Observes.class);

        // then
        assertEquals(TransactionPhase.AFTER_SUCCESS, observes.during());
    }

    @Test
    public void getMaxAuditlogs() {
        // given
        logService.configService = mock(ConfigurationServiceLocal.class);
        ConfigurationSetting setting = new ConfigurationSetting(
                AUDIT_LOG_MAX_ENTRIES_RETRIEVED, GLOBAL_CONTEXT, "876");
        doReturn(setting).when(logService.configService)
                .getConfigurationSetting(eq(AUDIT_LOG_MAX_ENTRIES_RETRIEVED),
                        eq(GLOBAL_CONTEXT));

        // when
        long maxAuditLogs = logService.getMaxAuditlogs();

        // then
        assertEquals(876, maxAuditLogs);
    }
}
