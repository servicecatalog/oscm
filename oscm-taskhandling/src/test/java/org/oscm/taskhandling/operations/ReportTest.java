/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.taskhandling.operations;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.types.enumtypes.EmailType;

/**
 * 
 * Test cases for report class
 * 
 * @author cheld
 * 
 */
public class ReportTest {

    Report report = new Report(1);

    @Test
    public void addErrorMessage() {
        report.addErrorMessage("user1", "something wrong");
        assertEquals("user1: something wrong", report.errorMessages().get(0));
    }

    @Test
    public void buildMailType_noError() {
        assertEquals(EmailType.BULK_USER_IMPORT_SUCCESS, report.buildMailType());
    }

    @Test
    public void buildMailType_withError() {
        report.addErrorMessage("user1", "something wrong");
        assertEquals(EmailType.BULK_USER_IMPORT_SOME_ERRORS,
                report.buildMailType());
    }

    /**
     * Mail text:
     * 
     * {0} out of {1} were successfully imported.
     */
    @Test
    public void buildMailParams_noError() {
        Object[] param = report.buildMailParams();
        assertEquals("1", param[0]); // imported users
        assertEquals("1", param[1]); // all users
        assertEquals(2, param.length);
    }

    /**
     * Mail text:
     * 
     * {0} out of {1} were successfully imported.\n\nThe following users were
     * not imported: \n{2}
     */
    @Test
    public void buildMailParams_withError() {
        report.addErrorMessage("user1", "something wrong");
        Object[] param = report.buildMailParams();
        assertEquals("0", param[0]); // imported users
        assertEquals("1", param[1]); // all users
        assertEquals("\tuser1: something wrong\n", param[2]);
        assertEquals(3, param.length);

    }
}
