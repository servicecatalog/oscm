package org.oscm.app.remote;

import javax.ejb.Remote;

@Remote
public interface APPCredentialsUpdateService {
	
	String updateUserCredentials(long userId, String username, String password);
	
}
