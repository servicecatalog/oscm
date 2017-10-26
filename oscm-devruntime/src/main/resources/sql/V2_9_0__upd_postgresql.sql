CREATE TABLE localizedbillingresource (
    tkey bigint NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    locale character varying(255) NOT NULL,
    datatype character varying(255) NOT NULL,
    resourcetype character varying(255) NOT NULL,
    objectId character varying(64) NOT NULL,
    value bytea NOT NULL );  
    
ALTER TABLE localizedbillingresource ADD CONSTRAINT localizedbillingresource_pk PRIMARY KEY (tkey);

insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'LocalizedBillingResource', COALESCE((MAX(tkey)/1000),0)+10 from localizedbillingresource;

ALTER TABLE pricemodel ADD "external" boolean NOT NULL DEFAULT false;
ALTER TABLE pricemodelhistory ADD "external" boolean NOT NULL DEFAULT false;

ALTER TABLE pricemodel ADD COLUMN "uuid" character varying(64);