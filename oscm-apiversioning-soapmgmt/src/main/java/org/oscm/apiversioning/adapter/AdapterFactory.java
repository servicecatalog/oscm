/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import org.oscm.apiversioning.enums.ModificationType;

/**
 * @author qiu
 * 
 */
public class AdapterFactory {

    public static IAdapter getAdapter(ModificationType type) {
        if (ModificationType.ADD.equals(type)) {
            return new AddAdapter();
        } else if (ModificationType.REMOVE.equals(type)) {
            return new RemoveAdapter();
        } else if (ModificationType.ADDEXCEPTION.equals(type)) {
            return new ExceptionAdapter();
        } else if (ModificationType.UPDATE.equals(type)) {
            return new UpdateAdapter();
        } else if (ModificationType.UPDATEFIELD.equals(type)) {
            return new UpdateFieldAdapter();
        } else {
            throw new RuntimeException("No adapter is found");
        }
    }
}
