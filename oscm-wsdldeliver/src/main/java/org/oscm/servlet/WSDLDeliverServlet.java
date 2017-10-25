/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.TenantSetting;
import org.oscm.enums.APIVersion;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.tenant.local.TenantServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * 
 * @author Gao
 * 
 */
public class WSDLDeliverServlet extends HttpServlet {

    private static final long serialVersionUID = -3504533241988904286L;

    private static Log4jLogger logger = LoggerFactory
            .getLogger(WSDLDeliverServlet.class);

    public static final String VERSION = "VERSION";
    public static final String PORT_TYPE = "PORT_TYPE";
    public static final String SERVICE_NAME = "SERVICE_NAME";
    public static final String FILE_TYPE = "FILE_TYPE";
    public static final String WSDL_ROOT_PATH = "/wsdl/";
    public static final String TENANT_ID = "TENANT_ID";
    
    private static final String SSO_STS_URL = "SSO_STS_URL";
    private static final String SSO_STS_ENCKEY_LEN = "SSO_STS_ENCKEY_LEN";
    private static final String SSO_STS_METADATA_URL = "SSO_STS_METADATA_URL";
    
    @EJB
    private TenantServiceLocal tenantService;
    
    @EJB
    private ConfigurationServiceLocal configurationService;
    
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request,
            HttpServletResponse response){
        String filePath = getTargetFilePathFromRequest(request);
        if (filePath.length() == 0) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_GET_FILE_CONTENT_FAILED);
        }
        InputStream fileStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filePath);
        try {
            String fileContent = convertStreamToString(fileStream);
            String portType = getValueFromRequest(request, PORT_TYPE);
            
            if("STS".equals(portType)){
                String tenantId = getValueFromRequest(request, TENANT_ID);
                
                boolean isDefault = isDefaultTenant(tenantId);
                String settingUrl = getTenantSetting(SSO_STS_URL, tenantId, isDefault);
                String settingMetadataUrl = getTenantSetting(SSO_STS_METADATA_URL, tenantId, isDefault);
                String settingEnckeyLen = getTenantSetting(SSO_STS_ENCKEY_LEN, tenantId, isDefault);
                
                fileContent = fileContent.replaceAll("@"+SSO_STS_URL +"@", settingUrl);
                fileContent = fileContent.replaceAll("@"+SSO_STS_ENCKEY_LEN +"@", settingEnckeyLen);
                fileContent = fileContent.replaceAll("@"+SSO_STS_METADATA_URL +"@", settingMetadataUrl);
            }
 
            response.getWriter().print(fileContent);
            
        } catch (IOException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_GET_FILE_CONTENT_FAILED);
        } catch (ObjectNotFoundException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_TENANT_NOT_FOUND);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    // ignore, wanted to close anyway
                }
            }
        }
    }

    private String getTargetFilePathFromRequest(HttpServletRequest request) {
        APIVersion version = getVersionFromRequest(request);
        if (version == null) {
            return "";
        }
        String portType = getValueFromRequest(request, PORT_TYPE);
        String fileType = getValueFromRequest(request, FILE_TYPE);
        String serviceName = getValueFromRequest(request, SERVICE_NAME);
        String fileName = serviceName;
        if (fileType.toLowerCase().equals("xsd")) {
            fileName = serviceName + "_schema1";
        }
        String filePath = WSDL_ROOT_PATH + version.getSourceLocation() + "/"
                + portType + "/" + fileName + "." + fileType;
        return filePath;
    }

    private String getValueFromRequest(HttpServletRequest request,
            String attributeName) {
        Object object = request.getAttribute(attributeName);
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    private APIVersion getVersionFromRequest(HttpServletRequest request) {
        String version = getValueFromRequest(request, VERSION);
        if (version.length() == 0) {
            return APIVersion.getCurrentVersion();
        } else {
            return APIVersion.getForURLString(version);
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }
    
    private String getTenantSetting(String settingKey, String tenantId,
            boolean isDefault) throws ObjectNotFoundException {

        if (StringUtils.isEmpty(tenantId) || isDefault) {
            ConfigurationKey configurationKey = ConfigurationKey
                    .valueOf(settingKey);
            ConfigurationSetting configurationSetting = configurationService
                    .getConfigurationSetting(configurationKey,
                            Configuration.GLOBAL_CONTEXT);
            return configurationSetting.getValue();
        } else {
            TenantSetting tenantSetting = tenantService
                    .getTenantSetting(settingKey, tenantId);
            return tenantSetting.getValue();
        }
    }
    
    private boolean isDefaultTenant(String tenantId) {

        ConfigurationSetting setting = configurationService
                .getConfigurationSetting(ConfigurationKey.SSO_DEFAULT_TENANT_ID,
                        Configuration.GLOBAL_CONTEXT);
        String defaultTenantId = setting.getValue();

        return tenantId.equals(defaultTenantId);
    }
}
