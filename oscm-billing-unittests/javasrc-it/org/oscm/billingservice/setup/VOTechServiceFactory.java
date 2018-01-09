/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 22.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author baumann
 * 
 */
public class VOTechServiceFactory {

    public static final String TECH_SERVICE_EXAMPLE_ID = "example";
    public static final String TECH_SERVICE_EXAMPLE_ASYNC_ID = "exampleAsync";
    public static final String TECH_SERVICE_EXAMPLE2_ID = "example2";
    public static final String TECH_SERVICE_EXAMPLE2_ASYNC_ID = "example2Async";

    /**
     * Imports the technical services from the given XML
     * 
     * @return the result message created by the XML parsing process
     */
    public static String importTechnicalServices(
            ServiceProvisioningService provisioningService, String tsXml)
            throws Exception {
        return provisioningService.importTechnicalServices(tsXml
                .getBytes("UTF-8"));
    }

    public static VOParameterDefinition getParamDefinition(String parameterId,
            VOTechnicalService technicalService) {
        for (VOParameterDefinition def : technicalService
                .getParameterDefinitions()) {
            if (def.getParameterId().equals(parameterId)) {
                return def;
            }
        }
        return null;
    }

    public static VORoleDefinition getRoleDefinition(
            VOTechnicalService technicalService, String roleId) {
        for (VORoleDefinition roleDef : technicalService.getRoleDefinitions()) {
            if (roleDef.getRoleId().equals(roleId)) {
                return roleDef;
            }
        }
        return null;
    }

    public static List<VORoleDefinition> getRoleDefinitions(
            VOTechnicalService technicalService, String... roleIds) {
        List<VORoleDefinition> roleDefinitions = new ArrayList<VORoleDefinition>();
        for (String roleId : roleIds) {
            roleDefinitions.add(getRoleDefinition(technicalService, roleId));
        }
        return roleDefinitions;
    }

    public static VOEventDefinition getEventDefinition(
            VOTechnicalService technicalService, String eventId) {
        List<VOEventDefinition> events = technicalService.getEventDefinitions();

        for (VOEventDefinition event : events) {
            if (event.getEventId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    public static List<VOEventDefinition> getEventDefinitions(
            VOTechnicalService technicalService, String... eventIds) {
        List<VOEventDefinition> eventDefinitions = new ArrayList<VOEventDefinition>();
        for (String eventId : eventIds) {
            eventDefinitions.add(getEventDefinition(technicalService, eventId));
        }
        return eventDefinitions;
    }

}
