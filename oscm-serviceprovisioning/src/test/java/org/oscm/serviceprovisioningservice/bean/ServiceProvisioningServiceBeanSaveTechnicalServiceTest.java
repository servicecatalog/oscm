/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-3-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Tag;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * Unit test for saveTechnicalServiceLocalization
 * 
 * @author fangzhongwei
 */
public class ServiceProvisioningServiceBeanSaveTechnicalServiceTest {

    private ServiceProvisioningServiceBean bean;
    private VOTechnicalService technicalService;
    private DataService ds;
    private PlatformUser platformUser;
    private Organization organization;
    private TechnicalProduct technicalProduct;
    private LocalizerServiceLocal localizer;
    private TagServiceLocal tagService;

    private RoleDefinition roleDefinition;
    private Event event;
    private ParameterDefinition paramDefinition;
    private ParameterOption paramOption;
    private TechnicalProductOperation technicalProductOperation;
    private OperationParameter operationParameter;

    @Before
    public void setUp() throws Exception {
        bean = spy(new ServiceProvisioningServiceBean());
        technicalService = new VOTechnicalService();
        ds = mock(DataService.class);
        bean.dm = ds;
        localizer = mock(LocalizerServiceLocal.class);
        bean.localizer = localizer;
        tagService = mock(TagServiceLocal.class);
        bean.tagService = tagService;

        platformUser = new PlatformUser();
        platformUser.setLocale("en");
        doReturn(platformUser).when(ds).getCurrentUser();
        organization = getOrganization();
        platformUser.setOrganization(organization);
        technicalProduct = getTechnicalProduct();
        doReturn(technicalProduct).when(ds).getReference(
                TechnicalProduct.class, technicalService.getKey());
        doReturn(Boolean.TRUE).when(localizer).storeLocalizedResource(
                anyString(), anyLong(), any(LocalizedObjectTypes.class),
                anyString());
        doNothing().when(tagService).updateTags(any(TechnicalProduct.class),
                anyString(), anyListOf(Tag.class));

        prepare_role();
        prepare_event();
        prepare_parameterOperation();
        preapre_technicalServiceOperation();
    }

