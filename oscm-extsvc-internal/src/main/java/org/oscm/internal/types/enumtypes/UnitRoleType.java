/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 28.07.15 15:32
 *
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * This class has to be in sync with db table 'unituserrole'.
 * Order of enum values has to be the same as order of rows (key ascending) in db.
 * This way {@link #getKey()} method will return actual mapped key of db role value.
 */
public enum UnitRoleType {
    
    /**
     * Represents Administrator role in Unit
     */
    ADMINISTRATOR,
    
    /**
     * Represents User role in Unit
     */
    USER;
    
    /**
     * 
     * @return - key of object in db
     */
    public int getKey() {
        return this.ordinal() + 1;
    }
}
