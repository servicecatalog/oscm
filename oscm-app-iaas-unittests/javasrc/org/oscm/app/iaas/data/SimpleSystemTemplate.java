/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Jan 28, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * 
 */
public class SimpleSystemTemplate implements VSystemTemplate {

    private String id;
    private String name;

    public SimpleSystemTemplate(String id) {
        this.name = id;
        this.id = id;
    }

    @Override
    public String getVSystemTemplateId() {
        return id;
    }

    @Override
    public String getVSystemTemplateName() {
        return name;
    }

}
