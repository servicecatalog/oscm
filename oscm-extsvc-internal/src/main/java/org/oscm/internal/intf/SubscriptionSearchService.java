/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.04.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

import javax.ejb.Local;
import java.util.Collection;

/**
 * Interface for full text subscription search.
 */
@Local
public interface SubscriptionSearchService {
    Collection<Long> searchSubscriptions(String searchPhrase)
            throws InvalidPhraseException, ObjectNotFoundException;
}
