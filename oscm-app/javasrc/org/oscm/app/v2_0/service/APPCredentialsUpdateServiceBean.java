package org.oscm.app.v2_0.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.oscm.app.remote.APPCredentialsUpdateService;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

@Stateless
public class APPCredentialsUpdateServiceBean implements APPCredentialsUpdateService {

	@EJB
	protected APPConfigurationServiceBean configService;

	@Override
	public void updateUserCredentials(long userId, String username, String password) {

		System.out.println("Update user credentials: " + userId + ", " + username + ", " + password);

		try {
			configService.getAllProxyConfigurationSettings().values().stream().forEach(
					(s) -> System.out.println(s.getControllerId() + " -> " + s.getKey() + " -> " + s.getValue()));
		} catch (ConfigurationException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}

	}

}
