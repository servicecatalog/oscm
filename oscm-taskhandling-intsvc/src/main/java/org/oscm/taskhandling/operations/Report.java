/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.EmailType;

class Report {

    int totalUsers;

    int failures;

    List<String> errorMessages = new ArrayList<String>();

    Report(int totalUsers) {
        this.totalUsers = totalUsers;
        this.failures = 0;
    }

    int allUsersToBeImported() {
        return totalUsers;
    }

    int failedUsers() {
        return failures;
    }

    int importedUsers() {
        return totalUsers - failures;
    }

    List<String> errorMessages() {
        return errorMessages;
    }

    public void addErrorMessage(String userId, String errorMessage) {
        failures++;
        errorMessages.add(userId + ": " + errorMessage);
    }

    EmailType buildMailType() {
        if (failures == 0) {
            return EmailType.BULK_USER_IMPORT_SUCCESS;
        }
        return EmailType.BULK_USER_IMPORT_SOME_ERRORS;
    }

    Object[] buildMailParams() {
        List<String> params = new ArrayList<String>();
        params.add(Integer.valueOf(importedUsers()).toString());
        params.add(Integer.valueOf(allUsersToBeImported()).toString());
        if (errorMessages.size() > 0) {
            params.add(buildErrorMessageParam());
        }
        return params.toArray();
    }

    String buildErrorMessageParam() {
        StringBuffer param = new StringBuffer();
        for (String error : errorMessages) {
            param.append("\t" + error + "\n");
        }
        return param.toString();
    }

}