    @Test
    public void getObjectByBusinessKey_success() throws Exception {
        // given
        doReturn(roleDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(RoleDefinition.class));
        doReturn(technicalProductOperation)
                .when(bean.dm)
                .getReferenceByBusinessKey(isA(TechnicalProductOperation.class));
        doReturn(operationParameter).when(bean.dm).getReferenceByBusinessKey(
                isA(OperationParameter.class));
        doReturn(event).when(bean.dm).getReferenceByBusinessKey(
                isA(Event.class));
        doReturn(paramDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(ParameterDefinition.class));
        doReturn(paramOption).when(bean.dm).getReferenceByBusinessKey(
                isA(ParameterOption.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);

        // then
        verify(ds, times(1)).getReferenceByBusinessKey(
                isA(RoleDefinition.class));
        verify(ds, times(1)).getReferenceByBusinessKey(
                isA(TechnicalProductOperation.class));
        verify(ds, times(1)).getReferenceByBusinessKey(
                isA(OperationParameter.class));
        verify(ds, times(1)).getReferenceByBusinessKey(isA(Event.class));
        verify(ds, times(1)).getReferenceByBusinessKey(
                isA(ParameterDefinition.class));
        verify(ds, times(1)).getReferenceByBusinessKey(
                isA(ParameterOption.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_RoleDefinition_Exception()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.dm)
                .getReferenceByBusinessKey(isA(RoleDefinition.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_TechnicalProductOperation_Exception()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException())
                .when(bean.dm)
                .getReferenceByBusinessKey(isA(TechnicalProductOperation.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_OperationParameter_Exception()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.dm)
                .getReferenceByBusinessKey(isA(OperationParameter.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_Event_Exception() throws Exception {
        // given
        doReturn(roleDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(RoleDefinition.class));
        doReturn(technicalProductOperation)
                .when(bean.dm)
                .getReferenceByBusinessKey(isA(TechnicalProductOperation.class));
        doReturn(operationParameter).when(bean.dm).getReferenceByBusinessKey(
                isA(OperationParameter.class));
        doThrow(new ObjectNotFoundException()).when(bean.dm)
                .getReferenceByBusinessKey(isA(Event.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_ParameterDefinition_Exception()
            throws Exception {
        // given
        doReturn(roleDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(RoleDefinition.class));
        doReturn(technicalProductOperation)
                .when(bean.dm)
                .getReferenceByBusinessKey(isA(TechnicalProductOperation.class));
        doReturn(operationParameter).when(bean.dm).getReferenceByBusinessKey(
                isA(OperationParameter.class));
        doReturn(event).when(bean.dm).getReferenceByBusinessKey(
                isA(Event.class));
        doThrow(new ObjectNotFoundException()).when(bean.dm)
                .getReferenceByBusinessKey(isA(ParameterDefinition.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getObjectByBusinessKey_ParameterOption_Exception()
            throws Exception {
        // given
        doReturn(roleDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(RoleDefinition.class));
        doReturn(technicalProductOperation)
                .when(bean.dm)
                .getReferenceByBusinessKey(isA(TechnicalProductOperation.class));
        doReturn(operationParameter).when(bean.dm).getReferenceByBusinessKey(
                isA(OperationParameter.class));
        doReturn(event).when(bean.dm).getReferenceByBusinessKey(
                isA(Event.class));
        doReturn(paramDefinition).when(bean.dm).getReferenceByBusinessKey(
                isA(ParameterDefinition.class));
        doThrow(new ObjectNotFoundException()).when(bean.dm)
                .getReferenceByBusinessKey(isA(ParameterOption.class));

        // when
        bean.saveTechnicalServiceLocalization(technicalService);
    }

    private Organization getOrganization() {
        Organization organization = new Organization();
        organization.setKey(1000);
        organization.setName("org_tpp");
        organization.setOrganizationId("fc1caca3");
        return organization;
    }

    private TechnicalProduct getTechnicalProduct() {
        TechnicalProduct tp = new TechnicalProduct();
        tp.setKey(10000);
        tp.setOrganization(organization);
        return tp;
    }

    private void prepare_role() {
        VORoleDefinition voRoleDefinition = new VORoleDefinition();
        voRoleDefinition.setKey(10000);
        voRoleDefinition.setRoleId("ADMIN");
        voRoleDefinition.setVersion(0);

        List<VORoleDefinition> roleDefinitions = new ArrayList<VORoleDefinition>();
        roleDefinitions.add(voRoleDefinition);
        technicalService.setRoleDefinitions(roleDefinitions);
        this.roleDefinition = new RoleDefinition();
        roleDefinition.setKey(10000);
    }

    private void prepare_event() {
        VOEventDefinition voEventDefinition = new VOEventDefinition();
        voEventDefinition.setKey(10000);
        voEventDefinition.setEventId("FILE_DOWNLOAD");
        voEventDefinition.setEventType(EventType.SERVICE_EVENT);
        voEventDefinition.setEventDescription("event desription.");

        List<VOEventDefinition> eventDefinitions = new ArrayList<VOEventDefinition>();
        eventDefinitions.add(voEventDefinition);
        technicalService.setEventDefinitions(eventDefinitions);

        event = new Event();
        event.setEventType(EventType.SERVICE_EVENT);
        event.setKey(10000);
    }

    private void prepare_parameterOperation() {
        VOParameterDefinition voParameterDefinition = new VOParameterDefinition();
        voParameterDefinition.setKey(10000);
        voParameterDefinition.setParameterId("MEMORY_STORAGE");
        voParameterDefinition.setParameterType(ParameterType.SERVICE_PARAMETER);
        voParameterDefinition.setValueType(ParameterValueType.ENUMERATION);

        VOParameterOption voParameterOption = new VOParameterOption();
        voParameterOption.setKey(10000);
        voParameterOption.setOptionId("1");
        voParameterOption.setParamDefId("10000");
        voParameterOption.setOptionDescription("Option description");

        List<VOParameterOption> voParameterOptions = new ArrayList<VOParameterOption>();
        voParameterOptions.add(voParameterOption);
        voParameterDefinition.setParameterOptions(voParameterOptions);
        List<VOParameterDefinition> parameterDefinitions = new ArrayList<VOParameterDefinition>();
        parameterDefinitions.add(voParameterDefinition);
        technicalService.setParameterDefinitions(parameterDefinitions);

        this.paramDefinition = new ParameterDefinition();
        paramDefinition.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDefinition.setKey(10000);
        this.paramOption = new ParameterOption();
        paramOption.setKey(10000);
    }

    private void preapre_technicalServiceOperation() {
        VOTechnicalServiceOperation voTechnicalServiceOperation = new VOTechnicalServiceOperation();
        voTechnicalServiceOperation.setKey(10000);
        voTechnicalServiceOperation.setOperationId("HELP");

        VOServiceOperationParameter voServiceOperationParameter = new VOServiceOperationParameter();
        voServiceOperationParameter.setKey(10003);
        voServiceOperationParameter
                .setType(OperationParameterType.INPUT_STRING);
        voServiceOperationParameter.setParameterId("XXX");
        voServiceOperationParameter.setParameterName("XXX_name");

        List<VOServiceOperationParameter> operationParameters = new ArrayList<VOServiceOperationParameter>();
        operationParameters.add(voServiceOperationParameter);
        voTechnicalServiceOperation.setOperationParameters(operationParameters);
        List<VOTechnicalServiceOperation> techinicalServiceOperations = new ArrayList<VOTechnicalServiceOperation>();
        techinicalServiceOperations.add(voTechnicalServiceOperation);
        technicalService
                .setTechnicalServiceOperations(techinicalServiceOperations);

        this.technicalProductOperation = new TechnicalProductOperation();
        technicalProductOperation.setKey(10000);
        this.operationParameter = new OperationParameter();
        operationParameter.setKey(10003);
    }
}
