/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 3, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.data;

import javax.ws.rs.WebApplicationException;

import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.rest.common.CommonParams;
import org.oscm.rest.common.Representation;
import org.oscm.rest.common.WebException;
import org.oscm.rest.trigger.config.TriggerCommonParams;
import org.oscm.validator.ADMValidator;

/**
 * Representation class of trigger definitions.
 * 
 * @author miethaner
 */
public class DefinitionRepresentation extends Representation {

    private String description;
    private Boolean suspend;
    private String target_type;
    private String target_url;
    private Owner owner;
    private String action;
    private Links links;

    public DefinitionRepresentation() {
    }

    public static class Owner {
        private Long id;
        private String description;

        public Owner() {
        }

        public Owner(Long id, String description) {
            this.id = id;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return description;
        }

        public void setName(String description) {
            this.description = description;
        }
    }

    public static class Links {
        private Long owner_id;

        public Links() {
        }

        public Links(Long owner_id) {
            this.owner_id = owner_id;
        }

        public Long getOwner_id() {
            return owner_id;
        }

        public void setOwner_id(Long owner_id) {
            this.owner_id = owner_id;
        }
    }

    public DefinitionRepresentation(VOTriggerDefinition definition) {
        super(definition);

        if (definition == null) {
            return;
        }

        setETag((long) definition.getVersion());
        this.description = definition.getName();

        if (definition.getTargetType() != null) {
            this.target_type = definition.getTargetType().toString();
        }

        this.target_url = definition.getTarget();

        if (definition.getType() != null) {
            this.action = definition.getType().toString();
        }

        this.suspend = definition.isSuspendProcess();

        if (definition.getOrganization() != null) {
            this.owner = new Owner(definition.getOrganization()
                    .getKey(), definition.getOrganization().getName());
            this.links = new Links(definition.getOrganization()
                    .getKey());
        }
    }

    public Long getOwnerId() {
        if (links != null) {
            return links.getOwner_id();
        } else {
            return null;
        }
    }

    public void setOwnerId(Long owner_id) {
        if (links == null) {
            links = new Links();
        }

        links.setOwner_id(owner_id);
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return target_type;
    }

    public void setType(String target_type) {
        this.target_type = target_type;
    }

    public String getTargetURL() {
        return target_url;
    }

    public void setTargetURL(String target_url) {
        this.target_url = target_url;
    }

    public Boolean isSuspending() {
        return suspend;
    }

    public void setSuspending(Boolean suspend) {
        this.suspend = suspend;
    }

    @Override
    public void validateContent() throws WebApplicationException {

        if (description == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_DESCRIPTION)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();
        } else if (!description.matches(CommonParams.PATTERN_STRING)) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_DESCRIPTION)
                    .message(CommonParams.ERROR_BAD_PROPERTY).build();
        }

        if (suspend == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_SUSPEND)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();
        }

        if (target_type == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_TARGET_TYPE)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();
        } else {
            try {
                TriggerTargetType.valueOf(target_type);
            } catch (IllegalArgumentException e) {
                throw WebException.badRequest()
                        .property(TriggerCommonParams.PROPERTY_TARGET_TYPE)
                        .message(CommonParams.ERROR_BAD_PROPERTY).build();
            }
        }

        if (target_url == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_TARGET_URL)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();
        } else if (!ADMValidator.isUrl(target_url)) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_TARGET_URL)
                    .message(CommonParams.ERROR_BAD_PROPERTY).build();
        }

        if (action == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_ACTION)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();
        } else {
            try {
                ActionRepresentation.Action.valueOf(action);
            } catch (IllegalArgumentException e) {
                throw WebException.badRequest()
                        .property(TriggerCommonParams.PROPERTY_ACTION)
                        .message(CommonParams.ERROR_BAD_PROPERTY).build();
            }
        }
    }
}
