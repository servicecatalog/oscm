package org.oscm.app.remote;

import javax.ejb.Remote;

@Remote
public interface APPCredentialsUpdateService {
	
	void updateUserCredentials(long userId, String username, String password);
	
}
