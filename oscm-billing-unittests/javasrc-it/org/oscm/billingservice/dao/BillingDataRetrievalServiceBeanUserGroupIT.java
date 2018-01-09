/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 09.06.2015                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.UserGroupHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * @author stavreva
 * 
 */
public class BillingDataRetrievalServiceBeanUserGroupIT extends EJBTestBase {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final long ORGANIZTION_KEY = 1000;
    private static final String UNIT_DESCRIPTION = "Unit description";
    private static final String UNIT_NAME = "Unit name";
    private static final String UNIT_REFERENCE = "Unit reference";
    private static final String UNIT_NAME_MODIFIED = "Unit name modified";
    private static final String UNIT_REFERENCE_MODIFIED = "Unit reference modified";
    private static final String PERIOD_END = "2015-06-15 00:00:00";
    private static final long NON_EXISTING_KEY = 1234567890L;
    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);
    }

    private UserGroupHistory createUserGroupHistory(final long userGroupObjKey,
            final String name, final String referenceID,
            final String modificationDate, final int version,
            final ModificationType modificationType) throws Exception {
        return runTX(new Callable<UserGroupHistory>() {
            @Override
            public UserGroupHistory call() throws Exception {
                UserGroupHistory history = new UserGroupHistory();
                history.setInvocationDate(new Date());
                history.setObjKey(userGroupObjKey);
                history.setObjVersion(version);
                history.setModdate(new SimpleDateFormat(DATE_PATTERN)
                        .parse(modificationDate));
                history.setModtype(modificationType);
                history.setModuser("moduser");
                history.setOrganizationObjKey(Long.valueOf(ORGANIZTION_KEY));
                history.setName(name);
                history.setReferenceId(referenceID);
                history.setDescription(UNIT_DESCRIPTION);
                ds.persist(history);
                return history;
            }
        });
    }

    @Test
    public void getUserGroupHistoryNonExistingKey() throws Exception {
        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(NON_EXISTING_KEY,
                        getPeriodEnd());
            }
        });

        // then
        assertNull(userGroupHistory);
    }

    @Test
    public void getUserGroupHistoryAddBeforeBillingPeriod() throws Exception {
        // given
        final long OBJKEY = 2001L;
        final String MODTIME = "2015-01-05 08:00:00";
        final int VERSION = 0;
        createUserGroupHistory(OBJKEY, UNIT_NAME, UNIT_REFERENCE, MODTIME,
                VERSION, ModificationType.ADD);
        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(OBJKEY, getPeriodEnd());
            }
        });

        // then
        assertNotNull(userGroupHistory);
        assertEquals(OBJKEY, userGroupHistory.getObjKey());
        assertEquals(UNIT_NAME, userGroupHistory.getName());
        assertEquals(UNIT_REFERENCE, userGroupHistory.getReferenceId());
        assertEquals(VERSION, userGroupHistory.getObjVersion());
        assertEquals(new SimpleDateFormat(DATE_PATTERN).parse(MODTIME),
                userGroupHistory.getModdate());
        assertEquals(ModificationType.ADD, userGroupHistory.getModtype());
    }

    @Test
    public void getUserGroupHistoryAddAfterBillingPeriod() throws Exception {
        // given
        final long OBJKEY = 3001L;
        final String MODTIME = "2017-01-05 08:00:00";
        final int VERSION = 0;
        createUserGroupHistory(OBJKEY, UNIT_NAME, UNIT_REFERENCE, MODTIME,
                VERSION, ModificationType.ADD);
        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(OBJKEY, getPeriodEnd());
            }
        });

        // then
        assertNull(userGroupHistory);
    }

    @Test
    public void getUserGroupHistoryModifyBeforeBillingPeriod() throws Exception {
        // given
        final long OBJKEY = 2002L;
        final String MODTIME = "2015-01-05 08:00:00";
        final int VERSION = 0;
        createUserGroupHistory(OBJKEY, UNIT_NAME, UNIT_REFERENCE, MODTIME,
                VERSION, ModificationType.ADD);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, MODTIME, VERSION + 1,
                ModificationType.MODIFY);
        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(OBJKEY, getPeriodEnd());
            }
        });

        // then
        assertNotNull(userGroupHistory);
        assertEquals(OBJKEY, userGroupHistory.getObjKey());
        assertEquals(UNIT_NAME_MODIFIED, userGroupHistory.getName());
        assertEquals(UNIT_REFERENCE_MODIFIED, userGroupHistory.getReferenceId());
        assertEquals(VERSION + 1, userGroupHistory.getObjVersion());
        assertEquals(new SimpleDateFormat(DATE_PATTERN).parse(MODTIME),
                userGroupHistory.getModdate());
        assertEquals(ModificationType.MODIFY, userGroupHistory.getModtype());
    }

    @Test
    public void getUserGroupHistoryModifyAfterBillingPeriod() throws Exception {
        // given
        final long OBJKEY = 4002L;
        final int VERSION = 0;
        createUserGroupHistory(OBJKEY, UNIT_NAME, UNIT_REFERENCE,
                "2015-01-05 08:00:00", VERSION, ModificationType.ADD);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED, UNIT_REFERENCE,
                "2015-01-06 08:50:00", VERSION + 1, ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, "2015-04-06 06:30:00", VERSION + 2,
                ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, "2015-04-06 08:50:00", VERSION + 3,
                ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, "2015-08-01 18:50:00", VERSION + 4,
                ModificationType.MODIFY);

        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(OBJKEY, getPeriodEnd());
            }
        });

        // then
        assertNotNull(userGroupHistory);
        assertEquals(OBJKEY, userGroupHistory.getObjKey());
        assertEquals(UNIT_NAME_MODIFIED, userGroupHistory.getName());
        assertEquals(UNIT_REFERENCE_MODIFIED, userGroupHistory.getReferenceId());
        assertEquals(VERSION + 3, userGroupHistory.getObjVersion());
        assertEquals(
                new SimpleDateFormat(DATE_PATTERN).parse("2015-04-06 08:50:00"),
                userGroupHistory.getModdate());
        assertEquals(ModificationType.MODIFY, userGroupHistory.getModtype());
    }

    @Test
    public void getUserGroupHistoryModifyOnEndBillingPeriod() throws Exception {
        // given
        final long OBJKEY = 4002L;
        final int VERSION = 0;
        createUserGroupHistory(OBJKEY, UNIT_NAME, UNIT_REFERENCE,
                "2015-01-05 08:00:00", VERSION, ModificationType.ADD);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED, UNIT_REFERENCE,
                "2015-01-06 08:50:00", VERSION + 1, ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, "2015-04-06 06:30:00", VERSION + 2,
                ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, "2015-04-06 08:50:00", VERSION + 3,
                ModificationType.MODIFY);
        createUserGroupHistory(OBJKEY, UNIT_NAME_MODIFIED,
                UNIT_REFERENCE_MODIFIED, PERIOD_END, VERSION + 4,
                ModificationType.MODIFY);

        // when
        UserGroupHistory userGroupHistory = runTX(new Callable<UserGroupHistory>() {

            @Override
            public UserGroupHistory call() throws Exception {
                return bdr.getLastValidGroupHistory(OBJKEY, getPeriodEnd());
            }
        });

        // then
        assertNotNull(userGroupHistory);
        assertEquals(OBJKEY, userGroupHistory.getObjKey());
        assertEquals(UNIT_NAME_MODIFIED, userGroupHistory.getName());
        assertEquals(UNIT_REFERENCE_MODIFIED, userGroupHistory.getReferenceId());
        assertEquals(VERSION + 3, userGroupHistory.getObjVersion());
        assertEquals(
                new SimpleDateFormat(DATE_PATTERN).parse("2015-04-06 08:50:00"),
                userGroupHistory.getModdate());
        assertEquals(ModificationType.MODIFY, userGroupHistory.getModtype());
    }

    private long getPeriodEnd() throws ParseException {
        return new SimpleDateFormat(DATE_PATTERN).parse(PERIOD_END).getTime();
    }
}
