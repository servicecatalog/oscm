/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.bes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.balancer.XMLHelper;
import org.oscm.app.vmware.persistence.APPDataAccessService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.intf.SubscriptionService;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOTechnicalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;

public class ServiceParamRetrieval {

	private static final Logger logger = LoggerFactory.getLogger(ServiceParamRetrieval.class);

	private VOServiceDetails service;
	private VMPropertyHandler ph;

	public ServiceParamRetrieval(VMPropertyHandler ph) {
		this.ph = ph;
		String customerOrgId = ph.getSettings().getOrganizationId();
		logger.debug("customerOrgId: " + customerOrgId);

		try {
			BesClient bes = new BesClient();
			Credentials credentials = ph.getTPUser();
			SubscriptionService subSvc = bes.getWebService(SubscriptionService.class, credentials);

			VOSubscriptionDetails subscr = subSvc.getSubscriptionForCustomer(customerOrgId,
					ph.getSettings().getSubscriptionId());

			APPDataAccessService das = new APPDataAccessService();
			credentials = das.getCredentials(subscr.getSubscribedService().getSellerId());
			ServiceProvisioningService provSvc = bes.getWebService(ServiceProvisioningService.class, credentials);
			VOService svc = subscr.getSubscribedService();
			service = provSvc.getServiceDetails(svc);
		} catch (Exception e) {
			logger.error("Failed to initialize ServiceParameter. customerOrg: " + customerOrgId, e);
		}
	}

	public String getServiceSetting(String parameterId) throws Exception {
		logger.debug("parameter: " + parameterId);
		String value = ph.getServiceSetting(parameterId);
		if (value != null) {
			String logValue = value;
			if (parameterId.endsWith("_PWD")) {
				logValue = "*****";
			}
			logger.debug("found parameter in parameter list. " + parameterId + ": " + logValue);
			return value;
		}

		List<VOParameter> serviceParams = service.getParameters();
		for (VOParameter p : serviceParams) {
			if (p.getParameterDefinition().getParameterId().equals(parameterId)) {
				String logValue = p.getValue();
				if (parameterId.endsWith("_PWD")) {
					logValue = "*****";
				}
				logger.debug("found parameter in marketable service. " + parameterId + ": " + logValue);
				return p.getValue();
			}
		}

		List<VOParameterDefinition> techServiceParams = service.getTechnicalService().getParameterDefinitions();
		for (VOParameterDefinition p : techServiceParams) {
			if (p.getParameterId().equals(parameterId)) {
				String logValue = p.getDefaultValue();
				if (parameterId.endsWith("_PWD")) {
					logValue = "*****";
				}
				logger.debug("found parameter in technical service. " + parameterId + ": " + logValue);
				return p.getDefaultValue();
			}
		}

		BesClient bes = new BesClient();
		ServiceProvisioningService sps = bes.getWebService(ServiceProvisioningService.class, ph.getTPUser());
		List<VOTechnicalService> technicalServices = new ArrayList<VOTechnicalService>();
		technicalServices.add(service.getTechnicalService());
		byte[] tsvc = sps.exportTechnicalServices(technicalServices);
		InputStream in = new ByteArrayInputStream(tsvc);
		String technicalService = IOUtils.toString(in, "UTF-8");

		return readServiceParameter(parameterId, technicalService);
	}

	protected static String readServiceParameter(String parameterId, String technicalService) throws Exception {
	    
	        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
	        dfactory.setValidating(false);
	        dfactory.setIgnoringElementContentWhitespace(true);
	        dfactory.setNamespaceAware(true);
	        DocumentBuilder builder = dfactory.newDocumentBuilder();
	        Document document = builder.parse(new InputSource(new StringReader(technicalService)));
		NodeList serviceParameters = document.getElementsByTagName("ParameterDefinition");

		for (int i = 0; i < serviceParameters.getLength(); i++) {
			Node serviceParameter = serviceParameters.item(i);
			boolean isConfigurable = Boolean
					.valueOf(XMLHelper.getAttributeValue(serviceParameter, "configurable", "false")).booleanValue();
			if (serviceParameter != null && !isConfigurable) {
				String id = XMLHelper.getAttributeValue(serviceParameter, "id", null);
				if (id != null && id.equalsIgnoreCase(parameterId)) {
					return XMLHelper.getAttributeValue(serviceParameter, "default", "");
				}
			}
		}
		throw new Exception("Failed to retrieve service parameter " + parameterId);
	}
}