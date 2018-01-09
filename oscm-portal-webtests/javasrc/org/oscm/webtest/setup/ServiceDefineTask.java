/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: May 20, 2011                                                      
 *                                                                              
 *  Completion Time: June 6, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;

import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Custom ANT task creating marketable services using the WS-API.
 * 
 * @author Dirk Bernsau
 */
public class ServiceDefineTask extends WebtestTask {

    private static final String CONCURRENT_USER = "CONCURRENT_USER";
    private static final String NAMED_USER = "NAMED_USER";

    private String techServiceId = "Example 1.00";
    private String serviceId;
    private String name;
    private String shortDescription;
    private String description;
    private boolean autoAssignUserEnabled = false;
    private String marketplaceId;
    private String image;
    private String concurrentUser;
    private String namedUser;
    private boolean configurableChecked;
    private boolean publicService = true;
    private boolean withParameters = true;
    private String svcKeyProperty;
    private String configuratorUrl = null;

    public void setConfiguratorUrl(String configuratorUrl) {
        this.configuratorUrl = configuratorUrl;
    }

    public void setTechServiceId(String value) {
        techServiceId = value;
    }

    public void setServiceId(String value) {
        serviceId = value;
    }

    public void setImage(String value) {
        image = value;
    }

    public void setName(String value) {
        name = value;
    }

    public void setMarketplaceId(String value) {
        marketplaceId = value;
        if (isEmpty(marketplaceId)) {
            marketplaceId = null;
        }
    }

    @Override
    public void setDescription(String value) {
        description = value;
    }

    public void setShortDescription(String value) {
        shortDescription = value;
    }

    public void setConcurrentUser(String value) {
        concurrentUser = value;
    }

    public void setNamedUser(String value) {
        namedUser = value;
    }

    public void setConfigurableChecked(String value) {
        configurableChecked = Boolean.parseBoolean(value);
    }

    public void setPublicService(String value) {
        publicService = Boolean.parseBoolean(value);
    }

    public void setUseParameters(String value) {
        withParameters = Boolean.parseBoolean(value);
    }

    public void setAutoAssignUserEnabled(boolean autoAssignUserEnabled) {
        this.autoAssignUserEnabled = autoAssignUserEnabled;
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {

        log("Creating service with ID " + serviceId, 2);
        ServiceProvisioningService spsSvc = getServiceInterface(ServiceProvisioningService.class);
        MarketplaceService mpSvc = getServiceInterface(MarketplaceService.class);

        VOTechnicalService techSvc = null;
        List<VOTechnicalService> technicalServices = spsSvc
                .getTechnicalServices(OrganizationRoleType.SUPPLIER);
        for (VOTechnicalService ts : technicalServices) {
            if (techServiceId.equals(ts.getTechnicalServiceId())) {
                techSvc = ts;
                break;
            }
        }
        if (techSvc == null) {
            throw new WebtestTaskException("No technical service with ID "
                    + techServiceId + " available!");
        }

        VOService svc = new VOService();
        svc.setServiceId(serviceId);
        svc.setName(name);
        svc.setShortDescription(shortDescription);
        svc.setDescription(description);
        svc.setAutoAssignUserEnabled(Boolean.valueOf(autoAssignUserEnabled));
        svc.setConfiguratorUrl(configuratorUrl);

        if (withParameters) {
            List<VOParameter> parameters = new ArrayList<VOParameter>();
            int cnt = 0;
            for (VOParameterDefinition parDef : techSvc
                    .getParameterDefinitions()) {
                VOParameter parameter = null;
                parameter = new VOParameter(parDef);
                parameter.setValue(parDef.getDefaultValue());
                // the first three are not configurable in this setup
                if (configurableChecked && parDef.isConfigurable() && cnt > 2) {
                    parameter.setConfigurable(true);
                }
                if (NAMED_USER.equals(parDef.getParameterId())
                        && namedUser != null) {
                    parameter.setValue(namedUser);
                }
                if (CONCURRENT_USER.equals(parDef.getParameterId())
                        && concurrentUser != null) {
                    parameter.setValue(concurrentUser);
                }
                parameters.add(parameter);
                cnt++;
            }
            if (parameters.size() > 0) {
                svc.setParameters(parameters);
            }
        }

        VOServiceDetails newService = spsSvc.createService(techSvc, svc,
                getImage());
        String message = "Created service with ID " + newService.getServiceId();
        if (marketplaceId != null) {
            publish(mpSvc, newService);
            message += " and published to marketplace " + marketplaceId;
        }
        if (isEmpty(svcKeyProperty)) {
            getProject().setProperty("createdSvcKey",
                    String.valueOf(newService.getKey()));
        } else {
            getProject().setProperty(svcKeyProperty,
                    String.valueOf(newService.getKey()));
        }

        log(message);
    }

    private void publish(MarketplaceService mpSvc, VOService service)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException {

        VOMarketplace voMp = new VOMarketplace();
        voMp.setMarketplaceId(marketplaceId);

        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setAnonymousVisible(publicService);
        voCE.setMarketplace(voMp);
        mpSvc.publishService(service, Arrays.asList(voCE));

    }

    private VOImageResource getImage() {
        VOImageResource result = null;
        if (image != null) {
            File baseDir = getProject().getBaseDir();
            File file = new File(baseDir, image);
            if (!file.exists()) {
                throw new WebtestTaskException("Image does not exist: "
                        + file.getAbsolutePath());
            }
            FileInputStream inputStream = null;
            ByteArrayOutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(file);
                outputStream = new ByteArrayOutputStream(1024);
                byte[] bytes = new byte[512];

                int readBytes;
                while ((readBytes = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, readBytes);
                }
                result = new VOImageResource();
                result.setBuffer(outputStream.toByteArray());
                result.setImageType(ImageType.SERVICE_IMAGE);
            } catch (FileNotFoundException e) {
                throw new WebtestTaskException("Image does not exist: "
                        + file.getAbsolutePath());
            } catch (IOException e) {
                throw new WebtestTaskException("Failed to load image: "
                        + file.getAbsolutePath());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return result;
    }

    public void setSvcKeyProperty(String svcKeyProperty) {
        this.svcKeyProperty = svcKeyProperty;
    }

}
