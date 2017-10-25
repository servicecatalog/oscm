/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.oscm.apiversioning.adapter.AdapterFactory;
import org.oscm.apiversioning.adapter.IAdapter;
import org.oscm.apiversioning.enums.ApiVersion;
import org.oscm.apiversioning.service.parser.ServiceParser;
import org.oscm.apiversioning.upgrade.info.ApiVersionUpgradeInfo;
import org.oscm.apiversioning.upgrade.info.ModificationDetail;
import org.oscm.apiversioning.upgrade.info.VORecords;

/**
 * @author qiu
 * 
 */
public class VOConverter implements IConverter {

    @Override
    public void exec(ConverterContext context, ConverterChain chain) {
        ApiVersion version = context.getVersion();
        String serviceName = context.getServiceName();
        String methodName = context.getMethodName();
        List<ApiVersion> versions = ApiVersion.getVersions(version);
        for (ApiVersion v : versions) {
            VORecords voRecordMap = ApiVersionUpgradeInfo.VO_INFO.get(v);
            Map<String, Class<?>> parameters = ServiceParser
                    .getParametersForMethod(serviceName, methodName);
            for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
                List<ModificationDetail> details = voRecordMap
                        .getModificationDetailsForVO(entry.getValue());
                if (!details.isEmpty()) {
                    String parameterName = entry.getKey();
                    adaptSOAPMessageForVO(context, details, parameterName);
                }
            }
        }
    }

    /**
     * @param context
     * @param details
     * @param parameterName
     */
    private void adaptSOAPMessageForVO(ConverterContext context,
            List<ModificationDetail> details, String parameterName) {
        for (ModificationDetail detail : details) {
            IAdapter adapter = AdapterFactory.getAdapter(detail.getType());
            detail.getVariable().setVariableName(parameterName);
            try {
                adapter.exec(context.getSoapContext(), detail);
            } catch (SOAPException e) {
                e.printStackTrace();
            }
        }
    }

}
