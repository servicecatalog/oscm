/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 23.07.15 14:32
 *
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.pagination.Pagination;

public class ToExtPaginationStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<org.oscm.paginator.Pagination, Pagination> {

    @Override
    public Pagination convert(org.oscm.paginator.Pagination pagination) {
        if (pagination == null) {
            return null;
        }

        Pagination result = new Pagination();

        result.setLimit(pagination.getLimit());
        result.setOffset(pagination.getOffset());

        return result;
    }
}
