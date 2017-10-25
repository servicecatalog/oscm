/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Aug 30, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model;

import java.io.Serializable;

/**
 * Contains common attributes for all Report Data Objects.
 * 
 * @author barzu
 */
public abstract class RDO implements Serializable {

    private static final long serialVersionUID = 121975116818514943L;

    /** id of a rdo, used by birt to group tables */
    private int entryNr;

    /** refers to a parent rdo object, used by birt to group tables */
    private int parentEntryNr;

    public RDO() {
        super();
    }

    public RDO(int parentEntryNr, int entryNr) {
        super();
        this.parentEntryNr = parentEntryNr;
        this.entryNr = entryNr;
    }

    public int getEntryNr() {
        return entryNr;
    }

    public void setEntryNr(int entryNr) {
        this.entryNr = entryNr;
    }

    public int getParentEntryNr() {
        return parentEntryNr;
    }

    public void setParentEntryNr(int parentEntryNr) {
        this.parentEntryNr = parentEntryNr;
    }

}
