/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-18                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies filters for domain object data which can be used to improve
 * performance.
 */
public enum PerformanceHint {

    /**
     * Only fields are loaded that are needed to identify a domain object.
     * Typically, these are the name, ID, and numeric key. These fields are
     * usually sufficient to perform an action on a selected object, for
     * example, to delete a service selected at the user interface.
     */
    ONLY_IDENTIFYING_FIELDS,

    /**
     * Only fields are loaded that are needed to present a domain object in a
     * table-like listing. Typically, these are the name, short description, and
     * rating. This type of list is used, for example, at the user interface
     * when a user chooses to browse all services.
     */
    ONLY_FIELDS_FOR_LISTINGS,

    /**
     * All fields are loaded. This is the default. It is used, for example, when
     * a user displays the details of a service at the user interface.
     */
    ALL_FIELDS

}
