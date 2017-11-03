/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PriceModelType;

public class PriceModels {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";

    public static PriceModelHistory createPriceModelHistory(DataService ds,
            long objKey, boolean isChargeable) throws Exception {
        return createPriceModelHistory(ds, objKey, new SimpleDateFormat(
                DATE_PATTERN).format(new Date()), 0, ModificationType.ADD,
                isChargeable);
    }

    public static PriceModelHistory createPriceModelHistory(DataService ds,
            final long objKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            boolean isChargeable) throws Exception {

        PriceModelType priceModelType = isChargeable ? PriceModelType.PRO_RATA
                : PriceModelType.FREE_OF_CHARGE;
        return createPriceModelHistory(ds, objKey, modificationDate, version,
                modificationType, priceModelType, 0L);
    }

    public static PriceModelHistory createPriceModelHistory(DataService ds,
            final long objKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            PriceModelType priceModelType, long productObjKey) throws Exception {

        return createPriceModelHistory(ds, objKey, modificationDate, version,
                modificationType, priceModelType, productObjKey, true);
    }

    public static PriceModelHistory createPriceModelHistory(DataService ds,
            final long objKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            PriceModelType priceModelType, long productObjKey,
            boolean provisioningCompleted) throws Exception {

        PriceModelHistory pmh = new PriceModelHistory();
        pmh.setObjKey(objKey);
        pmh.getDataContainer().setType(priceModelType);
        pmh.setInvocationDate(new Date());
        pmh.setObjVersion(version);
        pmh.setModdate(new SimpleDateFormat(DATE_PATTERN)
                .parse(modificationDate));
        pmh.setModtype(modificationType);
        pmh.setModuser("moduser");
        pmh.setProductObjKey(productObjKey);
        pmh.setProvisioningCompleted(provisioningCompleted);
        ds.persist(pmh);
        return pmh;
    }

}
