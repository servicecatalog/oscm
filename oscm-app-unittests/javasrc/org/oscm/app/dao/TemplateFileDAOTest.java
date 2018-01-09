/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 10, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.domain.TemplateFile;

public class TemplateFileDAOTest {

    private static TemplateFileDAO tfDAO;
    private static TypedQuery<TemplateFile> query;
    private static TemplateFile tf;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        tfDAO = spy(new TemplateFileDAO());
        EntityManager em = mock(EntityManager.class);
        tfDAO.em = em;
        query = mock(TypedQuery.class);
        doReturn(query).when(em).createNamedQuery(anyString(),
                eq(TemplateFile.class));

        tf = new TemplateFile(0L, "file", "content".getBytes(), "controller");
    }

    @Test
    public void testGetTemplateFile() throws Exception {
        when(query.getSingleResult()).thenReturn(tf);

        TemplateFile result = tfDAO.getTemplateFileByUnique("file",
                "controller");

        assertEquals(tf, result);
    }

    @Test
    public void testGetTemplateFile_NoResult() throws Exception {
        when(query.getSingleResult()).thenThrow(new NoResultException());

        TemplateFile result = tfDAO.getTemplateFileByUnique("file",
                "controller");

        assertNull(result);
    }

    @Test
    public void testGetTemplateFiles() throws Exception {
        when(query.getResultList()).thenReturn(Arrays.asList(tf));

        List<TemplateFile> result = tfDAO
                .getTemplateFilesByControllerId("controller");

        assertEquals(tf, result.get(0));
    }

    @Test
    public void testGetTemplateFiles_NoResult() throws Exception {
        when(query.getResultList()).thenThrow(new NoResultException());

        List<TemplateFile> result = tfDAO
                .getTemplateFilesByControllerId("controller");

        assertEquals(0, result.size());
    }

    @Test
    public void testSaveTemplateFiles_insert() throws Exception {

        tfDAO.saveTemplateFile(tf);

        verify(tfDAO.em).persist(tf);
    }

    @Test
    public void testSaveTemplateFiles_update() throws Exception {
        when(query.getSingleResult()).thenReturn(tf);

        tfDAO.saveTemplateFile(tf);

        verify(tfDAO.em).merge(tf);
    }

    @Test
    public void testDeleteTemplateFiles() throws Exception {
        when(query.getSingleResult()).thenReturn(tf);

        tfDAO.deleteTemplateFile(tf);

        verify(tfDAO.em).remove(tf);
    }
}
