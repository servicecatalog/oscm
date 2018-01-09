/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: Jun 8, 2011                                                      
 *                                                                              
 *  Completion Time: Jun 9, 2011                       
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

/**
 * @author Dirk Bernsau
 * 
 */
public abstract class IterableTask extends WebtestTask {

    private int start = -1;
    private int count = -1;
    private String invalidNumberText;

    public void setStart(String value) {
        try {
            start = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            invalidNumberText = e.getMessage();
        }
    }

    public void setCount(String value) {
        try {
            count = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            invalidNumberText = e.getMessage();
        }
    }

    protected String multiply(String value) {
        if (invalidNumberText != null) {
            throw new WebtestTaskException(invalidNumberText);
        }
        StringBuffer buff = new StringBuffer();
        if (value != null && start >= 0 && count > 0) {
            for (int i = start; i < start + count; i++) {
                if (i > start) {
                    buff.append(",");
                }
                buff.append(value + i);
            }
        } else {
            return value;
        }
        return buff.toString();
    }
}
