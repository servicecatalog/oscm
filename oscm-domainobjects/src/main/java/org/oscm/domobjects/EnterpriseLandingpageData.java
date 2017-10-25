/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Christoph Held                   
 *                                                                              
 *  Creation Date: 29.1.2014                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * JPA managed entity representing the EnterpriseLandingpage data.
 */
@Embeddable
public class EnterpriseLandingpageData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = -4270286455227354862L;

}
