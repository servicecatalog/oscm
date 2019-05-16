/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.08.2012                                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.app.v2_0.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.apache.commons.codec.binary.Base64;
import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.exceptions.ObjectNotFoundException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dirk Bernsau */
@Stateless(name = "org.oscm.app.v2_0.intf.APPlatformService")
@Remote(APPlatformService.class)
public class APPlatformServiceBean implements APPlatformService {

	private static final Logger LOGGER = LoggerFactory.getLogger(APPlatformServiceBean.class);

	private static final String PROXY_CONTROLLER_ID = "PROXY";
	private static final String PWD_KEY = "BSS_USER_PWD";

	public void setConfigService(APPConfigurationServiceBean configService) {
		this.configService = configService;
	}

	private Map<String, APPlatformController> controllers = new HashMap<String, APPlatformController>();

	@EJB
	protected APPConfigurationServiceBean configService;

	public void setConcurrencyService(APPConcurrencyServiceBean concurrencyService) {
		this.concurrencyService = concurrencyService;
	}

	@EJB
	protected APPConcurrencyServiceBean concurrencyService;

	public void setMailService(APPCommunicationServiceBean mailService) {
		this.mailService = mailService;
	}

	@EJB
	protected APPCommunicationServiceBean mailService;

	public void setAuthService(APPAuthenticationServiceBean authService) {
		this.authService = authService;
	}

	@EJB
	protected APPAuthenticationServiceBean authService;

	public void setInstanceDAO(ServiceInstanceDAO instanceDAO) {
		this.instanceDAO = instanceDAO;
	}

	@EJB
	protected ServiceInstanceDAO instanceDAO;

	@Override
	public boolean lockServiceInstance(String controllerId, String instanceId, PasswordAuthentication authentication)
			throws APPlatformException {
		authService.authenticateTMForInstance(controllerId, instanceId, authentication);
		return concurrencyService.lockServiceInstance(controllerId, instanceId);
	}

	@Override
	public void unlockServiceInstance(String controllerId, String instanceId, PasswordAuthentication authentication)
			throws APPlatformException {
		authService.authenticateTMForInstance(controllerId, instanceId, authentication);
		concurrencyService.unlockServiceInstance(controllerId, instanceId);
	}

	@Override
	public boolean exists(String controllerId, String instanceId) {
		return instanceDAO.exists(controllerId, instanceId);
	}

	@Override
	public void sendMail(List<String> mailAddresses, String subject, String text) throws APPlatformException {
		try {
			mailService.sendMail(mailAddresses, subject, text);
		} catch (Exception e) {
			LOGGER.warn("Controller cannot send mail for instance processing.", e);
			SuspendException se = new SuspendException("Controller cannot send mail for instance processing.", e);
			throw se;
		}
	}

	@Override
	public String getEventServiceUrl() throws ConfigurationException {
		String result = configService.getProxyConfigurationSetting(PlatformConfigurationKey.APP_BASE_URL);
		if (!result.endsWith("/")) {
			result += "/";
		}
		result += "notify";
		return result;
	}

