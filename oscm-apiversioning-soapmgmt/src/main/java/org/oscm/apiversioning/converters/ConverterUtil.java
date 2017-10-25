/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import java.util.List;

import javax.xml.soap.SOAPException;

import org.oscm.apiversioning.adapter.AdapterFactory;
import org.oscm.apiversioning.adapter.IAdapter;
import org.oscm.apiversioning.enums.ApiVersion;
import org.oscm.apiversioning.enums.ConverterType;
import org.oscm.apiversioning.upgrade.info.ApiVersionUpgradeInfo;
import org.oscm.apiversioning.upgrade.info.ModificationDetail;
import org.oscm.apiversioning.upgrade.info.ServiceInfo;
import org.oscm.apiversioning.upgrade.info.ServiceRecords;

/**
 * @author qiu
 * 
 */
public class ConverterUtil {
    public static void convert(ConverterContext context) throws SOAPException {
        ApiVersion version = context.getVersion();
        if (version == null) {
            return;
        }
        String serviceName = context.getServiceName();
        String methodName = context.getMethodName();
        List<ApiVersion> versions = ApiVersion.getVersions(version);
        ConverterType type = context.getConverterType();
        for (ApiVersion v : versions) {
            ServiceRecords recordMap = new ServiceRecords();
            if (ConverterType.REQUEST.equals(type)) {
                recordMap = ApiVersionUpgradeInfo.REQUEST_INFO.get(v);
            } else if (ConverterType.RESPONSE.equals(type)) {
                recordMap = ApiVersionUpgradeInfo.RESPONSE_INFO.get(v);
            } else if (ConverterType.EXCEPTION.equals(type)) {
                recordMap = ApiVersionUpgradeInfo.EXCEPTION_INFO.get(v);
            }

            if (null != recordMap && null != recordMap.getRecordsMap()) {
                List<ModificationDetail> details = recordMap.getRecordsMap()
                        .get(new ServiceInfo(serviceName, methodName));
                if (null != details) {
                    for (ModificationDetail detail : details) {
                        IAdapter adapter = AdapterFactory.getAdapter(detail
                                .getType());
                        adapter.exec(context.getSoapContext(), detail);
                    }
                }

            }
        }
    }
}
