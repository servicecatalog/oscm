&copy; Copyright FUJITSU LIMITED 2018

## Openstack Project Provisioning
You need to configure following technical service parameters to create and 
provision services for Openstack projects in OSCM.

- ```id="RESOURCE_TYPE" valueType="STRING" mandatory="true" configurable="false"```
- ```id="PROJECT_NAME" valueType="STRING" mandatory="true" configurable="true"```
- ```id="PROJECT_USER" valueType="STRING" mandatory="true" configurable="true"```
- ```id="PROJECT_USER_PWD" valueType="STRING" mandatory="true" configurable="true"```
- ```id="PROJECT_QUOTA_CORES" valueType="STRING" mandatory="false" configurable="true"```
- ```id="PROJECT_QUOTA_IP" valueType="STRING" mandatory="false" configurable="true"```
- ```id="PROJECT_QUOTA_GB" valueType="STRING" mandatory="false" configurable="true"```
- ```id="PROJECT_QUOTA_INSTANCES" valueType="STRING" mandatory="false" configurable="true"```
- ```id="PROJECT_QUOTA_KEYS" valueType="STRING" mandatory="false" configurable="true"```
- ```id="PROJECT_QUOTA_VOLUMES" valueType="STRING" mandatory="false" configurable="true"```

###### RESOURCE_TYPE
The resource type parameters defines which type of Openstack resource is provisioned. 
If you want to provision VMs use ```OS::Nova::Server``` as parameter value otherwise 
```OS::Keystone::Project```. Typically you can either provision a VM or a project with
one OSCM service. Therefore a user or a supplier should not configure this parameter.

###### PROJECT_NAME
Defines the name of the Openstack project. This names is for example listed in the Openstack
dashboard under Identity -> Projects. This parameter should be configured by the subscriber.

###### PROJECT_USER, PROJECT_USER_PWD
When an Openstack project is created an administrative user for this project is created as well.
You have to define the name and the password for this user. The subscription will not be created
if the user exists already in Openstack.

###### PROJECT_QUOTA_CORES
Openstack Quota setting to limit the number of CPUS.  

###### PROJECT_QUOTA_IP
Openstack Quota setting to limit the number of floating ips

###### PROJECT_QUOTA_GB
Openstack Quota setting to limit the disk size

###### PROJECT_QUOTA_INSTANCES
Openstack Quota setting to limit the number of VMs

###### PROJECT_QUOTA_KEYS
Openstack Quota setting to limit the number of key pairs

###### PROJECT_QUOTA_VOLUMES
Openstack Quota setting to limit the number of volumes
 
## Usage Collection
The Openstack contoller will automatically collect usage information for all project based OSCM
services. Usage data of an Openstack project is registered as events in the OSCM platform 
for the corresponding OSCM service. The collector collects usage data for following resources:
- ```DISK_GIGABYTE_HOURS```
- ```CPU_HOURS```
- ```RAM_MEGABYTE_HOURS```
- ```TOTAL_HOURS```

In order to register events in OSCM you have to configure two technical service parameters:

```TECHNICAL_SERVICE_ID``` equals the id of the technical service which is defined by the id
attribute of the TechnicalService XML element, e.g. ```<tns:TechnicalService id="OPENSTACK"...```.

The other parameter controls if events should be collected at all for the particular service.
It is called ```IS_CHARGING```.
 
Further you can control how often the controller collects usage information via the 
```TIMER_INTERVAL``` configuration setting. The fetch value is defined in milliseconds. If
the setting is not configured a default value of 4 hours is used. 