	@Override
	public String getBSSWebServiceUrl() throws ConfigurationException {
		if ("SAML_SP".equals(configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_AUTH_MODE))) {
			return configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_STS_WEBSERVICE_URL);
		}
		return configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_WEBSERVICE_URL);
	}

	@Override
	public HashMap<String, Setting> getControllerSettings(String controllerId, PasswordAuthentication authentication)
			throws APPlatformException {
		authService.authenticateTMForController(controllerId, authentication);
		return configService.getControllerConfigurationSettings(controllerId);
	}

	@Override
	public void storeControllerSettings(String controllerId, HashMap<String, Setting> controllerSettings,
			PasswordAuthentication authentication) throws APPlatformException {
		authService.authenticateTMForController(controllerId, authentication);
		configService.storeControllerConfigurationSettings(controllerId, controllerSettings);

		// controllers expect to be notified once the settings are modified
		requestControllerSettings(controllerId);
	}

	@Override
	public User authenticate(String controllerId, PasswordAuthentication authentication) throws APPlatformException {
		VOUserDetails vo = authService.getAuthenticatedTMForController(controllerId, authentication);
		User user = new User();
		user.setUserKey(vo.getKey());
		user.setUserId(vo.getUserId());
		user.setEmail(vo.getEMail());
		user.setLocale(vo.getLocale());
		user.setLastName(vo.getLastName());
		user.setFirstName(vo.getFirstName());
		return user;
	}

	private APPlatformController lookupController(String id) throws ControllerLookupException {
		APPlatformController c = controllers.get(id);
		if (c == null) {
			c = APPlatformControllerFactory.getInstance(id);
			controllers.put(id, c);
		}
		return c;
	}

	@Override
	public void requestControllerSettings(String controllerId)
			throws ConfigurationException, ControllerLookupException {
		HashMap<String, Setting> settings = configService.getControllerConfigurationSettings(controllerId);
		APPlatformController controller = lookupController(controllerId);
		ControllerSettings controllerSettings = new ControllerSettings(settings);
		controller.setControllerSettings(controllerSettings);
	}

	@Override
	public Collection<String> listServiceInstances(String controllerId, PasswordAuthentication authentication)
			throws APPlatformException {
		Collection<String> result = new ArrayList<>();
		List<ServiceInstance> instances = instanceDAO.getInstancesForController(controllerId);
		for (ServiceInstance instance : instances) {
			result.add(instance.getInstanceId());
		}
		return result;
	}

	@Override
	public ProvisioningSettings getServiceInstanceDetails(String controllerId, String instanceId,
			PasswordAuthentication authentication) throws APPlatformException {
		try {
			ServiceInstance instance = instanceDAO.getInstanceById(instanceId);
			return configService.getProvisioningSettings(instance, null);
		} catch (ServiceInstanceNotFoundException e) {
			throw new ObjectNotFoundException(e.getMessage());
		} catch (BadResultException e) {
			throw new APPlatformException(e.getMessage());
		}
	}

	@Override
	public ProvisioningSettings getServiceInstanceDetails(String controllerId, String instanceId, String subscriptionId,
			String organizationId) throws APPlatformException {
		try {
			ServiceInstance instance = instanceDAO.getInstanceById(instanceId);
			if (instance.getControllerId().equals(controllerId) && instance.getSubscriptionId().equals(subscriptionId)
					&& instance.getOrganizationId().equals(organizationId)) {
				return configService.getProvisioningSettings(instance, null, true);
			} else {
				throw new ServiceInstanceNotFoundException("Service instance with ID '%s' not found.", instanceId);
			}
		} catch (ServiceInstanceNotFoundException e) {
			throw new ObjectNotFoundException(e.getMessage());
		} catch (BadResultException e) {
			throw new APPlatformException(e.getMessage());
		}
	}

	@Override
	public String getBSSWebServiceWSDLUrl() throws ConfigurationException {
		if ("SAML_SP".equals(configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_AUTH_MODE))) {
			return configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL);
		}
		return configService.getProxyConfigurationSetting(PlatformConfigurationKey.BSS_WEBSERVICE_WSDL_URL);
	}

	@Override
	public void storeServiceInstanceDetails(String controllerId, String instanceId, ProvisioningSettings settings,
			PasswordAuthentication authentication) throws APPlatformException {
		try {
			ServiceInstance instance = instanceDAO.getInstanceById(instanceId);
			instance.setInstanceParameters(settings.getParameters());
		} catch (ServiceInstanceNotFoundException e) {
			throw new ObjectNotFoundException(e.getMessage());
		} catch (BadResultException e) {
			throw new APPlatformException(e.getMessage());
		}
	}

	@Override
	public boolean checkToken(String token, String signature) {

		InputStream is = null;
		try {

			// load configuration settings for truststore
			String loc = configService.getProxyConfigurationSetting(PlatformConfigurationKey.APP_TRUSTSTORE);
			String pwd = configService.getProxyConfigurationSetting(PlatformConfigurationKey.APP_TRUSTSTORE_PASSWORD);
			String alias = configService
					.getProxyConfigurationSetting(PlatformConfigurationKey.APP_TRUSTSTORE_BSS_ALIAS);

			if (loc == null || pwd == null || alias == null) {
				LOGGER.error("Missing configuration settings for token check");
				return false;
			}

			// create hash from given token
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(token.getBytes(StandardCharsets.UTF_8));
			String tokenHash = new String(md.digest());

			// load truststore
			is = new FileInputStream(loc);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, pwd.toCharArray());

			// get certificate for alias
			Certificate cert = keystore.getCertificate(alias);

			if (cert == null) {
				LOGGER.error("Unable to find certificate with alias " + alias);
				return false;
			}

			// get public key and decrypt signature
			Key key = cert.getPublicKey();

			if (key == null) {
				LOGGER.error("Certificate returned null key");
				return false;
			}

			Cipher c = Cipher.getInstance(key.getAlgorithm());
			c.init(Cipher.DECRYPT_MODE, key);

			byte[] decodedSignature = Base64.decodeBase64(signature);
			String decryptedHash = new String(c.doFinal(decodedSignature));

			// compare token hash with decrypted hash
			if (tokenHash.equals(decryptedHash)) {
				return true;
			}
		} catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
				| NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| ConfigurationException e) {
			LOGGER.error(e.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

		return false;
	}

	@Override
	public void updateUserCredentials(long useKey, String username, String password) {

		List<String> controllers = configService.getUserConfiguredControllers(username);

		if (controllers.contains(PROXY_CONTROLLER_ID)) {
			HashMap<String, String> settings = new HashMap<>();
			settings.put(PWD_KEY, password);
			try {
				configService.storeAppConfigurationSettings(settings);
				logSuccessInfo(username, PROXY_CONTROLLER_ID);
			} catch (ConfigurationException | GeneralSecurityException e) {
				logErrorMsg(PROXY_CONTROLLER_ID, PWD_KEY, e);
			}
		}

		controllers.stream().filter(id -> !PROXY_CONTROLLER_ID.equals(id)).forEach(id -> {
			HashMap<String, Setting> settings = new HashMap<>();
			settings.put(PWD_KEY, new Setting(PWD_KEY, password));
			try {
				configService.storeControllerConfigurationSettings(id, settings);
				logSuccessInfo(username, id);
			} catch (ConfigurationException e) {
				logErrorMsg(id, PWD_KEY, e);
			}
		});
	}

	private void logSuccessInfo(String username, String id) {
		LOGGER.info("User [" + username + "] password for controller [" + id + "] updated");
	}

	private void logErrorMsg(String controllerId, String settingKey, Exception excp) {
		LOGGER.error("Unable to store controller [" + controllerId + "] setting [" + settingKey + "]", excp);
	}
}
