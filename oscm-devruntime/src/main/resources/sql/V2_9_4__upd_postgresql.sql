CREATE TABLE "tenant" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER DEFAULT 0 NOT NULL,
		"tenantid" character varying(255) NOT NULL,
		"name" character varying(255) NOT NULL,
		"description" character varying(255),
		"idp" character varying(255)
	);
ALTER TABLE "tenant" ADD CONSTRAINT "tenant_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "tenantid_uidx" ON "tenant" ("tenantid");

ALTER TABLE "marketplace" ADD COLUMN "tenant_tkey" BIGINT;

ALTER TABLE "marketplace" ADD CONSTRAINT "marketplace_tenant_fk" FOREIGN KEY ("tenant_tkey")
	REFERENCES "tenant" ("tkey");

ALTER TABLE "organization" ADD COLUMN "tenant_tkey" BIGINT;

ALTER TABLE "organization" ADD CONSTRAINT "organization_tenant_fk" FOREIGN KEY ("tenant_tkey")
  REFERENCES "tenant" ("tkey");

CREATE TABLE "tenantsetting"
(
  "tkey" BIGINT NOT NULL,
  "version" INTEGER DEFAULT 0 NOT NULL ,
  "name" character varying(255) NOT NULL,
  "value" character varying(255),
  "tenant_tkey" bigint);

ALTER TABLE "tenantsetting" ADD CONSTRAINT "tenantsetting_pk" PRIMARY KEY ("tkey");

ALTER TABLE "tenantsetting" ADD CONSTRAINT "tenantsetting_tenant_fk" FOREIGN KEY ("tenant_tkey")
    REFERENCES "tenant" ("tkey");

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'Tenant', COALESCE((MAX(tkey)
    /1000),0)+10 FROM "tenant";
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'TenantSetting', COALESCE(
    (MAX(tkey)/1000),0)+10 FROM "tenantsetting";

DROP INDEX platformuser_bk_idx;
