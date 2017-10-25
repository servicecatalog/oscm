/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;

/**
 * Value object representing a PSP account.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VOPSPAccount extends BaseVO {

    private static final long serialVersionUID = 1902812993655218172L;

    private String pspIdentifier;
    private VOPSP psp;

    public String getPspIdentifier() {
        return pspIdentifier;
    }

    public void setPspIdentifier(String pspIdentifier) {
        this.pspIdentifier = pspIdentifier;
    }

    public VOPSP getPsp() {
        return psp;
    }

    public void setPsp(VOPSP psp) {
        this.psp = psp;
    }

}
