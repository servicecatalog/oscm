## APP Sample Controller
Sample implementation of a service controller based on the Application Provisioning Platform (APP).

The sample shows how the methods of a service controller could be implemented using the automatic polling mechanisms of APP.
For this purpose, the service controller does not require a real application. It just manages the instance status by means of 
a dispatcher and sends information emails.

For details, refer to the documentation in the source code.  

# Deploy ear file into TomEE deployment directory 
```
docker cp oscm-app-sample.ear oscm-app:/opt/apache-tomee/controllers/
```

Run docker ps. Ensure oscm-db is running and note the image name. Note the root password DB_SUPERPWD you have defined in env settings, e.g. 'less /docker/var.env | grep DB_SUPERPWD'

# Insert database settings

Start a temporary container from oscm-db image with a shell

```      
docker run -it --name samplesettings --rm --network docker_default  servicecatalog/oscm-db:latest /bin/bash
```
Set database root password
``` 
export PGPASSWORD=<DB_SUPERPW>
```

Insert following configuration settings for ess.sample. This assumes you have granted the platform administrator technology manager and service manager roles. Otherwise replace read here to grant the roles, or 
 replace BSS_ORGANIZATION_ID, BSS_USER_ID, BSS_USER_KEY and BSS_USER_PWD with a different technology provider.

```
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'VERSION', '1.0');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'APP_PROVISIONING_ON_INSTANCE', 'false');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_ORGANIZATION_ID', 'PLATFORM_OPERATOR');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_ID', 'administrator');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_KEY', '1000');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_PWD', '_crypt:admin123');"
```

Stop temporary container with exit and restart oscm-app
``` 
docker-compose -f docker-compose-oscm.yml restart oscm-app
```
