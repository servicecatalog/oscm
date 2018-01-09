/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 26.04.17 09:00
 *
 ******************************************************************************/

package org.oscm.operatorservice.queries;

import org.oscm.dataservice.local.DataService;
import org.oscm.internal.vo.VOSubscriptionUsageEntry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Authored by dawidch
 */
public class SubscriptionUsageQuery {

        private static final String query = "SELECT \n" +
                "                organization.name AS customerOrgName, \n" +
                "                organization.organizationid AS customerOrgId, \n" +
                "                product.productid AS productId, \n" +
                "                technicalproduct.technicalproductid AS technicalProductId, \n" +
                "                supplier.name AS supplierOrgName, \n" +
                "                supplier.organizationid AS supplierOrgId, \n" +
                "                subscription.subscriptionid,\n" +
                "                (select count(*) from bssuser.usagelicense where subscription_tkey = subscription.tkey) as numberOfUsers, parameter.value as numberOfVms\n" +
                "                FROM \n" +
                "                bssuser.subscription, \n" +
                "                bssuser.product, \n" +
                "                bssuser.organization, \n" +
                "                bssuser.technicalproduct,\n" +
                "                bssuser.parameterdefinition,\n" +
                "                bssuser.parameter,\n" +
                "                bssuser.parameterset,\n" +
                "                bssuser.organization supplier\n" +
                "                WHERE\n" +
                "                (subscription.status = 'ACTIVE' OR subscription.status = 'PENDING_UPD') AND \n" +
                "                subscription.product_tkey = product.tkey AND\n" +
                "                parameterdefinition.parameterid='VMS_NUMBER' AND\n" +
                "                parameter.parameterdefinitionkey=parameterdefinition.tkey AND\n" +
                "                parameter.parametersetkey=parameterset.tkey AND\n" +
                "                parameterset.tkey=product.parameterset_tkey AND\n" +
                "                parameterdefinition.technicalproduct_tkey=product.technicalproduct_tkey AND\n" +
                "                subscription.organizationkey = organization.tkey AND\n" +
                "                product.technicalproduct_tkey = technicalproduct.tkey AND\n" +
                "                technicalproduct.organizationkey = supplier.tkey AND\n" +
                "                parameter.value ~ '^\\d{1,}$' AND \n" +
                "                CAST(parameter.value AS INTEGER) > 0\n" +
                "                ORDER BY organization.organizationid;\n";


    public static Collection<VOSubscriptionUsageEntry> execute(DataService dm) {
        List resultList = dm.createNativeQuery(query).getResultList();
        Collection<VOSubscriptionUsageEntry> result = new ArrayList<>();
        for (Object o : resultList) {
            Object[] columns = (Object[]) o;
            result.add(new VOSubscriptionUsageEntry((String) columns[1], (String) columns[0], (String) columns[6],
                    ((String) columns[2]).split("#")[0],(String) columns[3], (String) columns[4],
                    (String) columns[5], columns[7].toString(), columns[8].toString()));
        }
        return result;
    }
}
