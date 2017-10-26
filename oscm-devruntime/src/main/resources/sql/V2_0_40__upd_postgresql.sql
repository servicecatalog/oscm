----------------------------------------------------------------------------
-- relation between marketplace and supplier + history
----------------------------------------------------------------------------
CREATE TABLE "marketplacetoorganization" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"marketplace_tkey" BIGINT NOT NULL,
		"organization_tkey" BIGINT NOT NULL
	);	
	
CREATE TABLE "marketplacetoorganizationhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"marketplaceobjkey" BIGINT NOT NULL,
		"organizationobjkey" BIGINT NOT NULL
	);	

----------------------------------------------------------------------------
-- all existing suppliers are assigned to fujitsu marketplace and suppliers
-- are assigned to their own local marketplace
----------------------------------------------------------------------------

-- table to generate the keys for the real table
CREATE TABLE "rel" (
		"tkey" SERIAL,
		"mpkey" BIGINT NOT NULL,
		"orgkey" BIGINT NOT NULL
	);
	
-- table to generate the keys for the real history table
CREATE TABLE "relhist" (
		"tkey" SERIAL,
		"relkey" BIGINT NOT NULL,
		"mpkey" BIGINT NOT NULL,
		"orgkey" BIGINT NOT NULL
	);

-- relation to local MP
INSERT INTO "rel" ("mpkey", "orgkey") SELECT "localmarketplace_tkey", "tkey" FROM "organization" AS "org" 
WHERE "org"."localmarketplace_tkey" IS NOT NULL ORDER BY "localmarketplace_tkey" ASC, "tkey" ASC;

-- history
INSERT INTO "relhist" ("relkey", "mpkey", "orgkey") SELECT "tkey", "mpkey", "orgkey" FROM "rel";

-- fill the real tables
INSERT INTO "marketplacetoorganization" ("tkey", "version", "marketplace_tkey", "organization_tkey") 
	SELECT "tkey", 1, "mpkey", "orgkey" FROM "rel";

INSERT INTO "marketplacetoorganizationhistory" ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "marketplaceobjkey", "organizationobjkey") 
	SELECT "tkey", now(), 'ADD', 'ANONYMOUS', "relkey", 1, "mpkey", "orgkey" FROM "relhist";

-- delete temporary tables
DROP TABLE "rel";
DROP TABLE "relhist";
	
-- add constraints
ALTER TABLE "marketplacetoorganization" ADD CONSTRAINT "marketplacetoorganization_pk" PRIMARY KEY ("tkey");
ALTER TABLE "marketplacetoorganizationhistory" ADD CONSTRAINT "marketplacetoorganizationhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "marketplacetoorganization" ADD CONSTRAINT mptoorg_mpkey_orgkey_uc UNIQUE ("marketplace_tkey", "organization_tkey");
ALTER TABLE "marketplacetoorganization" ADD CONSTRAINT "mptoorg_marketplace_fk" FOREIGN KEY ("marketplace_tkey")
	REFERENCES "marketplace" ("tkey");
ALTER TABLE "marketplacetoorganization" ADD CONSTRAINT "mptoorg_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");

----------------------------------------------------------------------------
-- update hibernate sequences
----------------------------------------------------------------------------
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'MarketplaceToOrganization', COALESCE((MAX(tkey)/1000),0)+10 FROM marketplacetoorganization;
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'MarketplaceToOrganizationHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM marketplacetoorganizationhistory;
