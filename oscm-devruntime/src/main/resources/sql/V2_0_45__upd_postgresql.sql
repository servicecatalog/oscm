CREATE TABLE "psp" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"identifier" VARCHAR(255) NOT NULL,
        "wsdlurl" VARCHAR(255) NOT NULL,
        "distinguishedname" VARCHAR(4096)
);

CREATE TABLE "psphistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"identifier" VARCHAR(255) NOT NULL,
        "wsdlurl" VARCHAR(255) NOT NULL,
        "distinguishedname" VARCHAR(4096)
);

CREATE TABLE "pspsetting" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"settingkey" VARCHAR(255) NOT NULL,
        "settingvalue" VARCHAR(255) NOT NULL,
        "psp_tkey" BIGINT NOT NULL
);

CREATE TABLE "pspsettinghistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"settingkey" VARCHAR(255) NOT NULL,
        "settingvalue" VARCHAR(255) NOT NULL,
        "pspobjkey" BIGINT NOT NULL
);

CREATE TABLE "pspaccount" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"pspidentifier" VARCHAR(255) NOT NULL,
		"psp_tkey" BIGINT NOT NULL,
		"organization_tkey" BIGINT NOT NULL
);

CREATE TABLE "pspaccounthistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"pspidentifier" VARCHAR(255) NOT NULL,
		"pspobjkey" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
);

CREATE TABLE "paymenttypehistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"pspobjkey" BIGINT NOT NULL,
		"collectiontype" VARCHAR(255) NOT NULL,
		"paymenttypeid" VARCHAR(255) NOT NULL
);

ALTER TABLE "paymenttype" ADD COLUMN "psp_tkey" BIGINT;

--------------------- 
-- data migration
---------------------

--create a pseude psp for now
INSERT INTO "psp" ("tkey", "version", "identifier", "wsdlurl") VALUES (1, 0, 'Invoice', '');
INSERT INTO "psp" ("tkey", "version", "identifier", "wsdlurl") VALUES (2, 0, 'heidelpay', '');
INSERT INTO "psphistory" ("tkey", "objversion", "objkey", "invocationdate", "moddate", "modtype", "moduser", "identifier", "wsdlurl") VALUES (1, 0, 1, now(), now(), 'ADD', '1000', 'Invoice', '');
INSERT INTO "psphistory" ("tkey", "objversion", "objkey", "invocationdate", "moddate", "modtype", "moduser", "identifier", "wsdlurl") VALUES (2, 0, 2, now(), now(), 'ADD', '1000', 'heidelpay', '');
UPDATE "paymenttype" SET "psp_tkey" = 1 where tkey = 3;
UPDATE "paymenttype" SET "psp_tkey" = 2 where tkey = 2;
UPDATE "paymenttype" SET "psp_tkey" = 2 where tkey = 1;

ALTER TABLE "paymenttype" ALTER COLUMN "psp_tkey" SET NOT NULL;
ALTER TABLE "paymenttype" DROP COLUMN "psppaymenttypeid";

INSERT INTO "paymenttypehistory" ("tkey", "objversion", "objkey", "invocationdate", "moddate", "modtype", "moduser", "paymenttypeid", "collectiontype", "pspobjkey") VALUES (1, 0, 1, now(), now(), 'ADD', '1000', 'CREDIT_CARD', 'PAYMENT_SERVICE_PROVIDER', 2);
INSERT INTO "paymenttypehistory" ("tkey", "objversion", "objkey", "invocationdate", "moddate", "modtype", "moduser", "paymenttypeid", "collectiontype", "pspobjkey") VALUES (2, 0, 2, now(), now(), 'ADD', '1000', 'DIRECT_DEBIT', 'PAYMENT_SERVICE_PROVIDER', 2);
INSERT INTO "paymenttypehistory" ("tkey", "objversion", "objkey", "invocationdate", "moddate", "modtype", "moduser", "paymenttypeid", "collectiontype", "pspobjkey") VALUES (3, 0, 3, now(), now(), 'ADD', '1000', 'INVOICE', 'ORGANIZATION', 1);

INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'de', 1, 'PAYMENT_TYPE_NAME', 'Kreditkarte');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'de', 2, 'PAYMENT_TYPE_NAME', 'Bankeinzug');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'de', 3, 'PAYMENT_TYPE_NAME', 'Rechnung');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'en', 1, 'PAYMENT_TYPE_NAME', 'Credit card');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'en', 2, 'PAYMENT_TYPE_NAME', 'Direct debit');
INSERT INTO "localizedresource" ("tkey", "version", "locale", "objectkey", "objecttype", "value") VALUES ((SELECT COALESCE(MAX(tkey), 0) + 1 FROM "localizedresource"), 0, 'en', 3, 'PAYMENT_TYPE_NAME', 'Invoice');

DELETE FROM "configurationsetting" WHERE "information_id" = 'PSP_TRUSTSTORE_FILE_PATH';
DELETE FROM "configurationsetting" WHERE "information_id" = 'PSP_TRUSTSTORE_PWD';

---------------------
-- primary keys
---------------------

ALTER TABLE "psp" ADD CONSTRAINT "psp_pk" PRIMARY KEY ("tkey");
ALTER TABLE "psphistory" ADD CONSTRAINT "psphistory_pk" PRIMARY KEY ("tkey");

ALTER TABLE "pspsetting" ADD CONSTRAINT "pspsetting_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pspsettinghistory" ADD CONSTRAINT "pspsettinghistory_pk" PRIMARY KEY ("tkey");

ALTER TABLE "pspaccount" ADD CONSTRAINT "pspaccount_pk" PRIMARY KEY ("tkey");
ALTER TABLE "pspaccounthistory" ADD CONSTRAINT "pspaccounthistory_pk" PRIMARY KEY ("tkey");


---------------------
-- unique indexes
---------------------

CREATE UNIQUE INDEX "psp_id_uidx" ON "psp" ("identifier");
CREATE UNIQUE INDEX "pspsetting_key_bk" ON "pspsetting" ("psp_tkey", "settingkey");
CREATE UNIQUE INDEX "pspaccount_orgpsp_uidx" ON "pspaccount" ("psp_tkey", "organization_tkey");
CREATE UNIQUE INDEX "orgreftopt_orgref_pt_uidx" ON "organizationreftopaymenttype" ("organizationreference_tkey", "paymenttype_tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "pspsetting" ADD CONSTRAINT "pspsetting_psp_fk" FOREIGN KEY ("psp_tkey") REFERENCES "psp" ("tkey");
ALTER TABLE "pspaccount" ADD CONSTRAINT "pspaccount_psp_fk" FOREIGN KEY ("psp_tkey") REFERENCES "psp" ("tkey");
ALTER TABLE "pspaccount" ADD CONSTRAINT "pspaccount_org_fk" FOREIGN KEY ("organization_tkey") REFERENCES "organization" ("tkey");
ALTER TABLE "paymenttype" ADD CONSTRAINT "paymenttype_psp_fk" FOREIGN KEY ("psp_tkey") REFERENCES "psp" ("tkey");


---------------------
-- hibernate sequence
---------------------

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSP', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSPHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSPSetting', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSPSettingHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSPAccount', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PSPAccountHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('PaymentTypeHistory', 10);