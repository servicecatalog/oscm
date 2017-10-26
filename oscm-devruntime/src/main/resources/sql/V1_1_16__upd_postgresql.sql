-- CREATE SCHEMA 

-- ----------------------------------------------
-- DDL-Anweisungen für Tabellen
-- ----------------------------------------------
CREATE TABLE "billingcontact" (
		"tkey" BIGINT NOT NULL,
		"address" VARCHAR(255),
		"companyname" VARCHAR(255),
		"email" VARCHAR(255) NOT NULL,
		"orgaddressused" BOOLEAN NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL
	);
	
CREATE TABLE "billingcontacthistory" (
		"tkey" BIGINT NOT NULL,
		"address" VARCHAR(255),
		"companyname" VARCHAR(255),
		"email" VARCHAR(255) NOT NULL,
		"orgaddressused" BOOLEAN NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "billingresult" (
		"tkey" BIGINT NOT NULL,
		"creationtime" BIGINT NOT NULL,
		"organizationtkey" BIGINT NOT NULL,
		"periodendtime" BIGINT NOT NULL,
		"periodstarttime" BIGINT NOT NULL,
		"resultxml" TEXT NOT NULL,
		"version" INTEGER NOT NULL
	);	
	
CREATE TABLE "configurationsetting" (
		"tkey" BIGINT NOT NULL,
		"context_id" VARCHAR(255) NOT NULL,
		"information_id" VARCHAR(255) NOT NULL,
		"env_value" VARCHAR(255),
		"version" INTEGER NOT NULL
	);	
	
CREATE TABLE "discount" (
		"tkey" BIGINT NOT NULL,
		"endtime" BIGINT,
		"starttime" BIGINT,
		"value" NUMERIC(19 , 2) NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "discounthistory" (
		"tkey" BIGINT NOT NULL,
		"endtime" BIGINT,
		"starttime" BIGINT,
		"value" NUMERIC(19 , 2) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "event" (
		"tkey" BIGINT NOT NULL,
		"eventidentifier" VARCHAR(255) NOT NULL,
		"eventtype" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"technicalproduct_tkey" BIGINT
	);	
	
CREATE TABLE "eventhistory" (
		"tkey" BIGINT NOT NULL,
		"eventidentifier" VARCHAR(255) NOT NULL,
		"eventtype" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"technicalproductobjkey" BIGINT
	);	

CREATE TABLE "gatheredevent" (
		"tkey" BIGINT NOT NULL,
		"actor" VARCHAR(255),
		"eventidentifier" VARCHAR(255) NOT NULL,
		"multiplier" BIGINT NOT NULL,
		"occurrencetime" BIGINT NOT NULL,
		"subscriptiontkey" BIGINT NOT NULL,
		"type" VARCHAR(255) NOT NULL,
		"uniqueid" VARCHAR(255),
		"version" INTEGER NOT NULL,
		"billingresult_tkey" BIGINT
	);	
	
CREATE TABLE "hibernate_sequences" (
		"sequence_name" VARCHAR(255),
		"sequence_next_hi_value" INTEGER
	);
	
CREATE TABLE "imageresource" (
		"imagetype" VARCHAR(255) NOT NULL,
		"objectkey" BIGINT NOT NULL,
		"buffer" OID NOT NULL,
		"contenttype" VARCHAR(255)
	);	
	
CREATE TABLE "localizedresource" (
		"locale" VARCHAR(255) NOT NULL,
		"objectkey" BIGINT NOT NULL,
		"objecttype" VARCHAR(255) NOT NULL,
		"value" TEXT NOT NULL
	);	

CREATE TABLE "organization" (
		"tkey" BIGINT NOT NULL,
		"address" VARCHAR(255),
		"deregistrationdate" BIGINT,
		"distinguishedname" VARCHAR(4096),
		"email" VARCHAR(255),
		"locale" VARCHAR(255),
		"name" VARCHAR(255),
		"organizationid" VARCHAR(255) NOT NULL,
		"phone" VARCHAR(255),
		"pspidentifier" VARCHAR(255),
		"registrationdate" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"defaultpayment_tkey" BIGINT,
		"supplierkey" BIGINT
	);	
	
CREATE TABLE "organizationhistory" (
		"tkey" BIGINT NOT NULL,
		"address" VARCHAR(255),
		"deregistrationdate" BIGINT,
		"distinguishedname" VARCHAR(4096),
		"email" VARCHAR(255),
		"locale" VARCHAR(255),
		"name" VARCHAR(255),
		"organizationid" VARCHAR(255) NOT NULL,
		"phone" VARCHAR(255),
		"pspidentifier" VARCHAR(255),
		"registrationdate" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"defaultpaymentobjkey" BIGINT,
		"supplierobjkey" BIGINT
	);	

CREATE TABLE "organizationreference" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"supplierkey" BIGINT NOT NULL,
		"technologyproviderkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "organizationreferencehistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"suppliertkey" BIGINT NOT NULL,
		"technologyprovidertkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "organizationrole" (
		"tkey" BIGINT NOT NULL,
		"rolename" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	);
	
CREATE TABLE "organizationsetting" (
		"tkey" BIGINT NOT NULL,
		"settingtype" VARCHAR(255) NOT NULL,
		"settingvalue" VARCHAR(255),
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "organizationtopaymenttype" (
		"tkey" BIGINT NOT NULL,
		"usedasdefault" BOOLEAN NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL,
		"organizationrole_tkey" BIGINT NOT NULL,
		"paymenttype_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "organizationtorole" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL,
		"organizationrole_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "organizationtorolehistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationroletkey" BIGINT NOT NULL,
		"organizationtkey" BIGINT NOT NULL
	);	

CREATE TABLE "parameter" (
		"tkey" BIGINT NOT NULL,
		"configurable" BOOLEAN NOT NULL,
		"value" VARCHAR(255),
		"version" INTEGER NOT NULL,
		"parameterdefinitionkey" BIGINT NOT NULL,
		"parametersetkey" BIGINT NOT NULL
	);
	

CREATE TABLE "parameterhistory" (
		"tkey" BIGINT NOT NULL,
		"configurable" BOOLEAN NOT NULL,
		"value" VARCHAR(255),
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"parameterdefinitionobjkey" BIGINT NOT NULL,
		"parametersetobjkey" BIGINT NOT NULL
	);		
	
CREATE TABLE "parameterdefinition" (
		"tkey" BIGINT NOT NULL,
		"configurable" BOOLEAN NOT NULL,
		"defaultvalue" VARCHAR(255),
		"mandatory" BOOLEAN NOT NULL,
		"maximumvalue" BIGINT,
		"minimumvalue" BIGINT,
		"parameterid" VARCHAR(255) NOT NULL,
		"parametertype" VARCHAR(255) NOT NULL,
		"valuetype" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"technicalproduct_tkey" BIGINT
	);	
	
CREATE TABLE "parameterdefinitionhistory" (
		"tkey" BIGINT NOT NULL,
		"configurable" BOOLEAN NOT NULL,
		"defaultvalue" VARCHAR(255),
		"mandatory" BOOLEAN NOT NULL,
		"maximumvalue" BIGINT,
		"minimumvalue" BIGINT,
		"parameterid" VARCHAR(255) NOT NULL,
		"parametertype" VARCHAR(255) NOT NULL,
		"valuetype" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"technicalproductobjkey" BIGINT
	);	
	
CREATE TABLE "parameteroption" (
		"tkey" BIGINT NOT NULL,
		"optionid" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"parameterdefinition_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "parameteroptionhistory" (
		"tkey" BIGINT NOT NULL,
		"optionid" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"parameterdefobjkey" BIGINT NOT NULL
	);
	
CREATE TABLE "parameterset" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL
	);	
	
CREATE TABLE "parametersethistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL
	);	
	
CREATE TABLE "paymentinfo" (
		"tkey" BIGINT NOT NULL,
		"creationtime" BIGINT NOT NULL,
		"externalidentifier" VARCHAR(255),
		"version" INTEGER NOT NULL,
		"organizationkey" BIGINT NOT NULL,
		"paymenttype_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "paymentinfohistory" (
		"tkey" BIGINT NOT NULL,
		"creationtime" BIGINT NOT NULL,
		"externalidentifier" VARCHAR(255),
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL,
		"paymenttypeobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "paymentresult" (
		"tkey" BIGINT NOT NULL,
		"processingexception" TEXT,
		"processingresult" TEXT,
		"processingstatus" VARCHAR(255) NOT NULL,
		"processingtime" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"billingresult_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "paymentresulthistory" (
		"tkey" BIGINT NOT NULL,
		"processingexception" TEXT,
		"processingresult" TEXT,
		"processingstatus" VARCHAR(255) NOT NULL,
		"processingtime" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"billingresultobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "paymenttype" (
		"tkey" BIGINT NOT NULL,
		"collectiontype" VARCHAR(255) NOT NULL,
		"paymenttypeid" VARCHAR(255) NOT NULL,
		"psppaymenttypeid" VARCHAR(255),
		"version" INTEGER NOT NULL
	);	

CREATE TABLE "platformuser" (
		"tkey" BIGINT NOT NULL,
		"additionalname" VARCHAR(255),
		"address" VARCHAR(255),
		"creationdate" BIGINT NOT NULL,
		"email" VARCHAR(255),
		"failedlogincounter" INTEGER NOT NULL,
		"firstname" VARCHAR(255),
		"lastname" VARCHAR(255),
		"locale" VARCHAR(255) NOT NULL,
		"organizationadmin" BOOLEAN NOT NULL,
		"phone" VARCHAR(255),
		"salutation" VARCHAR(255),
		"status" VARCHAR(255) NOT NULL,
		"passwordsalt" BIGINT NOT NULL DEFAULT 0,
		"passwordhash" bytea,
		"userid" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"organizationkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "platformuserhistory" (
		"tkey" BIGINT NOT NULL,
		"additionalname" VARCHAR(255),
		"address" VARCHAR(255),
		"creationdate" BIGINT NOT NULL,
		"email" VARCHAR(255),
		"failedlogincounter" INTEGER NOT NULL,
		"firstname" VARCHAR(255),
		"lastname" VARCHAR(255),
		"locale" VARCHAR(255) NOT NULL,
		"organizationadmin" BOOLEAN NOT NULL,
		"phone" VARCHAR(255),
		"salutation" VARCHAR(255),
		"status" VARCHAR(255) NOT NULL,
		"passwordsalt" BIGINT NOT NULL DEFAULT 0,
		"passwordhash" bytea,
		"userid" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricedevent" (
		"tkey" BIGINT NOT NULL,
		"eventprice" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"eventkey" BIGINT NOT NULL,
		"pricemodelkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricedeventhistory" (
		"tkey" BIGINT NOT NULL,
		"eventprice" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"eventobjkey" BIGINT NOT NULL,
		"pricemodelobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricedproductrole" (
		"tkey" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"pricemodel_tkey" BIGINT,
		"pricedoption_tkey" BIGINT,
		"pricedparameter_tkey" BIGINT,
		"roledefinition_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricedoption" (
		"tkey" BIGINT NOT NULL,
		"pricepersubscription" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"parameteroptionkey" BIGINT,
		"pricedparameter_tkey" BIGINT
	);	
	
CREATE TABLE "pricedoptionhistory" (
		"tkey" BIGINT NOT NULL,
		"pricepersubscription" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"parameteroptionobjkey" BIGINT NOT NULL,
		"pricedparameterobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricedparameter" (
		"tkey" BIGINT NOT NULL,
		"pricepersubscription" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"parameter_tkey" BIGINT,
		"pricemodelkey" BIGINT
	);	
	
CREATE TABLE "pricedparameterhistory" (
		"tkey" BIGINT NOT NULL,
		"pricepersubscription" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"parameterobjkey" BIGINT NOT NULL,
		"pricemodelobjkey" BIGINT NOT NULL
	);
	
CREATE TABLE "pricedproductrolehistory" (
		"tkey" BIGINT NOT NULL,
		"priceperuser" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"pricemodelobjkey" BIGINT,
		"pricedoptionobjkey" BIGINT,
		"pricedparameterobjkey" BIGINT,
		"roledefinitionobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "pricemodel" (
		"tkey" BIGINT NOT NULL,
		"ischargeable" BOOLEAN NOT NULL,
		"onetimefee" BIGINT NOT NULL,
		"period" VARCHAR(255),
		"periodhandling" VARCHAR(255),
		"priceperperiod" BIGINT NOT NULL,
		"priceperuserassignment" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"currency_tkey" BIGINT
	);	
	
CREATE TABLE "pricemodelhistory" (
		"tkey" BIGINT NOT NULL,
		"ischargeable" BOOLEAN NOT NULL,
		"onetimefee" BIGINT NOT NULL,
		"period" VARCHAR(255),
		"periodhandling" VARCHAR(255),
		"priceperperiod" BIGINT NOT NULL,
		"priceperuserassignment" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"currencyobjkey" BIGINT,
		"productobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "product" (
		"tkey" BIGINT NOT NULL,
		"deprovisioningdate" BIGINT,
		"descriptionurl" VARCHAR(255),
		"productid" VARCHAR(255) NOT NULL,
		"provisioningdate" BIGINT NOT NULL,
		"status" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"supplierkey" BIGINT NOT NULL,
		"parameterset_tkey" BIGINT,
		"pricemodel_tkey" BIGINT,
		"targetcustomer_tkey" BIGINT,
		"technicalproduct_tkey" BIGINT NOT NULL,
		"template_tkey" BIGINT
	);	
	

CREATE TABLE "producthistory" (
		"tkey" BIGINT NOT NULL,
		"deprovisioningdate" BIGINT,
		"descriptionurl" VARCHAR(255),
		"productid" VARCHAR(255) NOT NULL,
		"provisioningdate" BIGINT NOT NULL,
		"status" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"parametersetobjkey" BIGINT,
		"pricemodelobjkey" BIGINT,
		"supplierobjkey" BIGINT NOT NULL,
		"targetcustomerobjkey" BIGINT,
		"technicalproductobjkey" BIGINT NOT NULL,
		"templateobjkey" BIGINT
	);	
	
CREATE TABLE "productreference" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"sourceproduct_tkey" BIGINT NOT NULL,
		"targetproduct_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "productreferencehistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"sourceproducttkey" BIGINT NOT NULL,
		"targetproducttkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "report" (
		"tkey" BIGINT NOT NULL,
		"reportname" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"organizationrole_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "roledefinition" (
		"tkey" BIGINT NOT NULL,
		"roleid" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"technicalproduct_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "roledefinitionhistory" (
		"tkey" BIGINT NOT NULL,
		"roleid" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"technicalproductobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "session" (
		"tkey" BIGINT NOT NULL,
		"nodename" VARCHAR(255) NOT NULL,
		"platformuserid" VARCHAR(255) NOT NULL,
		"platformuserkey" BIGINT NOT NULL,
		"sessionid" VARCHAR(255),
		"sessiontype" VARCHAR(255),
		"subscriptiontkey" BIGINT,
		"usertoken" VARCHAR(255),
		"version" INTEGER NOT NULL
	);
	
CREATE TABLE "shop" (
		"tkey" BIGINT NOT NULL,
		"creationdate" BIGINT NOT NULL,
		"skinproperties" TEXT,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "shophistory" (
		"tkey" BIGINT NOT NULL,
		"creationdate" BIGINT NOT NULL,
		"skinproperties" TEXT,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "steppedprice" (
		"tkey" BIGINT NOT NULL, 
		"version" INTEGER NOT NULL, 
		"pricemodel_tkey" BIGINT, 
		"pricedevent_tkey" BIGINT, 
		"pricedparameter_tkey" BIGINT, 
		"upperlimit" BIGINT, 
		"price" BIGINT NOT NULL,
		"additionalprice" BIGINT NOT NULL, 
		"freeamount" BIGINT NOT NULL
	);

CREATE TABLE "steppedpricehistory" (
		"tkey" BIGINT NOT NULL, 
		"moddate" TIMESTAMP NOT NULL, 
		"modtype" VARCHAR(255) NOT NULL, 
		"moduser" VARCHAR(255) NOT NULL, 
		"objkey" BIGINT NOT NULL, 
		"objversion" BIGINT NOT NULL, 
		"pricemodelobjkey" BIGINT, 
		"pricedeventobjkey" BIGINT, 
		"pricedparameterobjkey" BIGINT , 
		"upperlimit" BIGINT, 
		"price" BIGINT NOT NULL, 
		"additionalprice" BIGINT NOT NULL, 
		"freeamount" BIGINT NOT NULL
	);	
	
CREATE TABLE "subscription" (
		"tkey" BIGINT NOT NULL,
		"accessinfo" VARCHAR(255),
		"activationdate" BIGINT,
		"baseurl" VARCHAR(255),
		"creationdate" BIGINT NOT NULL,
		"deactivationdate" BIGINT,
		"loginpath" VARCHAR(255),
		"productinstanceid" VARCHAR(255),
		"purchaseordernumber" VARCHAR(255),
		"status" VARCHAR(255) NOT NULL,
		"subscriptionid" VARCHAR(255) NOT NULL,
		"timeoutmailsent" BOOLEAN NOT NULL,
		"version" INTEGER NOT NULL,
		"organizationkey" BIGINT NOT NULL,
		"product_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "subscriptionhistory" (
		"tkey" BIGINT NOT NULL,
		"accessinfo" VARCHAR(255),
		"activationdate" BIGINT,
		"baseurl" VARCHAR(255),
		"creationdate" BIGINT NOT NULL,
		"deactivationdate" BIGINT,
		"loginpath" VARCHAR(255),
		"productinstanceid" VARCHAR(255),
		"purchaseordernumber" VARCHAR(255),
		"status" VARCHAR(255) NOT NULL,
		"subscriptionid" VARCHAR(255) NOT NULL,
		"timeoutmailsent" BOOLEAN NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL,
		"productobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "supportedcurrency" (
		"tkey" BIGINT NOT NULL,
		"currencyisocode" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	);
	
CREATE TABLE "technicalproduct" (
		"tkey" BIGINT NOT NULL,
		"accesstype" VARCHAR(255),
		"baseurl" VARCHAR(255),
		"descriptionurl" VARCHAR(255),
		"loginpath" VARCHAR(255),
		"provisioningpassword" VARCHAR(255),
		"provisioningtimeout" BIGINT,
		"provisioningtype" VARCHAR(255),
		"provisioningurl" VARCHAR(255) NOT NULL,
		"provisioningusername" VARCHAR(255),
		"provisioningversion" VARCHAR(255),
		"technicalproductbuildid" VARCHAR(255),
		"technicalproductid" VARCHAR(255) NOT NULL,
		"technicalproductversion" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"organizationkey" BIGINT NOT NULL
	);	

CREATE TABLE "technicalproducthistory" (
		"tkey" BIGINT NOT NULL,
		"accesstype" VARCHAR(255),
		"baseurl" VARCHAR(255),
		"descriptionurl" VARCHAR(255),
		"loginpath" VARCHAR(255),
		"provisioningpassword" VARCHAR(255),
		"provisioningtimeout" BIGINT,
		"provisioningtype" VARCHAR(255),
		"provisioningurl" VARCHAR(255) NOT NULL,
		"provisioningusername" VARCHAR(255),
		"provisioningversion" VARCHAR(255),
		"technicalproductbuildid" VARCHAR(255),
		"technicalproductid" VARCHAR(255) NOT NULL,
		"technicalproductversion" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "technicalproductoperation" (
		"tkey" BIGINT NOT NULL,
		"actionurl" VARCHAR(255) NOT NULL,
		"operationid" VARCHAR(255) NOT NULL,
		"operationtype" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"technicalproduct_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "technicalproductoperationhistory" (
		"tkey" BIGINT NOT NULL,
		"actionurl" VARCHAR(255) NOT NULL,
		"operationid" VARCHAR(255) NOT NULL,
		"operationtype" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"technicalproductobjkey" BIGINT NOT NULL
	);
	
CREATE TABLE "timerprocessing" (
		"tkey" BIGINT NOT NULL,
		"duration" BIGINT NOT NULL,
		"nodename" VARCHAR(255) NOT NULL,
		"starttime" BIGINT NOT NULL,
		"starttimemutex" BIGINT NOT NULL,
		"success" BOOLEAN NOT NULL,
		"timertype" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	);	
	
CREATE TABLE "triggerdefinition" (
		"tkey" BIGINT NOT NULL,
		"suspendprocess" BOOLEAN NOT NULL,
		"target" VARCHAR(255),
		"targettype" VARCHAR(255) NOT NULL,
		"type" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT
	);	
	
CREATE TABLE "triggerdefinitionhistory" (
		"tkey" BIGINT NOT NULL,
		"suspendprocess" BOOLEAN NOT NULL,
		"target" VARCHAR(255),
		"targettype" VARCHAR(255) NOT NULL,
		"type" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "triggerprocess" (
		"tkey" BIGINT NOT NULL,
		"activationdate" BIGINT NOT NULL,
		"status" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL,
		"triggerdefinition_tkey" BIGINT NOT NULL,
		"user_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "triggerprocesshistory" (
		"tkey" BIGINT NOT NULL,
		"activationdate" BIGINT NOT NULL,
		"status" VARCHAR(255) NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"triggerdefinitionobjkey" BIGINT NOT NULL,
		"userobjkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "triggerprocessparameter" (
		"tkey" BIGINT NOT NULL,
		"name" VARCHAR(255) NOT NULL,
		"serializedvalue" TEXT,
		"version" INTEGER NOT NULL,
		"triggerprocess_tkey" BIGINT NOT NULL
	);	

CREATE TABLE "usagelicense" (
		"tkey" BIGINT NOT NULL,
		"applicationuserid" VARCHAR(255),
		"assignmentdate" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"roledefinition_tkey" BIGINT,
		"subscription_tkey" BIGINT NOT NULL,
		"user_tkey" BIGINT NOT NULL
	);
	
CREATE TABLE "usagelicensehistory" (
		"tkey" BIGINT NOT NULL,
		"applicationuserid" VARCHAR(255),
		"assignmentdate" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"roledefinitionobjkey" BIGINT,
		"subscriptionobjkey" BIGINT NOT NULL,
		"userobjkey" BIGINT NOT NULL
	);
	
CREATE TABLE "version" (
		"productmajorversion" INTEGER NOT NULL, 
		"productminorversion" INTEGER NOT NULL, 
		"schemaversion" INTEGER NOT NULL, 
		"migrationdate" TIMESTAMP
	);	

-- ----------------------------------------------
-- DDL-Anweisungen für Schlüssel
-- ----------------------------------------------

-- indexes

CREATE UNIQUE INDEX "billingcontact_orgkey_uidx" ON "billingcontact" ("organization_tkey");
CREATE INDEX "billingcontact_orgkey_nuidx" ON "billingcontact" ("organization_tkey");

CREATE UNIQUE INDEX "configurationsetting_idctx_idx" ON "configurationsetting" ("information_id" ASC, "context_id" ASC);

CREATE UNIQUE INDEX "discount_orgkey_uidx" ON "discount" ("organization_tkey");	
CREATE INDEX "discount_orgkey_nuidx" ON "discount" ("organization_tkey");

CREATE INDEX "event_tp_nuidx" ON "event" ("technicalproduct_tkey");

CREATE INDEX "gatheredevent_billres_nuidx" ON "gatheredevent" ("billingresult_tkey");
CREATE UNIQUE INDEX "gatheredevent_subuid_idx" ON "gatheredevent" ("subscriptiontkey" ASC, "uniqueid" ASC);

CREATE UNIQUE INDEX "organization_bk_idx" ON "organization" ("organizationid");
CREATE INDEX "organization_defpay_nuidx" ON "organization" ("defaultpayment_tkey");
CREATE INDEX "organization_sup_nuidx" ON "organization" ("supplierkey");

CREATE INDEX "organizationreference_sup_nuidx" ON "organizationreference" ("supplierkey");
CREATE INDEX "organizationreference_tp_nuidx" ON "organizationreference" ("technologyproviderkey");

CREATE UNIQUE INDEX "organizationrole_role_uidx" ON "organizationrole" ("rolename");

CREATE INDEX "organizationsetting_org_nuidx" ON "organizationsetting" ("organization_tkey");

CREATE UNIQUE INDEX "orgtopaymenttype_bk_idx" ON "organizationtopaymenttype" ("organization_tkey" asc, "paymenttype_tkey" asc, "organizationrole_tkey" asc);
CREATE INDEX "orgtopaymenttype_org_nuidx" ON "organizationtopaymenttype" ("organization_tkey");
CREATE INDEX "orgtopaymenttype_pt_nuidx" ON "organizationtopaymenttype" ("paymenttype_tkey");
CREATE INDEX "orgtopaymenttype_orgrole_nuidx" ON "organizationtopaymenttype" ("organizationrole_tkey");

CREATE UNIQUE INDEX "organizationtorole_org_uidx" ON "organizationtorole" ("organization_tkey" asc, "organizationrole_tkey" asc);
CREATE INDEX "organizationtorole_org_nuidx" ON "organizationtorole" ("organization_tkey");
CREATE INDEX "organizationtorole_orgrole_nuidx" ON "organizationtorole" ("organizationrole_tkey");

CREATE INDEX "parameter_paramset_nuidx" ON "parameter" ("parametersetkey");
CREATE INDEX "parameter_paramdef_nuidx" ON "parameter" ("parameterdefinitionkey");

CREATE INDEX "parameterdefinition_tp_nuidx" ON "parameterdefinition" ("technicalproduct_tkey");

CREATE INDEX "parameteroption_paramdef_nuidx" ON "parameteroption" ("parameterdefinition_tkey");

CREATE INDEX "paymentinfo_ptype_nuidx" ON "paymentinfo" ("paymenttype_tkey");

CREATE INDEX "paymentresult_billres_nuidx" ON "paymentresult" ("billingresult_tkey");

CREATE UNIQUE INDEX "paymenttype_ptype_uidx" ON "paymenttype" ("paymenttypeid");

CREATE UNIQUE INDEX "platformuser_bk_idx" ON "platformuser" ("userid" asc, "organizationkey" asc);
CREATE INDEX "platformuser_org_nuidx" ON "platformuser" ("organizationkey");

CREATE INDEX "pricedevent_pm_nuidx" ON "pricedevent" ("pricemodelkey");
CREATE INDEX "pricedevent_evt_nuidx" ON "pricedevent" ("eventkey");

CREATE INDEX "pricedoption_pparam_nuidx" ON "pricedoption" ("pricedparameter_tkey");
CREATE INDEX "pricedoption_paramopt_nuidx" ON "pricedoption" ("parameteroptionkey");

CREATE INDEX "pricedparameter_param_nuidx" ON "pricedparameter" ("parameter_tkey");
CREATE INDEX "pricedparameter_pm_nuidx" ON "pricedparameter" ("pricemodelkey");

CREATE INDEX "pricedproductrole_roledef_nuidx" ON "pricedproductrole" ("roledefinition_tkey");
CREATE INDEX "pricedproductrole_pm_nuidx" ON "pricedproductrole" ("pricemodel_tkey");
CREATE INDEX "pricedproductrole_pparam_nuidx" ON "pricedproductrole" ("pricedparameter_tkey");
CREATE INDEX "pricedproductrole_popt_nuidx" ON "pricedproductrole" ("pricedoption_tkey");

CREATE INDEX "pricemodel_currency_nuidx" ON "pricemodel" ("currency_tkey");

CREATE UNIQUE INDEX "product_bk_idx" ON "product" ("productid" asc, "supplierkey" asc);
CREATE INDEX "product_pm_nuidx" ON "product" ("pricemodel_tkey");
CREATE INDEX "product_paramset_nuidx" ON "product" ("parameterset_tkey");
CREATE INDEX "product_tp_nuidx" ON "product" ("technicalproduct_tkey");
CREATE INDEX "product_supp_nuidx" ON "product" ("supplierkey");
CREATE INDEX "product_template_nuidx" ON "product" ("template_tkey");
CREATE INDEX "product_targetcust_nuidx" ON "product" ("targetcustomer_tkey");

CREATE INDEX "productreference_srcprod_nuidx" ON "productreference" ("sourceproduct_tkey");
CREATE INDEX "productreference_tgtprod_nuidx" ON "productreference" ("targetproduct_tkey");

CREATE INDEX "report_orgrole_nuidx" ON "report" (organizationrole_tkey);

CREATE INDEX "roledefinition_tp_nuidx" ON "roledefinition" ("technicalproduct_tkey");

CREATE INDEX "shop_org_nuidx" ON "shop" ("organization_tkey");

CREATE INDEX "steppedprice_pm_nuidx" ON "steppedprice" ("pricemodel_tkey");
CREATE INDEX "steppedprice_pevent_nuidx" ON "steppedprice" ("pricedevent_tkey");
CREATE INDEX "steppedprice_pparam_nuidx" ON "steppedprice" ("pricedparameter_tkey");

CREATE UNIQUE INDEX "subscription_bk_idx" ON "subscription" ("subscriptionid" asc, "organizationkey" asc);
CREATE INDEX "subscription_org_nuidx" ON "subscription" ("organizationkey");
CREATE INDEX "subscription_prod_nuidx" ON "subscription" ("product_tkey");

CREATE UNIQUE INDEX "supportedcurrency_bk_idx" ON "supportedcurrency" ("currencyisocode");

CREATE UNIQUE INDEX "technicalproduct_bk_idx" ON "technicalproduct" ("technicalproductversion" asc, "technicalproductid" asc, "organizationkey" asc);
CREATE INDEX "technicalproduct_org_nuidx" ON "technicalproduct" ("organizationkey");

CREATE UNIQUE INDEX "tprodoperation_bk_idx" ON "technicalproductoperation" ("technicalproduct_tkey" asc, "operationid" asc);
CREATE INDEX "tprodoperation_tp_nuidx" ON "technicalproductoperation" ("technicalproduct_tkey");

CREATE UNIQUE INDEX "timerprocessing_uc_uidx" ON "timerprocessing" ("timertype" asc, "starttimemutex" asc);

CREATE INDEX "triggerdefinition_org_nuidx" ON "triggerdefinition" ("organization_tkey");

CREATE INDEX "triggerprocess_trigdef_nuidx" ON "triggerprocess" ("triggerdefinition_tkey");
CREATE INDEX "triggerprocess_user_nuidx" ON "triggerprocess" ("user_tkey");

CREATE INDEX "triggerprocessparameter_trigproc_nuidx" ON "triggerprocessparameter" ("triggerprocess_tkey");

CREATE INDEX "usagelicense_sub_nuidx" ON "usagelicense" ("subscription_tkey");
CREATE INDEX "usagelicense_user_nuidx" ON "usagelicense" ("user_tkey");
CREATE INDEX "usagelicense_roledef_nuidx" ON "usagelicense" ("roledefinition_tkey");

---------------------
-- primary keys
---------------------

ALTER TABLE "billingresult" ADD CONSTRAINT "billingresult_pk" PRIMARY KEY ("tkey");
ALTER TABLE "billingcontact" ADD CONSTRAINT "billingcontact_pk" PRIMARY KEY ("tkey");
ALTER TABLE "billingcontacthistory" ADD CONSTRAINT "billingcontacthistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "configurationsetting" ADD CONSTRAINT "configurationsetting_pk" PRIMARY KEY ("tkey");
ALTER TABLE "discount" ADD CONSTRAINT "discount_pk" PRIMARY KEY ("tkey");
ALTER TABLE "discounthistory" ADD CONSTRAINT "discounthistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "event" ADD CONSTRAINT "event_pk" PRIMARY KEY ("tkey");
ALTER TABLE "eventhistory" ADD CONSTRAINT "eventhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "gatheredevent" ADD CONSTRAINT "gatheredevent_pk" PRIMARY KEY ("tkey");
ALTER TABLE "localizedresource" ADD CONSTRAINT "localizedresource_pk" PRIMARY KEY ("locale", "objectkey", "objecttype");
ALTER TABLE "organization" ADD CONSTRAINT "organization_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationhistory" ADD CONSTRAINT "organizationhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationtopaymenttype" ADD CONSTRAINT "organizationtopaymenttype_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationreference" ADD CONSTRAINT "organizationreference_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationreferencehistory" ADD CONSTRAINT "organizationreferencehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationrole" ADD CONSTRAINT "organizationrole_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationsetting" ADD CONSTRAINT "organizationsetting_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationtorole" ADD CONSTRAINT "organizationtorole_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationtorolehistory" ADD CONSTRAINT "organizationtorolehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameter" ADD CONSTRAINT "parameter_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameterdefinition" ADD CONSTRAINT "parameterdefinition_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameterdefinitionhistory" ADD CONSTRAINT "parameterdefinitionhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameterhistory" ADD CONSTRAINT "parameterhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameteroption" ADD CONSTRAINT "parameteroption_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parameteroptionhistory" ADD CONSTRAINT "parameteroptionhistory_uc" PRIMARY KEY ("tkey");
ALTER TABLE "parameterset" ADD CONSTRAINT "parameterset_pk" PRIMARY KEY ("tkey");
ALTER TABLE "parametersethistory" ADD CONSTRAINT "parametersethistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "paymentinfo" ADD CONSTRAINT "paymentinfo_pk" PRIMARY KEY ("tkey");
ALTER TABLE "paymentinfohistory" ADD CONSTRAINT "paymentinfohistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "paymentresult" ADD CONSTRAINT "paymentresult_pk" PRIMARY KEY ("tkey");
ALTER TABLE "paymentresulthistory" ADD CONSTRAINT "paymentresulthistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "paymenttype" ADD CONSTRAINT "paymenttype_pk" PRIMARY KEY ("tkey");
ALTER TABLE "platformuser" ADD CONSTRAINT "platformuser_pk" PRIMARY KEY ("tkey");
ALTER TABLE "platformuserhistory" ADD CONSTRAINT "platformuserhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedevent" ADD CONSTRAINT "pricedevent_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedeventhistory" ADD CONSTRAINT "pricedeventhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedoption" ADD CONSTRAINT "pricedoption_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedoptionhistory" ADD CONSTRAINT "pricedoptionhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedparameter" ADD CONSTRAINT "pricedparameter_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedparameterhistory" ADD CONSTRAINT "pricedparameterhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedproductrole" ADD CONSTRAINT "pricedproductrole_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricedproductrolehistory" ADD CONSTRAINT "pricedproductrolehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricemodel" ADD CONSTRAINT "pricemodel_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pricemodelhistory" ADD CONSTRAINT "pricemodelhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "product" ADD CONSTRAINT "product_pk" PRIMARY KEY ("tkey");
ALTER TABLE "producthistory" ADD CONSTRAINT "producthistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "productreference" ADD CONSTRAINT "productreference_pk" PRIMARY KEY ("tkey");
ALTER TABLE "productreferencehistory" ADD CONSTRAINT "productreferencehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "report" ADD CONSTRAINT "report_pk" PRIMARY KEY ("tkey");
ALTER TABLE "roledefinition" ADD CONSTRAINT "roledefinition_pk" PRIMARY KEY ("tkey");
ALTER TABLE "roledefinitionhistory" ADD CONSTRAINT "roledefinitionhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "session" ADD CONSTRAINT "session_pk" PRIMARY KEY ("tkey");
ALTER TABLE "shop" ADD CONSTRAINT "shop_pk" PRIMARY KEY ("tkey");
ALTER TABLE "shophistory" ADD CONSTRAINT "shophistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "steppedprice" ADD CONSTRAINT "steppedprice_pk" PRIMARY KEY ("tkey");
ALTER TABLE "steppedpricehistory" ADD CONSTRAINT "steppedpricehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "subscription" ADD CONSTRAINT "subscription_pk" PRIMARY KEY ("tkey");
ALTER TABLE "subscriptionhistory" ADD CONSTRAINT "subscriptionhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "supportedcurrency" ADD CONSTRAINT "supportedcurrency_pk" PRIMARY KEY ("tkey");
ALTER TABLE "technicalproduct" ADD CONSTRAINT "technicalproduct_pk" PRIMARY KEY ("tkey");
ALTER TABLE "technicalproducthistory" ADD CONSTRAINT "technicalproducthistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "technicalproductoperation" ADD CONSTRAINT "technicalproductoperation_pk" PRIMARY KEY ("tkey");
ALTER TABLE "technicalproductoperationhistory" ADD CONSTRAINT "technicalproductoperationhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "timerprocessing" ADD CONSTRAINT "timerprocessing_pk" PRIMARY KEY ("tkey");
ALTER TABLE "triggerdefinition" ADD CONSTRAINT "triggerdefinition_pk" PRIMARY KEY ("tkey");
ALTER TABLE "triggerdefinitionhistory" ADD CONSTRAINT "triggerdefinitionhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "triggerprocess" ADD CONSTRAINT "triggerprocess_pk" PRIMARY KEY ("tkey");
ALTER TABLE "triggerprocesshistory" ADD CONSTRAINT "triggerprocesshistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "triggerprocessparameter" ADD CONSTRAINT "triggerprocessmodification_pk" PRIMARY KEY ("tkey");
ALTER TABLE "usagelicense" ADD CONSTRAINT "usagelicense_pk" PRIMARY KEY ("tkey");
ALTER TABLE "usagelicensehistory" ADD CONSTRAINT "usagelicensehistory_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "billingcontact" ADD CONSTRAINT "billingcontact_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");

ALTER TABLE "discount" ADD CONSTRAINT "discount_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
	
ALTER TABLE "event" ADD CONSTRAINT "event_to_techprod_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");	
	
ALTER TABLE "gatheredevent" ADD CONSTRAINT "gathevt_to_billres_fk" FOREIGN KEY ("billingresult_tkey")
	REFERENCES "billingresult" ("tkey");	

ALTER TABLE "organization" ADD CONSTRAINT "org_to_paymentinfo_fk" FOREIGN KEY ("defaultpayment_tkey")
	REFERENCES "paymentinfo" ("tkey");
ALTER TABLE "organization" ADD CONSTRAINT "cust_to_supplier_fk" FOREIGN KEY ("supplierkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "organizationtopaymenttype" ADD CONSTRAINT "organizationtopaymenttype_paymenttype_fk" FOREIGN KEY ("paymenttype_tkey")
	REFERENCES "paymenttype" ("tkey");	
ALTER TABLE "organizationtopaymenttype" ADD CONSTRAINT "organizationtopaymenttype_organizationrole_fk" FOREIGN KEY ("organizationrole_tkey")
	REFERENCES "organizationrole" ("tkey");
ALTER TABLE "organizationtopaymenttype" ADD CONSTRAINT "organizationtopaymenttype_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "organizationreference" ADD CONSTRAINT "organizationreference_to_supplier_fk" FOREIGN KEY ("supplierkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "organizationreference" ADD CONSTRAINT "organizationreference_to_tp_fk" FOREIGN KEY ("technologyproviderkey")
	REFERENCES "organization" ("tkey");	

ALTER TABLE "organizationsetting" ADD CONSTRAINT "setting_to_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
	
ALTER TABLE "organizationtorole" ADD CONSTRAINT "orgtorole_org_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");	
ALTER TABLE "organizationtorole" ADD CONSTRAINT "orgtorole_role_fk" FOREIGN KEY ("organizationrole_tkey")
	REFERENCES "organizationrole" ("tkey");	
	
ALTER TABLE "parameter" ADD CONSTRAINT "param_to_def_fk" FOREIGN KEY ("parameterdefinitionkey")
	REFERENCES "parameterdefinition" ("tkey");
ALTER TABLE "parameter" ADD CONSTRAINT "param_to_paramset_fk" FOREIGN KEY ("parametersetkey")
	REFERENCES "parameterset" ("tkey");	
	
ALTER TABLE "parameterdefinition" ADD CONSTRAINT "tech_prod_to_param_def_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");
	
ALTER TABLE "parameteroption" ADD CONSTRAINT "parameteroption_to_parameterdef_fk" FOREIGN KEY ("parameterdefinition_tkey")
	REFERENCES "parameterdefinition" ("tkey");	
	
ALTER TABLE "paymentinfo" ADD CONSTRAINT "paymentinfo_paymenttype_fk" FOREIGN KEY ("paymenttype_tkey")
	REFERENCES "paymenttype" ("tkey");	
	
ALTER TABLE "paymentresult" ADD CONSTRAINT "paymentresult_to_billingresult_fk" FOREIGN KEY ("billingresult_tkey")
	REFERENCES "billingresult" ("tkey");	
	
ALTER TABLE "platformuser" ADD CONSTRAINT "platformuser_to_org_fk" FOREIGN KEY ("organizationkey")
	REFERENCES "organization" ("tkey");	

ALTER TABLE "pricedevent" ADD CONSTRAINT "pricedevent_to_event_fk" FOREIGN KEY ("eventkey")
	REFERENCES "event" ("tkey");
ALTER TABLE "pricedevent" ADD CONSTRAINT "pricedevent_to_pricemodel_fk" FOREIGN KEY ("pricemodelkey")
	REFERENCES "pricemodel" ("tkey");
	
ALTER TABLE "pricedoption" ADD CONSTRAINT "pricedoption_to_parameteroption_fk" FOREIGN KEY ("parameteroptionkey")
	REFERENCES "parameteroption" ("tkey");
ALTER TABLE "pricedoption" ADD CONSTRAINT "pricedoption_to_pricedparameter_fk" FOREIGN KEY ("pricedparameter_tkey")
	REFERENCES "pricedparameter" ("tkey");	
	
ALTER TABLE "pricedparameter" ADD CONSTRAINT "parameter_to_pricedparameter_fk" FOREIGN KEY ("parameter_tkey")
	REFERENCES "parameter" ("tkey");
ALTER TABLE "pricedparameter" ADD CONSTRAINT "pricemodel_to_pricedparam_fk" FOREIGN KEY ("pricemodelkey")
	REFERENCES "pricemodel" ("tkey");
	
ALTER TABLE "pricedproductrole" ADD CONSTRAINT "pricedproductrole_pricemodel_fk" FOREIGN KEY ("pricemodel_tkey")
	REFERENCES "pricemodel" ("tkey");	
ALTER TABLE "pricedproductrole" ADD CONSTRAINT "pricedproductrole_pricedparameter_fk" FOREIGN KEY ("pricedparameter_tkey")
	REFERENCES "pricedparameter" ("tkey");
ALTER TABLE "pricedproductrole" ADD CONSTRAINT "pricedproductrole_pricedoption_fk" FOREIGN KEY ("pricedoption_tkey")
	REFERENCES "pricedoption" ("tkey");	
ALTER TABLE "pricedproductrole" ADD CONSTRAINT "pricedproductrole_roledefinition_fk" FOREIGN KEY ("roledefinition_tkey")
	REFERENCES "roledefinition" ("tkey");

ALTER TABLE "pricemodel" ADD CONSTRAINT "pricemodel_to_currency" FOREIGN KEY ("currency_tkey")
	REFERENCES "supportedcurrency" ("tkey");	

ALTER TABLE "product" ADD CONSTRAINT "prod_tarcust_fk" FOREIGN KEY ("targetcustomer_tkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "product" ADD CONSTRAINT "prod_template_fk" FOREIGN KEY ("template_tkey")
	REFERENCES "product" ("tkey");	
ALTER TABLE "product" ADD CONSTRAINT "prod_techprod_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");	
ALTER TABLE "product" ADD CONSTRAINT "prod_pricemodel_fk" FOREIGN KEY ("pricemodel_tkey")
	REFERENCES "pricemodel" ("tkey");
ALTER TABLE "product" ADD CONSTRAINT "prod_paramset_fk" FOREIGN KEY ("parameterset_tkey")
	REFERENCES "parameterset" ("tkey");
ALTER TABLE "product" ADD CONSTRAINT "prod_to_org_fk" FOREIGN KEY ("supplierkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "productreference" ADD CONSTRAINT "productreference_tgtprod_fk" FOREIGN KEY ("targetproduct_tkey")
	REFERENCES "product" ("tkey");
ALTER TABLE "productreference" ADD CONSTRAINT "productreference_srcprod_fk" FOREIGN KEY ("sourceproduct_tkey")
	REFERENCES "product" ("tkey");	
	
ALTER TABLE "report" ADD CONSTRAINT "report_to_orgrole_fk" FOREIGN KEY ("organizationrole_tkey")
	REFERENCES "organizationrole" ("tkey");	
	
ALTER TABLE "roledefinition" ADD CONSTRAINT "roledefinition_technicalproduct_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");	
	
ALTER TABLE "shop" ADD CONSTRAINT "shop_to_org_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
	
ALTER TABLE "steppedprice" ADD CONSTRAINT "steppedprice_pricedevent_fk" FOREIGN KEY ("pricedevent_tkey")
	REFERENCES "pricedevent" ("tkey");
ALTER TABLE "steppedprice" ADD CONSTRAINT "steppedprice_pricedparameter_fk" FOREIGN KEY ("pricedparameter_tkey")
	REFERENCES "pricedparameter" ("tkey");
ALTER TABLE "steppedprice" ADD CONSTRAINT "steppedprice_pricemodel_fk" FOREIGN KEY ("pricemodel_tkey")
	REFERENCES "pricemodel" ("tkey");	
	
ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_product_fk" FOREIGN KEY ("product_tkey")
	REFERENCES "product" ("tkey");	
ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_org_fk" FOREIGN KEY ("organizationkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "technicalproduct" ADD CONSTRAINT "tech_prod_to_org_fk" FOREIGN KEY ("organizationkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "technicalproductoperation" ADD CONSTRAINT "technicalproductoperation_technicalproduct_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");	

ALTER TABLE "triggerdefinition" ADD CONSTRAINT "triggerdefinition_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");	
	
ALTER TABLE "triggerprocess" ADD CONSTRAINT "triggerprocess_user_fk" FOREIGN KEY ("user_tkey")
	REFERENCES "platformuser" ("tkey");	
ALTER TABLE "triggerprocess" ADD CONSTRAINT "triggerprocess_triggerdefinition_fk" FOREIGN KEY ("triggerdefinition_tkey")
	REFERENCES "triggerdefinition" ("tkey");
	
ALTER TABLE "triggerprocessparameter" ADD CONSTRAINT "triggerprocessmodification_triggerprocess_fk" FOREIGN KEY ("triggerprocess_tkey")
	REFERENCES "triggerprocess" ("tkey");	
	
ALTER TABLE "usagelicense" ADD CONSTRAINT "usagelicense_to_sub_fk" FOREIGN KEY ("subscription_tkey")
	REFERENCES "subscription" ("tkey");	
ALTER TABLE "usagelicense" ADD CONSTRAINT "usagelicense_roledefinition_fk" FOREIGN KEY ("roledefinition_tkey")
	REFERENCES "roledefinition" ("tkey");
ALTER TABLE "usagelicense" ADD CONSTRAINT "usagelicense_to_puser_fk" FOREIGN KEY ("user_tkey")
	REFERENCES "platformuser" ("tkey");	

---------------------
-- initial data
---------------------

INSERT INTO "organizationrole" ("tkey", "version", "rolename") VALUES (1, 0, 'SUPPLIER');
INSERT INTO "organizationrole" ("tkey", "version", "rolename") VALUES (2, 0, 'TECHNOLOGY_PROVIDER');
INSERT INTO "organizationrole" ("tkey", "version", "rolename") VALUES (3, 0, 'CUSTOMER');
INSERT INTO "organizationrole" ("tkey", "version", "rolename") VALUES (4, 0, 'PLATFORM_OPERATOR');

INSERT INTO "event" ("tkey", "version", "eventidentifier", "eventtype") VALUES (1000, 0, 'USER_LOGIN_TO_SERVICE', 'PLATFORM_EVENT');
INSERT INTO "event" ("tkey", "version", "eventidentifier", "eventtype") VALUES (1001, 0, 'USER_LOGOUT_FROM_SERVICE', 'PLATFORM_EVENT');
  
INSERT INTO "eventhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "eventidentifier", "eventtype") VALUES (1000, CURRENT_TIMESTAMP, 'ADD', 'ANONYMOUS', 1000, 0, 'USER_LOGIN_TO_SERVICE', 'PLATFORM_EVENT');
INSERT INTO "eventhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "eventidentifier", "eventtype") VALUES (1001, CURRENT_TIMESTAMP, 'ADD', 'ANONYMOUS', 1001, 0, 'USER_LOGOUT_FROM_SERVICE', 'PLATFORM_EVENT');

INSERT INTO "parameterdefinition" ("tkey", "version", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1000, 0, 'CONCURRENT_USER', 'PLATFORM_PARAMETER', 'LONG', true, false, 1);
INSERT INTO "parameterdefinition" ("tkey", "version", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1001, 0, 'NAMED_USER', 'PLATFORM_PARAMETER', 'LONG', true, false, 1);
INSERT INTO "parameterdefinition" ("tkey", "version", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1002, 0, 'PERIOD', 'PLATFORM_PARAMETER', 'DURATION', true, false, 0);

INSERT INTO "paymenttype" ("tkey", "version", "paymenttypeid", "collectiontype", "psppaymenttypeid") VALUES (1, 0, 'CREDIT_CARD', 'PAYMENT_SERVICE_PROVIDER', 'CC');
INSERT INTO "paymenttype" ("tkey", "version", "paymenttypeid", "collectiontype", "psppaymenttypeid") VALUES (2, 0, 'DIRECT_DEBIT', 'PAYMENT_SERVICE_PROVIDER', 'DD');
INSERT INTO "paymenttype" ("tkey", "version", "paymenttypeid", "collectiontype") VALUES (3, 0, 'INVOICE', 'ORGANIZATION');
  
INSERT INTO "parameterdefinitionhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1000, CURRENT_TIMESTAMP, 'ADD', 'ANONYMOUS', 1000, 0, 'CONCURRENT_USER', 'PLATFORM_PARAMETER', 'LONG', false, false, 1);
INSERT INTO "parameterdefinitionhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1001, CURRENT_TIMESTAMP, 'ADD', 'ANONYMOUS', 1001, 0, 'NAMED_USER', 'PLATFORM_PARAMETER', 'LONG', false, false, 1);
INSERT INTO "parameterdefinitionhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "parameterid", "parametertype", "valuetype", "configurable", "mandatory", "minimumvalue") VALUES (1002, CURRENT_TIMESTAMP, 'ADD', 'ANONYMOUS', 1002, 0, 'PERIOD', 'PLATFORM_PARAMETER', 'DURATION', false, false, 0);

INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 0, 0, 'Subscription', 3);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 1, 0, 'Event', 3);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 2, 0, 'Supplier_Product', 1);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 3, 0, 'Supplier_Customer', 1);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 4, 0, 'Provider_Event', 2);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 5, 0, 'Provider_Supplier', 2);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 6, 0, 'Provider_Subscription', 2);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 7, 0, 'Provider_Instance', 2);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 8, 0, 'Supplier_Billing', 1);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 9, 0, 'Supplier_PaymentResultStatus', 1);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 10, 0, 'Supplier_BillingDetails', 1);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 11, 0, 'Customer_BillingDetails', 3);
INSERT INTO "report" ("tkey", "version", "reportname", "organizationrole_tkey") VALUES ( 12, 0, 'Customer_CurrentBillingDetails', 3);

INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 0, 'REPORT_DESC', 'Abo-Bericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 0, 'REPORT_DESC', 'Subscription report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 0, 'REPORT_DESC', 'サブスクリプションレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1, 'REPORT_DESC', 'Ereignisbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1, 'REPORT_DESC', 'Event report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1, 'REPORT_DESC', 'イベントレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 2, 'REPORT_DESC', 'Servicebericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 2, 'REPORT_DESC', 'Service report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 2, 'REPORT_DESC', 'サービスレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 3, 'REPORT_DESC', 'Kundenbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 3, 'REPORT_DESC', 'Customer report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 3, 'REPORT_DESC', 'カスタマーレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 4, 'REPORT_DESC', 'Ereignisbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 4, 'REPORT_DESC', 'Event report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 4, 'REPORT_DESC', 'イベントレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 5, 'REPORT_DESC', 'Serviceanbieter-Bericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 5, 'REPORT_DESC', 'Supplier report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 5, 'REPORT_DESC', 'サプライヤーレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 6, 'REPORT_DESC', 'Abo-Bericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 6, 'REPORT_DESC', 'Subscription report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 6, 'REPORT_DESC', 'サブスクリプションレポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 7, 'REPORT_DESC', 'Instanzenbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 7, 'REPORT_DESC', 'Instance report');  
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 7, 'REPORT_DESC', 'インスタンスレポート');  
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 8, 'REPORT_DESC', 'Rechnungsbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 8, 'REPORT_DESC', 'Billing report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 8, 'REPORT_DESC', '請求レポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 9, 'REPORT_DESC', 'Abrechnungsstatusbericht');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 9, 'REPORT_DESC', 'Payment processing status report');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 9, 'REPORT_DESC', '支払処理状態レポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 10, 'REPORT_DESC', 'Detaillierter Rechnungsbericht für eine existierende Rechnung eines Kunden');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 10, 'REPORT_DESC', 'Detailed billing report for an existing invoice of a customer');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 10, 'REPORT_DESC', 'カスタマーの既存の請求書の明細レポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 11, 'REPORT_DESC', 'Detaillierter Rechnungsbericht für eine existierende Rechnung');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 11, 'REPORT_DESC', 'Detailed billing report for an existing invoice');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 11, 'REPORT_DESC', '既存の請求書の明細レポート');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 12, 'REPORT_DESC', 'Kostenvorschau für den aktuellen Monat bis zum aktuellen Zeitpunkt');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 12, 'REPORT_DESC', 'Payment preview for the current month until now');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 12, 'REPORT_DESC', '月初から現在までの支払いのプレビュー');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1000, 'PARAMETER_DEF_DESC', 'Maximum number of concurrent users for a subscription.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1000, 'PARAMETER_DEF_DESC', 'Maximale Anzahl gleichzeitiger Benutzer eines Abonnements.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1000, 'PARAMETER_DEF_DESC', 'サブスクリプションの最大同時利用ユーザー数');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1001, 'PARAMETER_DEF_DESC', 'Maximum number of registered users for a subscription.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1001, 'PARAMETER_DEF_DESC', 'Maximale Anzahl der Benutzer, die einem Abonnement zugeordnet werden können.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1001, 'PARAMETER_DEF_DESC', 'サブスクリプションの最大登録ユーザー数');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1002, 'PARAMETER_DEF_DESC', 'Number of days after which the subscription will be deactivated, e.g. 2.25.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1002, 'PARAMETER_DEF_DESC', 'Anzahl von Tagen, nach deren Ablauf ein Abonnement deaktiviert wird, z.B. 2,25.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1002, 'PARAMETER_DEF_DESC', 'サブスクリプションが無効化されるまでの日数(例: 2.25)');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1000, 'EVENT_DESC', 'Login of a user to the service.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1000, 'EVENT_DESC', 'Anmeldung eines Benutzers bei dem Service.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1000, 'EVENT_DESC', 'サービスへのユーザーのログイン');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('en', 1001, 'EVENT_DESC', 'Logout of a user from the service.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('de', 1001, 'EVENT_DESC', 'Abmeldung eines Benutzers von dem Service.');
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") VALUES ('ja', 1001, 'EVENT_DESC', 'サービスからのユーザーのログアウト');

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('BillingContact', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('BillingContactHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('BillingResult', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ConfigurationSetting', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Discount', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('DiscountHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Event', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('EventHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('GatheredEvent', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ImageResource', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('LocalizedResource', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Organization', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationReference', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationReferenceHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationRole', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationSetting', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationToPaymentType', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationToRole', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('OrganizationToRoleHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Parameter', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterDefinition', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterDefinitionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterOption', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterOptionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterSet', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ParameterSetHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentInfo', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentInfoHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentResult', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentResultHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentType', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PlatformUser', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PlatformUserHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedEvent', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedEventHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedOption', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedOptionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedParameter', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedParameterHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedProductRole', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PricedProductRoleHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PriceModel', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PriceModelHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Product', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductReference', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductReferenceHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Report', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('RoleDefinition', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('RoleDefinitionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Session', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Shop', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ShopHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('SteppedPrice', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('SteppedPriceHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Subscription', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('SubscriptionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('SupportedCurrency', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TechnicalProduct', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TechnicalProductHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TechnicalProductOperation', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TechnicalProductOperationHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TimerProcessing', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerDefinition', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerDefinitionHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerProcess', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerProcessHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerProcessParameter', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UsageLicense', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('UsageLicenseHistory', 10);

INSERT INTO "organization" ("tkey", "version", "locale", "organizationid", "registrationdate") VALUES (1, 0, 'en', 'PLATFORM_OPERATOR', date_part('epoch', now())*1000);
INSERT INTO "organizationhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "locale", "organizationid", "registrationdate") VALUES (1, now(), 'ADD', 'ANONYMOUS', 1, 0, 'en', 'PLATFORM_OPERATOR', date_part('epoch', now())*1000);

INSERT INTO "organizationtorole" ("tkey", "version", "organizationrole_tkey", "organization_tkey" ) VALUES (1, 0, 4, 1);
INSERT INTO "organizationtorolehistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationroletkey", "organizationtkey" ) VALUES (1, now(), 'ADD', 'ANONYMOUS', 1, 0, 4, 1);

---------------------------------------------------
-- user 1000 (administrator) with password admin123
---------------------------------------------------


