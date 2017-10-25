/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 3, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.triggerservice.data.TriggerProcessRepresentation.ServiceRepresentation.ParameterRepresentation;

/**
 * Representation class of trigger processes.
 * 
 * @author miethaner
 */
public class TriggerProcessRepresentation {

    public static class Author {
        private Long id;
        private String email;

        public Author() {
        }

        public Author(Long id, String email) {
            this.id = id;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class SubscriptionRepresentation {
        private Long id;
        private String name;

        public SubscriptionRepresentation() {
        }

        public SubscriptionRepresentation(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ServiceRepresentation {

        public static class ParameterRepresentation {
            private String name;
            private String value;

            public ParameterRepresentation() {
            }

            public ParameterRepresentation(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        private String name;
        private ParameterRepresentation[] parameters;

        public ServiceRepresentation() {
        }

        public ServiceRepresentation(String name,
                ParameterRepresentation[] parameters) {
            super();
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ParameterRepresentation[] getParameters() {
            return parameters;
        }

        public void setParameters(ParameterRepresentation[] parameters) {
            this.parameters = parameters;
        }
    }

    public static class Links {
        private Long trigger_id;
        private Long author_id;
        private Long subscription_id;

        public Links() {
        }

        public Links(Long trigger_id, Long author_id, Long subscription_id) {
            this.trigger_id = trigger_id;
            this.author_id = author_id;
            this.subscription_id = subscription_id;
        }

        public Long getTrigger_id() {
            return trigger_id;
        }

        public void setTrigger_id(Long definition_id) {
            this.trigger_id = definition_id;
        }

        public Long getAuthor_id() {
            return author_id;
        }

        public void setAuthor_id(Long author_id) {
            this.author_id = author_id;
        }

        public Long getSubscription_id() {
            return subscription_id;
        }

        public void setSubscription_id(Long subscription_id) {
            this.subscription_id = subscription_id;
        }
    }

    private Long id;
    private Date activation_time;
    private String status;
    private Author author;
    private SubscriptionRepresentation subscription;
    private ServiceRepresentation service;
    private Links links;

    public TriggerProcessRepresentation() {
    }

    public TriggerProcessRepresentation(Long id, Date activation_time,
            String status, Author author,
            SubscriptionRepresentation subscription,
            ServiceRepresentation service, Links links) {
        this.id = id;
        this.activation_time = activation_time;
        this.status = status;
        this.author = author;
        this.subscription = subscription;
        this.service = service;
        this.links = links;
    }

    public TriggerProcessRepresentation(TriggerProcess process,
            Subscription subscription, Product product) {
        this.id = new Long(process.getKey());
        this.activation_time = new Date(process.getActivationDate());
        this.status = process.getStatus().toString();
        this.author = new Author(new Long(process.getUser().getKey()), process
                .getUser().getEmail());

        this.links = new Links(
                new Long(process.getTriggerDefinition().getKey()), new Long(
                        process.getUser().getKey()), null);

        if (subscription != null) {
            this.subscription = new SubscriptionRepresentation(new Long(
                    subscription.getKey()), subscription.getSubscriptionId());
            this.links.setSubscription_id(new Long(subscription.getKey()));
        }

        if (product != null) {
            List<ParameterRepresentation> prodParams = new ArrayList<ParameterRepresentation>();

            for (Parameter voParam : product.getParameterSet().getParameters()) {
                prodParams.add(new ParameterRepresentation(voParam
                        .getParameterDefinition().getParameterId(), voParam
                        .getValue()));
            }

            this.service = new ServiceRepresentation(
                    product.getCleanProductId(),
                    prodParams.toArray(new ParameterRepresentation[] {}));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusRest() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getActivationTime() {
        return activation_time;
    }

    public void setActivitionTime(Date activition_time) {
        this.activation_time = activition_time;
    }

    public Long getTriggerId() {
        if (links != null) {
            return links.getTrigger_id();
        } else {
            return null;
        }
    }

    public void setTriggerId(Long definition_id) {
        if (links == null) {
            links = new Links();
        }

        links.setTrigger_id(definition_id);
    }

    public Long getAuthorId() {
        if (links != null) {
            return links.getAuthor_id();
        } else {
            return null;
        }
    }

    public void setAuthorId(Long author_id) {
        if (links == null) {
            links = new Links();
        }

        links.setAuthor_id(author_id);
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Long getSubscriptionId() {
        if (links != null) {
            return links.getSubscription_id();
        } else {
            return null;
        }
    }

    public void setSubscriptionId(Long subscription_id) {
        if (links == null) {
            links = new Links();
        }

        links.setSubscription_id(subscription_id);
    }

    public SubscriptionRepresentation getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionRepresentation subscription) {
        this.subscription = subscription;
    }

    public ServiceRepresentation getService() {
        return service;
    }

    public void setService(ServiceRepresentation service) {
        this.service = service;
    }
}
