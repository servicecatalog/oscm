/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 31, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.oscm.app.domain.TemplateFile;

/**
 * Data access object for template files.
 * 
 * @author miethaner
 */
@Stateless
@LocalBean
public class TemplateFileDAO {

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    public EntityManager em;

    public TemplateFile getTemplateFileByUnique(String fileName,
            String controllerId) {

        TypedQuery<TemplateFile> query = em.createNamedQuery(
                "TemplateFile.getForFileAndControllerId", TemplateFile.class);
        query.setParameter("fileName", fileName);
        query.setParameter("controllerId", controllerId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<TemplateFile> getTemplateFilesByControllerId(
            String controllerId) {

        TypedQuery<TemplateFile> query = em.createNamedQuery(
                "TemplateFile.getForControllerId", TemplateFile.class);
        query.setParameter("controllerId", controllerId);

        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    public void saveTemplateFile(TemplateFile file) {

        file.setLastChange(new Date());
        TemplateFile dbFile = find(file);

        if (dbFile == null) {
            em.persist(file);
        } else {
            file.setTkey(dbFile.getTkey());
            em.merge(file);
        }
        em.flush();
    }

    public void deleteTemplateFile(TemplateFile file) {
        TemplateFile dbFile = find(file);
        em.remove(dbFile);
        em.flush();
    }

    public TemplateFile find(TemplateFile file) {
        TemplateFile dbFile = em.find(TemplateFile.class,
                Long.valueOf(file.getTkey()));
        if (dbFile == null) {
            dbFile = getTemplateFileByUnique(file.getFileName(),
                    file.getControllerId());
        }
        return dbFile;
    }
}
