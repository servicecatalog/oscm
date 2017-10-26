CREATE TABLE billingadapter  (
    tkey bigint NOT NULL,
    billingidentifier character varying(255) NOT NULL,
    name character varying(255),
    connectionproperties text,
    defaultadapter boolean NOT NULL,
    version INTEGER NOT NULL);

ALTER TABLE billingadapter ADD CONSTRAINT billingadapter_pk PRIMARY KEY (tkey);
ALTER TABLE billingadapter ADD CONSTRAINT billingadapter_uniq UNIQUE (billingidentifier);

INSERT INTO billingadapter (tkey, billingidentifier, name, defaultadapter, version) VALUES (1, 'NATIVE_BILLING', 'Native CT-MG Billing', TRUE, 0);
insert into "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") select 'BillingAdapter', COALESCE((MAX(tkey)/1000),0)+10 from billingadapter;

ALTER TABLE technicalproduct ADD COLUMN "billingidentifier" character varying(255) NOT NULL DEFAULT 'NATIVE_BILLING';
ALTER TABLE technicalproduct ALTER COLUMN "billingidentifier" drop DEFAULT;

ALTER TABLE technicalproducthistory ADD COLUMN "billingidentifier" character varying(255) NOT NULL DEFAULT 'NATIVE_BILLING';
ALTER TABLE technicalproducthistory ALTER COLUMN "billingidentifier" drop DEFAULT;