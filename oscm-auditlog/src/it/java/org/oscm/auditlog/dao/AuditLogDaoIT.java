/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.dao;

import static org.oscm.auditlog.matchers.AuditLogMatchers.compareAuditLogEntry;
import static org.oscm.auditlog.matchers.AuditLogMatchers.containCreationTimes;
import static org.oscm.auditlog.matchers.AuditLogMatchers.isSameAs;
import static org.oscm.auditlog.matchers.AuditLogMatchers.notSortedCreationTimes;
import static org.oscm.auditlog.matchers.AuditLogMatchers.sortedCreationTimes;
import static org.oscm.auditlog.util.AuditLogFactory.createAuditLogEntries;
import static org.oscm.auditlog.util.AuditLogFactory.createAuditLogEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;

import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

@SuppressWarnings({ "unchecked", "boxing" })
public class AuditLogDaoIT extends EJBTestBase {
    private AuditLogDao dao;
    private EntityManager em;
    private final List<String> operatorIds = new ArrayList<String>();

    @Override
    protected void setup(TestContainer beanManager) throws Exception {
        beanManager.addBean(new AuditLogDao());
        dao = beanManager.get(AuditLogDao.class);
        em = beanManager.getPersistenceUnit("oscm-auditlog");
    }

    @Test
    public void loadAuditLogs_byOperationName1() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId1");
        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(operatorIds, 0,
                Long.MAX_VALUE);

