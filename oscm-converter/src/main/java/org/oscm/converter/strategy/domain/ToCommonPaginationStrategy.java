/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 23.07.15 14:32
 *
 *******************************************************************************/

package org.oscm.converter.strategy.domain;

import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.converter.strategy.api.AbstractConversionStrategy;
import org.oscm.pagination.Pagination;

public class ToCommonPaginationStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<Pagination, org.oscm.paginator.Pagination> {

    @Override
    public org.oscm.paginator.Pagination convert(Pagination pagination) {

        if (pagination == null) {
            return null;
        }

        org.oscm.paginator.Pagination result = new org.oscm.paginator.Pagination();

        result.setLimit(pagination.getLimit());
        result.setOffset(pagination.getOffset());

        return result;
    }
}
