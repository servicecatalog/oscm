/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components.response;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents an error that occurred during service execution
 * 
 * @author cheld
 */
public class ReturnCode implements Serializable {

    private static final long serialVersionUID = 8319959115391354955L;

    ReturnType type;

    String messageKey;

    String[] messageParam;

    String member;

    public ReturnCode() {
    }

    public ReturnCode(ReturnType type, String messageKey,
            String... messageParam) {
        this.type = type;
        this.messageKey = messageKey;
        this.messageParam = messageParam;
    }

    public ReturnType getType() {
        return type;
    }

    public void setSeverity(ReturnType type) {
        this.type = type;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String[] getMessageParam() {
        return messageParam;
    }

    public void setMessageParam(String[] messageParam) {
        this.messageParam = messageParam;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return messageKey + ", " + Arrays.toString(messageParam);
    }

}
