/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import javax.ejb.Local;

import org.oscm.internal.types.exception.ObjectNotFoundException;

@Local
public interface SearchServiceLocal {

    /**
     * (Re)Creates the initial index from scratch for the objects already being
     * in the database. The indexing operation is performed only if either the
     * index is empty (which will be the case when the server is started for the
     * very first time), and skipped otherwise, or if <code>force</code> is set
     * to <code>true</code> (in this case, a possibly existing index is
     * overridden).
     */
    public void initIndexForFulltextSearch(boolean force);

    /**
     * Returns a list of services according to the specified criteria. If the
     * calling user is logged in, the list only includes services which are
     * visible to him and his organization. Otherwise, the list only includes
     * services visible to anonymous (non-registered) users.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace to get the services for
     * @param categoryId
     *            the ID of the category to get the services for
     * @return the list of services matching the specified criteria
     * @throws ObjectNotFoundException
     *             if the specified marketplace or category was not found
     */
    public ProductSearchResult getServicesByCategory(String marketplaceId,
            String categoryId) throws ObjectNotFoundException;
}
