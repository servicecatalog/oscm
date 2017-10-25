/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.identityservice.bean;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.oscm.identityservice.bean.BulkUserImportReader.Row;
import org.oscm.stream.Streams;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.BulkUserImportException.Reason;
import org.oscm.internal.vo.VOUserDetails;

/**
 * An organization administrator can import multiple users to its own
 * organization via .csv file import (UTF-8 encoding). The field separator is
 * the comma. One line contains the properties of a single user in the following
 * order:<br>
 * <br>
 * 
 * User ID (mandatory), Email (mandatory), Language, Locale (mandatory), Title
 * ("MR" or "MS"), First name, Last name, One or several user roles<br>
 * <br>
 * 
 * As first and last name may contain a comma and multiple roles are also
 * separated by a comma (field delimiter), the data fields have to be put in
 * double quotes. Optional and not used fields have to be empty. Double quotes
 * inside a field (first and last name) have to be escaped by double quotes.<br>
 * <br>
 * 
 * Sample for users to be imported to a technology provider organization
 * (white-spaces only for better readability):<br>
 * <br>
 * 
 * "user1, user1@org.com, en, MR, "John", "Doe", "ORGANIZATION_ADMIN,
 * TECHNOLOGY_MANAGER"<br>
 * "user2, user2@org.com, en, , , , "TECHNOLOGY_MANAGER"<br>
 * "user3, user3@org.com, en, MR, , "Miller", "TECHNOLOGY_MANAGER"
 * 
 * @author cheld
 * 
 */
class BulkUserImportReader implements Iterable<Row>, Closeable {

    /**
     * Format of the CVS file for the bulk user import
     */
    final static CSVFormat BULK_USER_IMPORT_CSV_FORMAT = CSVFormat.EXCEL
            .withIgnoreSurroundingSpaces(true)
            .withIgnoreEmptyLines(true)
            .withHeader("UserId", "EMail", "Locale", "Salutation", "FirstName",
                    "LastName", "Roles");

    CSVParser csvParser;

    public BulkUserImportReader(byte[] cvsData) {
        try {
            Reader reader = new StringReader(Strings.toString(cvsData));
            csvParser = BULK_USER_IMPORT_CSV_FORMAT.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e); // cannot happen
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return new Iterator<BulkUserImportReader.Row>() {
            Iterator<CSVRecord> i = csvParser.iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public Row next() {
                CSVRecord record = i.next();
                return new Row(record);
            }

            @Override
            public void remove() {
                i.remove();
            }
        };
    }

    static class Row {

        CSVRecord record;

        Row(CSVRecord record) {
            this.record = record;
        }

        VOUserDetails getUserDetails() {
            VOUserDetails userDetails = new VOUserDetails();
            userDetails.setUserId(record.get("UserId"));
            userDetails.setEMail(record.get("EMail"));
            userDetails.setLocale(record.get("Locale"));
            String salutationAsString = record.get("Salutation");
            userDetails.setSalutation(mapToSalutation(salutationAsString));
            userDetails.setFirstName(record.get("FirstName"));
            userDetails.setLastName(record.get("LastName"));
            return userDetails;
        }

        public List<UserRoleType> getRoles() {
            return mapToUserRoles(record.get("Roles"));
        }

    }

    static Salutation mapToSalutation(String salutationAsString) {
        if (!Strings.isEmpty(salutationAsString)) {
            return Salutation.valueOf(salutationAsString);
        }
        return null;
    }

    static List<UserRoleType> mapToUserRoles(String roleNamesAsString) {
        List<UserRoleType> result = new ArrayList<>();
        if (!roleNamesAsString.isEmpty()) {
            final String[] roleNames = roleNamesAsString.split(",");
            for (String roleName : roleNames) {
                UserRoleType userRole = UserRoleType.valueOf(roleName.trim());
                result.add(userRole);
            }
        }
        return result;
    }

    /**
     * Returns true if the given CVS data can be parsed. Throws an exception
     * otherwise.
     * 
     * @return boolean
     * @throws BulkUserImportException
     *             if the validation fails
     */
    public boolean validate() throws BulkUserImportException {
        int count = 1;
        try {
            for (Iterator<CSVRecord> i = csvParser.iterator(); i.hasNext(); count++) {
                CSVRecord record = i.next();
                if (!record.isConsistent()) {
                    throw new BulkUserImportException(
                            Reason.WRONG_NUMBER_OF_FIELDS, null, count);
                }
                if (record.get("UserId").isEmpty()) {
                    throw new BulkUserImportException(Reason.MISSING_USERID,
                            null, count);
                }
                if (record.get("Locale").isEmpty()) {
                    throw new BulkUserImportException(Reason.MISSING_LOCALE,
                            null, count);
                }
                try {
                    mapToSalutation(record.get("Salutation"));
                } catch (Exception e) {
                    throw new BulkUserImportException(Reason.WRONG_SALUTATION,
                            e, count);
                }
                try {
                    mapToUserRoles(record.get("Roles"));
                } catch (Exception e) {
                    throw new BulkUserImportException(Reason.WRONG_ROLE, e,
                            count);
                }

            }
        } catch (RuntimeException e) {
            throw new BulkUserImportException(Reason.PARSING_FAILED, e, count);
        }
        return true;
    }

    public static boolean validate(byte[] csv) throws BulkUserImportException {
        BulkUserImportReader reader = new BulkUserImportReader(csv);
        try {
            return reader.validate();
        } finally {
            Streams.close(reader);
        }
    }

    @Override
    public void close() throws IOException {
        Streams.close(csvParser);
    }

}
