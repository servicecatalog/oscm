---------------------
-- Market Place
---------------------

--Schema
ALTER TABLE "shop" DROP CONSTRAINT "shop_pk" CASCADE;
ALTER TABLE "shophistory" DROP CONSTRAINT "shophistory_pk" CASCADE;
ALTER TABLE "shop" DROP CONSTRAINT "shop_to_org_fk" CASCADE;
DROP INDEX  "shop_org_nuidx";

ALTER TABLE "shop" RENAME TO "marketplace";
ALTER TABLE "shophistory" RENAME TO "marketplacehistory";

ALTER TABLE "marketplace" ADD COLUMN "marketplaceid" VARCHAR(255);
ALTER TABLE "marketplace" ADD COLUMN "global" BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE "marketplacehistory" ADD COLUMN "marketplaceid" VARCHAR(255);
ALTER TABLE "marketplacehistory" ADD COLUMN "global" BOOLEAN NOT NULL DEFAULT FALSE;



-- Migrate
CREATE TABLE "marketplace_temp" (
	"tkey" serial,
	"creationdate" BIGINT NOT NULL,
	"organization_tkey" BIGINT NOT NULL
);	

-- Search all suppliers that do not have an shop already
INSERT INTO "marketplace_temp"  (  creationdate, organization_tkey )
	SELECT  date_part('epoch', now())*1000, org.tkey
  	FROM "organization" AS org, "organizationtorole" AS orgtorole, "organizationrole"  AS orgrole 
  	WHERE org.tkey=orgtorole.organization_tkey 
  		AND orgrole.tkey=orgtorole.organizationrole_tkey 
  		AND orgrole.rolename='SUPPLIER' 
  		AND org.tkey NOT IN (
  			SELECT  org.tkey
  			FROM "marketplace" mp, "organization" org
  			WHERE org.tkey=mp.organization_tkey );

  			
INSERT INTO marketplace (tkey,  version,  creationdate, organization_tkey )
  SELECT (temp.tkey + (SELECT COALESCE((SELECT max(tkey) FROM marketplace),1))),
          1,
          temp.creationdate,
          temp.organization_tkey
  FROM marketplace_temp AS temp;
  
INSERT INTO marketplacehistory ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion",creationdate,organizationobjkey,"global" ) 
  SELECT (tkey + (SELECT COALESCE((SELECT max(tkey) FROM marketplacehistory),1))), now(), 'ADD', '1000', tkey, 0, creationdate, organization_tkey, 'false' 
  FROM marketplace_temp;

  
UPDATE "marketplace" mp SET "marketplaceid" = (
	SELECT DISTINCT "organizationid" FROM "organization" 
	WHERE "organization"."tkey" = mp."organization_tkey");

UPDATE "marketplacehistory" mph SET "marketplaceid" = (
	SELECT DISTINCT "organizationid" FROM "organization" 
	WHERE "organization"."tkey" = mph."organizationobjkey" );
	

-- add constraints after migration	
CREATE INDEX "marketplace_organization_idx" ON "marketplace" ("organization_tkey");
ALTER TABLE "marketplace" ALTER COLUMN "marketplaceid" SET NOT NULL;
CREATE UNIQUE INDEX "marketplace_bk" ON "marketplace" ("marketplaceid");

ALTER TABLE "marketplace" ADD CONSTRAINT "marketplace_pk" PRIMARY KEY ("tkey");
ALTER TABLE "marketplacehistory" ADD CONSTRAINT "marketplacehistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "marketplace" ADD CONSTRAINT "marketplace_to_org_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
	

-- add default local marketplace name
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") SELECT 'en', mp.tkey, 'MARKETPLACE_NAME', 'private marketplace' FROM "marketplace" mp WHERE mp.tkey <> 1;
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") SELECT 'de', mp.tkey, 'MARKETPLACE_NAME', 'privater Marktplatz' FROM "marketplace" mp WHERE mp.tkey <> 1;
INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") SELECT 'ja', mp.tkey, 'MARKETPLACE_NAME', '民間市場' FROM "marketplace" mp WHERE mp.tkey <> 1;

-- Hibernate
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'Marketplace', COALESCE((MAX(tkey)/1000),0)+10 FROM marketplace;
INSERT INTO hibernate_sequences ("sequence_name", "sequence_next_hi_value") 
	SELECT 'MarketplaceHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM marketplacehistory;

DELETE FROM hibernate_sequences WHERE sequence_name = 'Shop';
DELETE FROM hibernate_sequences WHERE sequence_name = 'ShopHistory';

-- Clean up	
DROP TABLE "marketplace_temp";