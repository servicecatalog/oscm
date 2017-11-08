/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 28.10.2011
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import org.oscm.types.enumtypes.EmailType;

/*
 * Hold recipient (instance), the email type and a mail parameter list. 
 * This class is only needed in testcases where a mail is sended and should be verified.
 */
public class MailDetails<T> {
    private T instance;
    private EmailType emailType;
    private Object[] params;

    public MailDetails(T instance, EmailType emailType, Object[] params) {
        setInstance(instance);
        setEmailType(emailType);
        setParams(params);
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        this.emailType = emailType;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
