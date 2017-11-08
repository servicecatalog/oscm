/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Sep 19, 2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TimerType;

/**
 * @author tokoda
 * 
 */
public class Log4jLoggerTest {

    private Log4jLogger logger;
    private Log4jLogger loggerLocaleTest;

    @Before
    public void setup() {
        logger = new Log4jLogger(Log4jLogger.class, Locale.getDefault());
        loggerLocaleTest = new Log4jLogger(Log4jLogger.class,
                new Locale("test"));
    }

    @Test
    public void testIsDebugEnabled() {
        assertFalse(logger.isDebugLoggingEnabled());
    }

    @Test
    public void testlogDebug() {
        logger.logDebug("test");
    }

    @Test
    public void testlogDebug_SystemLog() {
        logger.logDebug("test", Log4jLogger.SYSTEM_LOG);
    }

    @Test
    public void testlogDebug_AccessLog() {
        logger.logDebug("test", Log4jLogger.ACCESS_LOG);
    }

    @Test
    public void testlogDebug_AuditLog() {
        logger.logDebug("test", Log4jLogger.AUDIT_LOG);
    }

    @Test
    public void testlogError_String() {
        logger.logError(LogMessageIdentifier.DEBUG, "test");
    }

    @Test
    public void testlogError_StringArray() {
        String[] params = { "test", "value" };
        logger.logError(LogMessageIdentifier.DEBUG, params);
    }

