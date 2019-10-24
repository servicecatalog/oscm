package org.oscm.identityservice.rest;

import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.IdentityConfiguration;

public class RestUtils {
    
    public static ApiIdentityClient createClient(String tenantId) {
        IdentityConfiguration config = IdentityConfiguration.of()
                .tenantId(tenantId).sessionContext(null).build();
        ApiIdentityClient client = new ApiIdentityClient(config);
        return client;
    }
}
