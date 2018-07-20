# APP Sample Controller
Sample implementation of a service controller based on the Application Provisioning Platform (APP).

The sample shows how the methods of a service controller could be implemented using the automatic polling mechanisms of APP.
For this purpose, the service controller does not require a real application. It just manages the instance status by means of 
a dispatcher and sends information emails.

For details, refer to the documentation in the source code.  

## Deploy ear file
You can either build `oscm-app-sample.ear` from source or download it from [here](https://github.com/servicecatalog/oscm/releases/download/v17.7/app-sample-controller.zip). Deploy the file in your TomEE deployment directory in the OSCM APP container.
```
docker cp oscm-app-sample.ear oscm-app:/opt/apache-tomee/controllers/
```


## Insert database settings

Ensure oscm-db is running with 'docker ps' and note the image name. Note the root password DB_SUPERPWD you have defined in your env settings, e.g. `less /docker/var.env | grep DB_SUPERPWD` 

Start a temporary container from oscm-db image with a shell

```      
docker run -it --name samplesettings --rm --network docker_default  servicecatalog/oscm-db:latest /bin/bash
```
Set database root password
``` 
export PGPASSWORD=<DB_SUPERPW>
```

Insert following configuration settings for ess.sample. This assumes you have granted the platform administrator technology manager and service manager roles. Otherwise replace read here to grant the roles, or replace `BSS_ORGANIZATION_ID`, `BSS_USER_ID`, `BSS_USER_KEY` and `BSS_USER_PWD` for using a different technology provider.

```
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'VERSION', '1.0');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'APP_PROVISIONING_ON_INSTANCE', 'false');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_ORGANIZATION_ID', 'PLATFORM_OPERATOR');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_ID', 'administrator');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_KEY', '1000');"
psql -h oscm-db -U postgres -d bssapp -c "INSERT INTO bssappuser.configurationsetting (controllerid, settingkey, settingvalue) VALUES ('ess.sample', 'BSS_USER_PWD', '_crypt:admin123');"
```

Stop temporary container with exit. 

## Restart oscm-app
``` 
docker-compose -f docker-compose-oscm.yml restart oscm-app
```
## Deploy and use the service
1. Login as technology manager to the OSCM administrator portal `https://<FQDN>:8081/oscm-portal`. 
2. Get the technical service template and import it into OSCM. Select 'Technical service > Import service definition' and upload the XML file.
3. Define a marketable service and price model on base of AppSampleService and publish it on your marketplace. See [Supplier's Guide](https://github.com/servicecatalog/documentation) for details on how to publish services.
4. Login to the marketplace and subscribe the service.
