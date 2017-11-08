/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Test;

import org.oscm.identityservice.bean.BulkUserImportReader.Row;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;

public class BulkUserImportReaderTest {

    BulkUserImportReader reader;

    @After
    public void after() {
        Streams.close(reader);
    }

    /**
     * Parse the user details:<br>
     * userId, email, locale, salutation, first name, last name
     */
    @Test
    public void read_userDetails() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csv);
        Row row = reader.iterator().next();

        // then
        assertEquals("user1", row.getUserDetails().getUserId());
        assertEquals("user1@org.com", row.getUserDetails().getEMail());
        assertEquals("en", row.getUserDetails().getLocale());
        assertEquals(Salutation.MR, row.getUserDetails().getSalutation());
        assertEquals("John", row.getUserDetails().getFirstName());
        assertEquals("Doe", row.getUserDetails().getLastName());
    }

    /**
     * Empty file should not cause error
     */
    @Test
    public void read_empty() {

        // given
        byte[] emptyCsv = bytes("");

        // when
        reader = new BulkUserImportReader(emptyCsv);

        // then
        assertFalse(reader.iterator().hasNext());
    }

    /**
     * Empty lines should be ignored
     */
    @Test
    public void read_emptyLine() {

        // given
        byte[] emptyCsv = bytes("\n\n");

        // when
        reader = new BulkUserImportReader(emptyCsv);

        // then
        assertFalse(reader.iterator().hasNext());
    }

    /**
     * All fields empty
     */
    @Test
    public void read_emptyFields() throws Exception {

        // given
        byte[] emptyCsv = bytes(",,,,,,");

        // when
        reader = new BulkUserImportReader(emptyCsv);
        Row row = reader.iterator().next();

        // then
        assertEquals("", row.getUserDetails().getUserId());
        assertThat(row.getRoles(), hasNoItems());
    }

    @Test(expected = NullPointerException.class)
    public void read_null() {
        reader = new BulkUserImportReader(null);
    }

    /**
     * Whitespace in the CSV file should be ignored
     */
    @Test
    public void read_ignoreWhitespace() throws Exception {

        // given
        byte[] csvWithSpaces = bytes("   user1  ,  user1@org.com  ,en  ,MR ,  John ,Doe,ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csvWithSpaces);
        Row row = reader.iterator().next();

        // then
        assertEquals("user1", row.getUserDetails().getUserId());
        assertEquals("user1@org.com", row.getUserDetails().getEMail());
        assertEquals("en", row.getUserDetails().getLocale());
        assertEquals(Salutation.MR, row.getUserDetails().getSalutation());
        assertEquals("John", row.getUserDetails().getFirstName());
        assertEquals("Doe", row.getUserDetails().getLastName());
    }

    /**
     * CSV line does not contain a user ID. User ID is required for mail report, later.
     */
    @Test(expected = BulkUserImportException.class)
    public void read_emptyUserId() throws Exception {

        // given missing user id (before comma)
        byte[] csv = bytes(",user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csv);

        // then exception is thrown
        reader.validate();
    }

    /**
     * CSV line does not contain a locale.
     */
    @Test(expected = BulkUserImportException.class)
    public void read_emptyLocale() throws Exception {

        // given missing locale
        byte[] csv = bytes("user1,user1@org.com, ,MR,John,Doe,ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csv);

        // then exception is thrown
        reader.validate();
    }

    /**
     * Values can be surrounded by quotes
     */
    @Test
    public void read_useQuotes() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("text without quotes, \" surounding spaces \" , \"text, with, comma \" ,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);
        Row row = reader.iterator().next();

        // then
        assertEquals("text without quotes", row.getUserDetails().getUserId());
        assertEquals(" surounding spaces ", row.getUserDetails().getEMail());
        assertEquals("text, with, comma ", row.getUserDetails().getLocale());
    }

    /**
     * The CVS file of this test contains a quote that is not properly closed.
     * 
     * This test checks only for runtime exception. The runtime exception can be
     * prevented when calling validate() beforehand.
     */
    @Test(expected = RuntimeException.class)
    public void read_nonClosedQuote() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN \n user2,\"text with opening quote, but not closing quote , , ,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);
        Iterator<Row> i = reader.iterator();
        i.next().getUserDetails();
        i.next().getUserDetails();
    }

    /**
     * The CVS file of this test contains a quote that is not properly closed.
     * 
     * The validate method will throw a business exception
     */
    @Test(expected = BulkUserImportException.class)
    public void validate_nonClosedQuote() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN \n user2,\"text with opening quote, but not closing quote , , ,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);

        // then exception is thrown
        reader.validate();
    }

    /**
     * The text ["luke" is my name] is not allowed with quoting like this. The
     * beginning quote is interpreted as field quote, not as a sub part quoted
     * text.
     * 
     * This test checks only for runtime exception. The runtime exception can be
     * prevented when calling validate() beforehand.
     */
    @Test(expected = RuntimeException.class)
    public void read_closingQuoteAtWrongPosition() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("\"luke\" is my name,field2,,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);
        Row row = reader.iterator().next();

        // then
        assertEquals("\"luke\" is my name", row.getUserDetails().getUserId());
        assertEquals("field2", row.getUserDetails().getEMail());
    }

    /**
     * The text ["luke" is my name] is not allowed with quoting like this. The
     * beginning quote is interpreted as field quote, not as a sub part quoted
     * text.
     * 
     * The validate method will throw a business exception
     */
    @Test(expected = BulkUserImportException.class)
    public void validate_closingQuoteAtWrongPosition() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("\"luke\" is my name,field2,,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);

        // then exception is thrown
        reader.validate();
    }

    /**
     * The text [my name is "luke"] IS allowed to be quoted like this, because
     * the "luke" is considered only as a sub-part text.
     */
    @Test
    public void read_startingQuoteAtWrongPosition() throws Exception {

        // given
        byte[] csvWithQuotes = bytes("my name is \"luke\",field2,,,,,");

        // when
        reader = new BulkUserImportReader(csvWithQuotes);
        Row row = reader.iterator().next();

        // then
        assertEquals("my name is \"luke\"", row.getUserDetails().getUserId());
        assertEquals("field2", row.getUserDetails().getEMail());
    }

    /**
     * No error should happen if values are empty
     */
    @Test
    public void read_missingText() throws Exception {

        // given
        byte[] csvWithoutText = bytes(",,,,,,");

        // when
        reader = new BulkUserImportReader(csvWithoutText);
        Row row = reader.iterator().next();

        // then
        assertEquals("", row.getUserDetails().getUserId());
        assertEquals("", row.getUserDetails().getEMail());
        assertEquals("", row.getUserDetails().getLocale());
        assertEquals(null, row.getUserDetails().getSalutation());
        assertEquals("", row.getUserDetails().getFirstName());
        assertEquals("", row.getUserDetails().getLastName());
    }

    /**
     * The field is quoted from beginning to end. Additional a sub-part is
     * quoted inside.<br>
     * 
     * Intended text: ["user ID "luke" is used"] (Please note the beginning and
     * ending quote)<br>
     * <br>
     * In this case the quotes in the value must be double quoted
     */
    @Test
    public void read_escapeQuotesWithOuterQuotes() throws Exception {

        // given
        byte[] fieldThatContainsEscapings = bytes("\"user ID \"\"luke\"\" is used\",field2,,,,,");

        // when
        reader = new BulkUserImportReader(fieldThatContainsEscapings);
        Row row = reader.iterator().next();

        // then
        assertEquals("user ID \"luke\" is used", row.getUserDetails()
                .getUserId());
        assertEquals("field2", row.getUserDetails().getEMail());
    }

    /**
     * The start and the end of the field is not quoted. Only a sub-part<br>
     * 
     * Intended text: [user ID "luke" is used]<br>
     * <br>
     * In this case the quotes in the value must be double quoted
     */
    @Test
    public void read_quotesInsideValue() throws Exception {

        // given
        byte[] fieldThatContainsEscapings = bytes("user ID \"luke\" is used,field2,,,,,");

        // when
        reader = new BulkUserImportReader(fieldThatContainsEscapings);
        Row row = reader.iterator().next();

        // then
        assertEquals("user ID \"luke\" is used", row.getUserDetails()
                .getUserId());
        assertEquals("field2", row.getUserDetails().getEMail());
    }

    /**
     * Salutation can only be MR or MS. No other value allowed
     */
    @Test(expected = RuntimeException.class)
    public void read_invalidSalutation() throws Exception {

        // given
        byte[] invalidSalutation = bytes(",,,Herr,,,");

        // when
        reader = new BulkUserImportReader(invalidSalutation);
        Row row = reader.iterator().next();

        // then exception is thrown
        row.getUserDetails();
    }

    @Test(expected = BulkUserImportException.class)
    public void validate_invalidSalutation() throws Exception {

        // given
        byte[] invalidSalutation = bytes(",,,Herr,,,");

        // when
        reader = new BulkUserImportReader(invalidSalutation);

        // then exception is thrown
        reader.validate();
    }

    /**
     * Read two lines
     */
    @Test
    public void read_multipleLines() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN \n user2,user2@org.com,en,MR,John2,Doe2,ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csv);
        Iterator<Row> i = reader.iterator();

        // then
        assertEquals("user1", i.next().getUserDetails().getUserId());
        assertEquals("user2", i.next().getUserDetails().getUserId());
        assertFalse(i.hasNext());
    }

    /**
     * Read two lines that have several empty lines in between
     */
    @Test
    public void read_multipleLinesWithEmptyLines() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN \n\n\n\n user2,user2@org.com,en,MR,John2,Doe2,ORGANIZATION_ADMIN \n\n");

        // when
        reader = new BulkUserImportReader(csv);
        Iterator<Row> i = reader.iterator();

        // then
        assertEquals("user1", i.next().getUserDetails().getUserId());
        assertEquals("user2", i.next().getUserDetails().getUserId());
        assertFalse(i.hasNext());
    }

    /**
     * This CVS file contains a line with too little number of fields (or
     * columns).
     * 
     * This test checks only for runtime exception. The runtime exception can be
     * prevented when calling validate() beforehand.
     */
    @Test(expected = RuntimeException.class)
    public void read_wrongNumberOfFieldSeparators() throws Exception {

        // given
        byte[] csvWithTooLittleColumns = bytes(",,");

        // when
        reader = new BulkUserImportReader(csvWithTooLittleColumns);
        Iterator<Row> i = reader.iterator();

        // then exception is thrown
        i.next().getRoles();
    }

    /**
     * This CVS file contains a line with too little number of fields (or
     * columns).
     * 
     * The validate method will throw a business exception
     */
    @Test(expected = BulkUserImportException.class)
    public void validate_wrongNumberOfFieldSeparators() throws Exception {

        // given
        byte[] csvWithTooLittleColumns = bytes(",,");

        // when
        reader = new BulkUserImportReader(csvWithTooLittleColumns);

        // then exception is thrown
        reader.validate();
    }

    /**
     * Read a single role
     */
    @Test
    public void read_roles() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe, ORGANIZATION_ADMIN");

        // when
        reader = new BulkUserImportReader(csv);
        Row row = reader.iterator().next();

        // then
        assertTrue(row.getRoles().contains(UserRoleType.ORGANIZATION_ADMIN));
    }

    /**
     * Read CVS with multiple role names contained in one field (the roles are
     * separated with commas)
     */
    @Test
    public void read_multipleRoles() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,\"ORGANIZATION_ADMIN,TECHNOLOGY_MANAGER\"");

        // when
        reader = new BulkUserImportReader(csv);
        Row row = reader.iterator().next();

        // then
        assertTrue(row.getRoles().contains(UserRoleType.ORGANIZATION_ADMIN));
    }

    /**
     * Read CVS with multiple role names and some whitespace
     */
    @Test
    public void read_multipleRolesWithWhiteSpace() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,\"  ORGANIZATION_ADMIN  ,  TECHNOLOGY_MANAGER  \"");

        // when
        reader = new BulkUserImportReader(csv);
        Row row = reader.iterator().next();

        // then
        assertTrue(row.getRoles().contains(UserRoleType.ORGANIZATION_ADMIN));
        assertTrue(row.getRoles().contains(UserRoleType.TECHNOLOGY_MANAGER));
    }

    /**
     * The CVS file of this test case contains a role name that does not exists.
     * 
     * This test checks only for runtime exception. The runtime exception can be
     * prevented when calling validate() beforehand.
     */
    @Test(expected = RuntimeException.class)
    public void read_invalidRole() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,\"  WRONG_ROLE  \"");

        // when
        reader = new BulkUserImportReader(csv);
        Row row = reader.iterator().next();

        // then exception is thrown
        row.getRoles();
    }

    /**
     * The CVS file of this test case contains a role name that does not exists.
     * 
     * The validate method will throw a business exception
     */
    @Test(expected = BulkUserImportException.class)
    public void validate_invalidRole() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,\"  WRONG_ROLE  \"");

        // when
        BulkUserImportReader.validate(csv);
    }

    /**
     * The CVS file of this case has proper syntax. No problem should be
     * detected when validating.
     */
    @Test
    public void validate_properCVS() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

        // when validating then no error
        assertTrue(BulkUserImportReader.validate(csv));
    }

    @Test
    public void validate_wrongEncoding() throws Exception {

        // given
        byte[] csv = "user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN"
                .getBytes("Cp1252");

        // when
        reader = new BulkUserImportReader(csv);

        // then
        assertEquals("user1", reader.iterator().next().getUserDetails()
                .getUserId());
        assertTrue(reader.validate());
    }

    private byte[] bytes(String value) {
        return Strings.toBytes(value);
    }

}
