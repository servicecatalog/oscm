/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.util;

import static org.oscm.auditlog.matchers.AuditLogMatchers.isCorrectTimeStampFormat;
import static org.oscm.auditlog.matchers.AuditLogMatchers.isSerializedCorrectly;
import static org.oscm.auditlog.util.AuditLogFactory.createAuditLogs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import org.oscm.auditlog.model.AuditLog;

public class AuditLogSerializerTest {
    @Test
    public void serialize() {
        // given
        AuditLogSerializer auditLogSerializer = new AuditLogSerializer();
        List<AuditLog> auditLogs = createAuditLogs();

        // when
        byte[] result = auditLogSerializer.serialize(auditLogs);

        // then
        assertThat(result, isSerializedCorrectly(auditLogs));
    }

    @Test
    public void getCreationTime_distinguishablePMTime_B10558() {
        // given
        AuditLogSerializer auditLogSerializer = new AuditLogSerializer();

        Calendar anyTime = givenTimeIsOneAM();
        Calendar twelveHoursLater = givenTimeIsOnePM();

        // when
        String amTime = auditLogSerializer.getCreationTimeAsStr(anyTime
                .getTimeInMillis());

        String pmTime = auditLogSerializer
                .getCreationTimeAsStr(twelveHoursLater.getTimeInMillis());

        // then
        assertFalse("Timestamps may not be AM and PM distinct.",
                amTime.equals(pmTime));
    }

    @Test
    public void getCreationTimeAsStr() {
        // given
        AuditLogSerializer auditLogSerializer = new AuditLogSerializer();
        long anyTime = 1372042100577L;

        // when
        String result = auditLogSerializer.getCreationTimeAsStr(anyTime);

        // then
        assertEquals(TimeZone.getDefault(), AuditLogSerializer.DATE_FORMATTER
                .get().getTimeZone());
        assertThat(result, isCorrectTimeStampFormat());
    }

    private Calendar givenTimeIsOneAM() {
        Calendar anyTime = Calendar.getInstance();
        anyTime.setTimeInMillis(1372042100577L);
        anyTime.set(Calendar.HOUR_OF_DAY, 1);
        return anyTime;
    }

    private Calendar givenTimeIsOnePM() {
        Calendar twelveHoursLater = (Calendar) givenTimeIsOneAM().clone();
        twelveHoursLater.set(Calendar.HOUR_OF_DAY, 13);
        return twelveHoursLater;
    }
}