    @Test
    public void testlogError_SystemLog() {
        logger.logError(Log4jLogger.SYSTEM_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogError_AccessLog() {
        logger.logError(Log4jLogger.ACCESS_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogError_AuditLog() {
        logger.logError(Log4jLogger.AUDIT_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogWarning_SystemLog() {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogWarning_AccessLog() {
        logger.logWarn(Log4jLogger.ACCESS_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogWarning_AuditLog() {
        logger.logWarn(Log4jLogger.AUDIT_LOG, new Exception("test"),
                LogMessageIdentifier.DEBUG);
    }

    @Test
    public void testlogWarning1_SystemLog() {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, LogMessageIdentifier.DEBUG,
                "test");
    }

    @Test
    public void testlogWarning1_AccessLog() {
        logger.logWarn(Log4jLogger.ACCESS_LOG, LogMessageIdentifier.DEBUG,
                "test");
    }

    @Test
    public void testlogWarning1_AuditLog() {
        logger.logWarn(Log4jLogger.AUDIT_LOG, LogMessageIdentifier.DEBUG,
                "test");
    }

    @Test
    public void testlogWarning_Array_SystemLog() {
        String[] params = { "test", "value" };
        logger.logWarn(Log4jLogger.SYSTEM_LOG, LogMessageIdentifier.DEBUG,
                params);
    }

    @Test
    public void testlogWarning_Array_AccessLog() {
        logger.logWarn(Log4jLogger.ACCESS_LOG, LogMessageIdentifier.DEBUG,
                "test", "value");
    }

    @Test
    public void testlogWarning_Array_AuditLog() {
        logger.logWarn(Log4jLogger.AUDIT_LOG, LogMessageIdentifier.DEBUG,
                "test", "value");
    }

    @Test
    public void testlogInfo_SystemLog() {
        logger.logInfo(
                Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.INFO_NO_EMAIL_ADDRESS_SPECIFIED_ORGANIZATION,
                "test");
    }

    @Test
    public void testlogInfo_AuditLog() {
        logger.logInfo(
                Log4jLogger.AUDIT_LOG,
                LogMessageIdentifier.INFO_NO_EMAIL_ADDRESS_SPECIFIED_ORGANIZATION,
                "test");
    }

    @Test
    public void testlogInfo_AccessLog() {
        logger.logInfo(
                Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.INFO_NO_EMAIL_ADDRESS_SPECIFIED_ORGANIZATION,
                "test");
    }

    @Test
    public void testGetLogMessageTextLocalizedResource() {
        String msg = loggerLocaleTest
                .getLogMessageText(LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS);
        assertEquals("00001: MSG TEST", msg);
    }

    @Test
    public void testGetLogMessageTextWrongIdentifier() {
        String msg = logger.getLogMessageText(LogMessageIdentifier.DEBUG);
        assertEquals("?? key 'DEBUG' not found ??", msg);

    }

    @Test
    public void testGetLogMessageText_00001() {
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS, "testuser",
                "127.0.0.1", "tenantID");
        assertEquals(
                "00001: User 'testuser' for tenant tenantID logged in (access from 127.0.0.1)", msg);
    }

    @Test
    public void testGetLogMessageText_00010() {
        String testDateStr = createDateStringForTest();
        long timerInterval = 100;
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.INFO_TIMER_CREATED_WITH_INTERVAL,
                String.valueOf(TimerType.DISCOUNT_END_CHECK), testDateStr,
                Long.toString(timerInterval));
        assertEquals(
                "00010: Timer for type 'DISCOUNT_END_CHECK' will be executed at: 1/1/11 12:00 AM with an interval of 100 milliseconds",
                msg);
    }

    @Test
    public void testGetLogMessageText_00016() {
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.INFO_PAYMENT_INFO_REGISTRATION_SUCCESS,
                "testKey", "testCode", "testReturn", "testReturnCode",
                "testReason", "2011-09-19 09:11:22");
        assertEquals(
                "00016: Successful registration of payment information 'testKey' with the processing code: 'testCode', return code: 'testReturn(testReturnCode)', reason: 'testReason', processing time '2011-09-19 09:11:22'",
                msg);
    }

    @Test
    public void testGetLogMessageText_00017() {
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.INFO_USER_PWDRECOVERY_REQUEST, "admin");
        assertEquals("00017: User 'admin' requested to recover password", msg);
    }

    @Test
    public void testGetLogMessageText_00018() {
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.INFO_USER_PWDRECOVERY_COMPLETE, "admin");
        assertEquals("00018: User 'admin' has reset password", msg);
    }

    @Test
    public void testGetLogMeaageText_40083() {
        long userKey = 10000;
        long subscriptionKey = 20000;
        long organizationKey = 88888;
        long targetProductKey = 1;
        long subscriptionProductKey = 12345;
        String msg = logger
                .getLogMessageText(
                        LogMessageIdentifier.WARN_MIGRATE_PRODUCT_FAILED_NOT_COMPATIBLE,
                        Long.toString(userKey), Long.toString(subscriptionKey),
                        Long.toString(organizationKey),
                        Long.toString(targetProductKey),
                        Long.toString(subscriptionProductKey));
        assertEquals(
                "40083: User '10000' failed in the attempt to migrate the subscription '20000' for organization '88888' as the target service '1' is not compatible with the current service '12345'",
                msg);

    }

    @Test
    public void testGetLogMeaageText_40163() {
        String paymentInfoKey = "10000";
        String processingCode = "aaa";
        String processingReturn = "bbb";
        String processingReturnCode = "ccc";
        String processingReason = "reason1";
        String processingTime = "2011-09-19 09:11:22";
        String msg = logger
                .getLogMessageText(
                        LogMessageIdentifier.WARN_PAYMENT_INFORMATION_WITH_PSP_REGISTRATION_FAILED,
                        paymentInfoKey, processingCode, processingReturn,
                        processingReturnCode, processingReason, processingTime);
        assertEquals(
                "40163: Registration of payment information '10000' with the PSP failed. Processing code: 'aaa', return code: 'bbb(ccc)', reason: 'reason1', processing time '2011-09-19 09:11:22'.",
                msg);
    }

    @Test
    public void testGetLogMessageText_40183() {
        EmailType emailType = EmailType.USER_CREATED_WITH_MARKETPLACE;
        String marketplaceId = "aaa";
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.WARN_RETRIEVE_MARKETPLACE_URL_FAILED,
                String.valueOf(emailType), marketplaceId);
        assertEquals(
                "40183: The marketplace URL for email type USER_CREATED_WITH_MARKETPLACE and marketplace ID aaa could not be retrieved. ",
                msg);
    }

    @Test
    public void testGetLogMeaageText_40247() {
        String userKey = "10000";
        EmailType emailType = EmailType.SUBSCRIPTION_USER_ADDED;
        Object[] paramObjects = new Object[] { "aaa", "bbb" };
        String params = Arrays.toString(paramObjects);
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.WARN_MAIL_SENDING_TASK_FAILED, userKey,
                String.valueOf(emailType), params);
        assertEquals(
                "40247: Sending mail failed (Recipient user='10000', Mail type='SUBSCRIPTION_USER_ADDED', Parameters=[aaa, bbb]).",
                msg);
    }

    /**
     * Bug 9187: Ensures the message parameters contain no new lines, so the
     * message is not confusing nor considered as 'Log forging' by a static code
     * analysis tool. See: https://www.fortify.com/vulncat/en/vulncat/index.html
     */
    @Test
    public void getLogMessageText_NoNewlinesInParameters() {
        // given:
        String param1 = "1001'\nALARM: BAD HACKER just logger in\n\n'1001";
        String param2 = "supplier";

        // when
        String msg = logger.getLogMessageText(
                LogMessageIdentifier.WARN_READ_ORGANIZATION_FAILED_WRONG_TYPE,
                param1, param2);
        System.out.println(msg);

        // then: the \n is escaped to \\n
        assertEquals(
                "40022: It was attempted to read the data of organization '1001'\\nALARM: BAD HACKER just logger in\\n\\n'1001' which is not a supplier",
                msg);
    }

    private String createDateStringForTest() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2011);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy h:mm a");
        return sdf.format(cal.getTime());
    }
}