        // then
        long[] creationTimes = new long[] { 1, 2, 3 };
        assertThat(dbAuditLogs, containCreationTimes("opId1", creationTimes));
    }

    private List<AuditLog> givenAuditLogs() throws Exception {
        return runTX(new Callable<List<AuditLog>>() {
            @Override
            public List<AuditLog> call() {
                List<AuditLog> auditLogs = new LinkedList<AuditLog>();
                auditLogs.add(createAuditLog("opId1", 1));
                em.persist(auditLogs.get(0));

                auditLogs.add(createAuditLog("opId1", 2));
                em.persist(auditLogs.get(1));

                auditLogs.add(createAuditLog("opId1", 3));
                em.persist(auditLogs.get(2));

                auditLogs.add(createAuditLog("opId2", 2));
                em.persist(auditLogs.get(3));
                return auditLogs;
            }

            AuditLog createAuditLog(String operationId, long creationTime) {
                AuditLog auditLog = new AuditLog();
                auditLog.setOperationId(operationId);
                auditLog.setOperationName("opName");
                auditLog.setCreationTime(creationTime);
                auditLog.setLog("abc");
                auditLog.setOrganizationId("orgId");
                auditLog.setOrganizationName("orgName");
                auditLog.setUserId("userId");
                return auditLog;
            }
        });
    }

    List<AuditLog> loadAuditLogsInTx(final List<String> operationName,
            final long startTime, final long endTime) throws Exception {
        return runTX(new Callable<List<AuditLog>>() {
            @Override
            public List<AuditLog> call() {
                return dao.loadAuditLogs(operationName, startTime, endTime);
            }
        });
    }

    @Test
    public void countAuditLogs_byOperationName1() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId1");
        // when
        long cnt = countAuditLogsInTx(operatorIds, 0, Long.MAX_VALUE);

        // then
        assertEquals(3, cnt);
    }

    public long countAuditLogsInTx(final List<String> operationName,
            final long startTime, final long endTime) throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() {
                return dao.countAuditLogs(operationName, startTime, endTime);
            }
        });
    }

    @Test
    public void loadAuditLogs_byOperationName2() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId2");
        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(operatorIds, 0,
                Long.MAX_VALUE);

        // then
        long[] creationTimes = new long[] { 2 };
        assertThat(dbAuditLogs, containCreationTimes("opId2", creationTimes));
    }

    @Test
    public void loadAuditLogs_sortCreationTime() throws Exception {
        // given
        List<AuditLog> givenAuditLogs = givenAuditLogs();
        assertThat(givenAuditLogs, notSortedCreationTimes());

        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(operatorIds, 0,
                Long.MAX_VALUE);

        // then
        assertThat(dbAuditLogs, sortedCreationTimes());
    }

    @Test
    public void countAuditLogs_byOperationName2() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId1");
        // when
        long cnt = countAuditLogsInTx(operatorIds, 0, Long.MAX_VALUE);

        // then
        assertEquals(3, cnt);
    }

    @Test
    public void loadAuditLogs() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId1");
        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(operatorIds, 2, 3);

        // then
        long[] creationTimes = new long[] { 2 };
        assertThat(dbAuditLogs, containCreationTimes("opId1", creationTimes));
    }

    @Test
    public void loadAuditLogs_byNulloperationIds() throws Exception {
        // given
        givenAuditLogs();
        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(null, 2, 3);

        // then
        assertEquals(2, dbAuditLogs.size());
    }

    @Test
    public void loadAuditLogs_byEmptyOperationIds() throws Exception {
        // given
        givenAuditLogs();
        // when
        List<AuditLog> dbAuditLogs = loadAuditLogsInTx(operatorIds, 2, 3);

        // then
        assertEquals(2, dbAuditLogs.size());
    }

    @Test
    public void countAuditLogs_byNullOperationIds() throws Exception {
        // given
        givenAuditLogs();
        // when
        long cnt = countAuditLogsInTx(null, 2, 3);

        // then
        assertEquals(2, cnt);
    }

    @Test
    public void countAuditLogs_byEmptyOperationIds() throws Exception {
        // given
        givenAuditLogs();
        // when
        long cnt = countAuditLogsInTx(operatorIds, 2, 3);

        // then
        assertEquals(2, cnt);
    }

    @Test
    public void countAuditLogs() throws Exception {
        // given
        givenAuditLogs();
        operatorIds.add("opId1");
        // when
        long cnt = countAuditLogsInTx(operatorIds, 2, 3);

        // then
        assertEquals(1, cnt);
    }

    @Test
    public void saveAuditLog() throws Exception {
        // given
        List<AuditLogEntry> logEntries = createAuditLogEntries("opName1", 5);

        // when
        saveAuditLogInTx(logEntries);

        // then
        assertThat(logEntries, isSameAs(loadAuditLogsFromDb()));
    }

    private void saveAuditLogInTx(final List<AuditLogEntry> logEntries)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                dao.saveAuditLog(logEntries);
                return null;
            }
        });
    }

    private List<AuditLog> loadAuditLogsFromDb() throws Exception {
        return runTX(new Callable<List<AuditLog>>() {
            @Override
            public List<AuditLog> call() {
                return em
                        .createQuery("SELECT o FROM AuditLog o ORDER BY o.key")
                        .getResultList();
            }
        });
    }

    @Test
    public void saveAuditLog_checkBatchInsert() {
        // given
        dao = spy(new AuditLogDao());
        dao.batchSize = 2;
        doNothing().when(dao).persist(any(AuditLog.class));
        doNothing().when(dao).flushAndClear();
        List<AuditLogEntry> logEntries = createAuditLogEntries("operationName",
                11);

        // when
        dao.saveAuditLog(logEntries);

        // then
        verify(dao, times(11)).persist(any(AuditLog.class));
        verify(dao, times(6)).flushAndClear();
    }

    @Test
    public void persist() {
        // given
        dao = new AuditLogDao();
        dao.em = mock(EntityManager.class);

        // when
        dao.persist(new AuditLog());

        // then
        verify(dao.em, times(1)).persist(any(AuditLog.class));
    }

    @Test
    public void flushAndClear() {
        // given
        dao = new AuditLogDao();
        dao.em = mock(EntityManager.class);

        // when
        dao.flushAndClear();

        // then
        verify(dao.em, times(1)).flush();
        verify(dao.em, times(1)).clear();
    }

    @Test
    public void createAuditLog() {
        // given
        dao = new AuditLogDao();
        AuditLogEntry logEntry = createAuditLogEntry("operationName");

        // when
        AuditLog auditLog = dao.createAuditLog(logEntry);

        // then
        compareAuditLogEntry(logEntry, auditLog);
    }
}
