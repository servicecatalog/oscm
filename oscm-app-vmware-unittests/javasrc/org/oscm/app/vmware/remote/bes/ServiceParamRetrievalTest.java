/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 2019-01-29
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.bes;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServiceParamRetrievalTest {

  
    
    final String technicalService = ""
   + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
   +"<!-- Copyright FUJITSU LIMITED 2018-->"
   +"<tns:TechnicalServices xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd ../../oscm-serviceprovisioning/javares/TechnicalServices.xsd\" xmlns:tns=\"oscm.serviceprovisioning/1.9/TechnicalService.xsd\">"
   +" <tns:TechnicalService accessType=\"DIRECT\" allowingOnBehalfActing=\"false\" baseUrl=\"\" billingIdentifier=\"NATIVE_BILLING\" build=\"2017-09-07\" id=\"VMware ESS 6.5 17.4\" loginPath=\"/\" onlyOneSubscriptionPerUser=\"false\" provisioningPassword=\"\" provisioningType=\"ASYNCHRONOUS\" provisioningUrl=\"http://oscm-app:8880/oscm-app/webservices/oscm-app/oscm-app/org.oscm.app.v2_0.service.AsynchronousProvisioningProxy?wsdl\" provisioningUsername=\"\" provisioningVersion=\"1.0\">"
   +"  <AccessInfo locale=\"en\">VMware Access Info</AccessInfo>"
   +"   <LocalizedDescription locale=\"de\">VMware Virtual Machine Provisioning</LocalizedDescription>"
   +"   <LocalizedDescription locale=\"en\">VMware Virtual Machine Provisioning</LocalizedDescription>"
   +"   <LocalizedLicense locale=\"en\">License Agreement</LocalizedLicense>"
   +"   <LocalizedTag locale=\"en\">vmware</LocalizedTag>"
   +"   <ParameterDefinition configurable=\"true\" default=\"${HOST}, ${IP}\" id=\"ACCESS_INFO\" mandatory=\"false\" valueType=\"STRING\">"
   +"     <LocalizedDescription locale=\"en\">Access info pattern. Valid placeholder: ${HOST},${IP}</LocalizedDescription>"
   +"     <LocalizedDescription locale=\"de\"/>"
   +"     <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"true\" default=\"1024\" id=\"AMOUNT_OF_RAM\" mandatory=\"true\" valueType=\"ENUMERATION\">"
   +"      <Options>"
   +"        <Option id=\"1024\">"
   +"          <LocalizedOption locale=\"en\">1 GB</LocalizedOption>"
   +"        </Option>"
   +"        <Option id=\"2048\">"
   +"          <LocalizedOption locale=\"en\">2 GB</LocalizedOption>"
   +"        </Option>"
   +"        <Option id=\"4096\">"
   +"          <LocalizedOption locale=\"en\">4 GB</LocalizedOption>"
   +"        </Option>"
   +"      </Options>"
   +"      <LocalizedDescription locale=\"en\">Amount of RAM</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"false\" default=\"ess.vmware\" id=\"APP_CONTROLLER_ID\" mandatory=\"true\" valueType=\"STRING\">"
   +"      <LocalizedDescription locale=\"en\">The ID of the APP controller implementation.</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"true\" default=\"40\" id=\"DISK_SIZE\" mandatory=\"true\" maxValue=\"160\" minValue=\"40\" valueType=\"INTEGER\">"
   +"      <LocalizedDescription locale=\"en\">System disk size in Gigabyte</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"true\" id=\"DATA_DISK_SIZE_1\" mandatory=\"false\" maxValue=\"140\" minValue=\"20\" valueType=\"INTEGER\">"
   +"      <LocalizedDescription locale=\"en\">Data disk size in Gigabyte</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"true\" default=\"/data\" id=\"DATA_DISK_TARGET_1\" mandatory=\"false\" modificationType=\"ONE_TIME\" valueType=\"STRING\">"
   +"      <LocalizedDescription locale=\"en\">Target mount point</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <ParameterDefinition configurable=\"false\" default=\"/data\" id=\"DATA_DISK_TARGET_VALIDATION_1\" mandatory=\"false\" modificationType=\"ONE_TIME\" valueType=\"STRING\">"
   +"      <LocalizedDescription locale=\"en\">Regex validation pattern for the target mount point</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"de\"/>"
   +"      <LocalizedDescription locale=\"ja\"/>"
   +"    </ParameterDefinition>"
   +"    <Operation actionURL=\"http://oscm-app:8880/oscm-app/webservices/oscm-app/oscm-app/org.oscm.app.v2_0.service.AsynchronousOperationProxy?wsdl\" id=\"RESTART_VM\">"
   +"      <LocalizedName locale=\"de\">Neustart der virtuellen Maschine</LocalizedName>"
   +"      <LocalizedName locale=\"en\">Restart of virtual machine</LocalizedName>"
   +"      <LocalizedDescription locale=\"de\">Die virtuelle Maschine wird runtergefahren und wieder hochgefahren</LocalizedDescription>"
   +"      <LocalizedDescription locale=\"en\">The virtual machine will be shutdown and started again</LocalizedDescription>"
   +"    </Operation>"
   +"  </tns:TechnicalService>"
   +"</tns:TechnicalServices>";

	
	@Test
	public void testDataDiskTargetValidation() throws Exception {

		//given
		String param = "DATA_DISK_TARGET_VALIDATION_1";

		//when
		String result = ServiceParamRetrieval.readServiceParameter(param, technicalService);
		
		//then
		assertEquals("/data", result);
	}
	
	@Test
	public void testAppControllerId() throws Exception {

		//given
		String param = "APP_CONTROLLER_ID";

		//when
		String result = ServiceParamRetrieval.readServiceParameter(param, technicalService);
		
		//then
		assertEquals("ess.vmware", result);
	}
	
	@Test (expected = Exception.class)
	public void testDataDiskTarget() throws Exception {

		//given
		String param = "DATA_DISK_TARGET_1";

		//when
		ServiceParamRetrieval.readServiceParameter(param, technicalService);
		
	}
	


}
