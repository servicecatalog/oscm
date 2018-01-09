/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 22.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author baumann
 */
public class VOServiceFactory {

    public enum TestService {
        /**
         * Based on technical service "example" from
         * BaseAdmUmTest.TECHNICAL_SERVICES_XML
         */
        EXAMPLE,

        /**
         * Asynchronous Technical service based on technical service
         * "exampleAsync" from BaseAdmUmTest.TECHNICAL_SERVICES_XML
         */
        EXAMPLE_ASYNC,

        /**
         * Based on technical service "example2" from
         * BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML
         */
        EXAMPLE2,
        /**
         * Based on technical service "example2Async" from
         * BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_ASYNC_XML
         */
        EXAMPLE2_ASYNC

    }

    public static VOService createVOService(String serviceId,
            TestService testService, VOTechnicalService technicalService) {
        switch (testService) {
        case EXAMPLE:
            return createExampleService(serviceId, technicalService);
        case EXAMPLE_ASYNC:
            return createAsyncExampleService(serviceId, technicalService);
        case EXAMPLE2:
            return createExample2Service(serviceId, technicalService);
        case EXAMPLE2_ASYNC:
            return createAsyncExample2Service(serviceId, technicalService);
        default:
            return null;
        }
    }

    // TODO emil what is necessary for async service creation?
    private static VOService createAsyncExampleService(String serviceId,
            VOTechnicalService technicalService) {
        VOService voService = new VOServiceDetails();
        voService.setServiceId(serviceId);

        List<VOParameter> paramList = Arrays.asList(new VOParameter[] {
                newVOParameter("MAX_FOLDER_NUMBER", "15", true,
                        technicalService),
                newVOParameter("HAS_OPTIONS", "1", true, technicalService),
                newVOParameter("BOOLEAN_PARAMETER", Boolean.TRUE.toString(),
                        true, technicalService),
                newVOParameter("STRING_PARAMETER", "xyz", false,
                        technicalService),
                newVOParameter("PERIOD", DateTimeHandling.daysToMillis(120)
                        + "", true, technicalService),
                newVOParameter("LONG_NUMBER", "813", true, technicalService) });

        voService.setParameters(paramList);

        return voService;
    }

    private static VOService createExampleService(String serviceId,
            VOTechnicalService technicalService) {
        VOService voService = new VOServiceDetails();
        voService.setServiceId(serviceId);

        List<VOParameter> paramList = Arrays.asList(new VOParameter[] {
                newVOParameter("MAX_FOLDER_NUMBER", "15", true,
                        technicalService),
                newVOParameter("HAS_OPTIONS", "1", true, technicalService),
                newVOParameter("BOOLEAN_PARAMETER", Boolean.TRUE.toString(),
                        true, technicalService),
                newVOParameter("STRING_PARAMETER", "xyz", false,
                        technicalService),
                newVOParameter("PERIOD", DateTimeHandling.daysToMillis(120)
                        + "", true, technicalService),
                newVOParameter("LONG_NUMBER", "813", true, technicalService) });

        voService.setParameters(paramList);

        return voService;
    }

    private static VOService createExample2Service(String serviceId,
            VOTechnicalService technicalService) {
        VOService voService = new VOServiceDetails();
        voService.setServiceId(serviceId);

        List<VOParameter> paramList = Arrays
                .asList(new VOParameter[] {
                        newVOParameter("MAX_FOLDER_NUMBER", "35", true,
                                technicalService),
                        newVOParameter("HAS_OPTIONS", "2", true,
                                technicalService),
                        newVOParameter("BOOLEAN_PARAMETER",
                                Boolean.FALSE.toString(), true,
                                technicalService),
                        newVOParameter("STRING_PARAMETER", "abc", false,
                                technicalService),
                        newVOParameter("PERIOD",
                                DateTimeHandling.daysToMillis(150) + "", true,
                                technicalService),
                        newVOParameter("LONG_NUMBER", "4711", true,
                                technicalService) });

        voService.setParameters(paramList);

        return voService;
    }

    private static VOService createAsyncExample2Service(String serviceId,
            VOTechnicalService technicalService) {
        VOService voService = new VOServiceDetails();
        voService.setServiceId(serviceId);

        List<VOParameter> paramList = Arrays
                .asList(new VOParameter[] {
                        newVOParameter("MAX_FOLDER_NUMBER", "35", true,
                                technicalService),
                        newVOParameter("HAS_OPTIONS", "2", true,
                                technicalService),
                        newVOParameter("BOOLEAN_PARAMETER",
                                Boolean.FALSE.toString(), true,
                                technicalService),
                        newVOParameter("STRING_PARAMETER", "abc", false,
                                technicalService),
                        newVOParameter("PERIOD",
                                DateTimeHandling.daysToMillis(150) + "", true,
                                technicalService),
                        newVOParameter("LONG_NUMBER", "4711", true,
                                technicalService) });

        voService.setParameters(paramList);

        return voService;
    }

    private static VOParameter newVOParameter(String parameterId,
            String parameterValue, boolean configurable,
            VOTechnicalService technicalService) {
        VOParameter param = new VOParameter(
                VOTechServiceFactory.getParamDefinition(parameterId,
                        technicalService));
        param.setValue(parameterValue);
        param.setConfigurable(configurable);
        return param;
    }

    /**
     * Gets the definition of the specified role for the given service
     * 
     * @param voServiceDetails
     *            a service details object
     * @param index
     *            the index of the role
     * @return the role definition
     */
    public static VORoleDefinition getRole(VOServiceDetails voServiceDetails,
            String roleId) {
        return VOTechServiceFactory.getRoleDefinition(
                voServiceDetails.getTechnicalService(), roleId);
    }

    /**
     * Gets the specified parameter of the given service
     * 
     * @param service
     * @param parameterId
     * @return the service parameter if it exists; otherwise <code>null</code>
     */
    public static VOParameter getParameter(VOService service, String parameterId) {
        for (VOParameter param : service.getParameters()) {
            if (parameterId.equals(param.getParameterDefinition()
                    .getParameterId())) {
                return param;
            }
        }
        return null;
    }

    /**
     * Get all parameters of a service
     * 
     * @param service
     * @return a map with the service parameters. The key is the parameter ID.
     */
    public static Map<String, VOParameter> getParameters(VOService service) {
        Map<String, VOParameter> parameterMap = new HashMap<String, VOParameter>();
        for (VOParameter param : service.getParameters()) {
            parameterMap.put(param.getParameterDefinition().getParameterId(),
                    param);
        }
        return parameterMap;
    }

}
