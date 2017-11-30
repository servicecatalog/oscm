/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author yuyin
 * 
 */
public class ManageUdaDefinitionBeanTest {

    private ManageUdaDefinitionBean bean;
    private AccountService delegate = mock(AccountService.class);

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
    private String message;
    private Severity facesSeverity;
    private Object[] passedParams;
    private ManageUdaDefinitionCtrl controller = spy(new ManageUdaDefinitionCtrl(
            delegate));

    @Before
    public void setup() {

        bean = spy(new ManageUdaDefinitionBean() {
            private static final long serialVersionUID = 1L;

            @Override
            protected AccountService getAccountingService() {
                return delegate;
            }

            @Override
            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key,
                    final Object[] params) {
                facesSeverity = severity;
                message = key;
                passedParams = params;
            }
        });
        bean.setController(controller);
    }

    @Test
    public void create_Ok() throws Exception {
        // given
        doNothing().when(controller).createUdaDefinition();
        // when
        String result = bean.create();
        // then
        verify(controller, times(1)).createUdaDefinition();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verifyAddedMessage(FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_SAVED);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void create_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(controller)
                .createUdaDefinition();
        // when
        bean.create();
        // then
        verify(controller, times(1)).createUdaDefinition();
    }

    @Test
    public void update_Ok() throws Exception {
        // given
        doNothing().when(controller).updateUdaDefinition();
        // when
        String result = bean.update();
        // then
        verify(controller, times(1)).updateUdaDefinition();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verifyAddedMessage(FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_SAVED);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void update_ConcurrentModificationException() throws Exception {
        // given
        doThrow(new ConcurrentModificationException()).when(controller)
                .updateUdaDefinition();
        // when
        bean.update();
        // then
        verify(controller, times(1)).updateUdaDefinition();
    }

    @Test
    public void update_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(controller)
                .updateUdaDefinition();
        // when
        String result = bean.update();
        // then
        verify(controller, times(1)).updateUdaDefinition();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertNotNull(bean.onfe);
    }

    @Test
    public void delete_Ok() throws Exception {
        // given
        doNothing().when(controller).deleteUdaDefinition();
        // when
        String result = bean.delete();
        // then
        verify(controller, times(1)).deleteUdaDefinition();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verifyAddedMessage(FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_DELETED);
    }

    @Test(expected = ValidationException.class)
    public void delete_ValidationException() throws Exception {
        // given
        doThrow(new ValidationException()).when(controller)
                .deleteUdaDefinition();
        // when
        bean.delete();
        // then
        verify(controller, times(1)).deleteUdaDefinition();
    }

    @Test
    public void reset() throws Exception {
        // given model
        controller.setModel(new ManageUdaDefinitionPage());

        // when
        bean.reset();
        controller.getModel();

        // then
        verify(controller, times(1)).refreshModel();
    }

    @Test
    public void refreshDeleteSuccessMessage() throws Exception {
        // given model
        controller.setModel(new ManageUdaDefinitionPage());

        // when
        String outcome = bean.refreshDeleteSuccessMessage();
        controller.getModel();

        // then
        verify(controller, times(1)).refreshModel();
        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verifyAddedMessage(FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_DELETED);
    }

    @Test
    public void refreshSaveSuccessMessage() throws Exception {
        // given model
        controller.setModel(new ManageUdaDefinitionPage());

        // when
        String outcome = bean.refreshSaveSuccessMessage();
        controller.getModel();

        // then
        verify(controller, times(1)).refreshModel();
        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verifyAddedMessage(FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_UDADEFINITIONS_SAVED);
    }

    @Test
    public void refreshSaveSuccessMessage_ConcurrentDeletion() throws Exception {
        // given model
        controller.setModel(new ManageUdaDefinitionPage());
        ObjectNotFoundException onfe = new ObjectNotFoundException(
                ClassEnum.UDA_DEFINITION, "uda1");
        bean.onfe = onfe;

        // when
        String outcome = bean.refreshSaveSuccessMessage();
        controller.getModel();

        // then
        verify(controller, times(1)).refreshModel();
        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verifyAddedMessage(FacesMessage.SEVERITY_ERROR, onfe.getMessageKey());
        assertSame(onfe.getMessageParams(), passedParams);
    }

    private void verifyAddedMessage(Severity severity, String msgKey) {
        assertNotNull(facesMessages);
        assertEquals(msgKey, message);
        assertEquals(severity, facesSeverity);
    }
}
