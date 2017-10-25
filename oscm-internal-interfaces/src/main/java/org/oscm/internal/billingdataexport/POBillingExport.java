/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import java.io.Serializable;
import java.util.Date;

public class POBillingExport implements Serializable {

    private static final long serialVersionUID = -2746685834655744045L;

    Date from;
    Date to;
    byte[] xmlOutput;

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public byte[] getXmlOutput() {
        return xmlOutput;
    }

    public void setXmlOutput(byte[] xmlOutput) {
        this.xmlOutput = xmlOutput;
    }

}
