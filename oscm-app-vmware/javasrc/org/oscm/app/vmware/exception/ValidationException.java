/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.exception;

/**
 * @author kulle
 *
 */
public class ValidationException extends Exception
        implements VmwareControllerException {

    private static final long serialVersionUID = -9004826998456595538L;

    public ValidationException(String msg) {
        super(msg);
    }

}
