/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 7, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;
import java.util.Date;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.RevenueShareModelHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;

/**
 * @author tokoda
 * 
 */
public class RevenueShareModels {

    public static RevenueShareModelHistory createRevenueShareModelHistory(
            DataService ds, final long objKey, final Date modDate,
            final int version, final ModificationType modificationType,
            final BigDecimal revenueShare,
            final RevenueShareModelType revenueShareModelType) throws Exception {

        RevenueShareModelHistory rsmh = new RevenueShareModelHistory();
        rsmh.setObjKey(objKey);
        rsmh.setInvocationDate(new Date());
        rsmh.setObjVersion(version);
        rsmh.setModdate(modDate);
        rsmh.setModtype(modificationType);
        rsmh.setModuser("moduser");
        rsmh.getDataContainer().setRevenueShare(revenueShare);
        rsmh.getDataContainer().setRevenueShareModelType(revenueShareModelType);
        ds.persist(rsmh);
        return rsmh;
    }
}
