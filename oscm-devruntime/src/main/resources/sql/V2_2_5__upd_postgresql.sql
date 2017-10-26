------------------------------
-- REQ New price model types - 
------------------------------

-- create new tables

CREATE TABLE "revenuesharemodel" (
		"tkey" BIGINT NOT NULL,
		"revenueshare" NUMERIC(5, 2) NOT NULL,
		"revenuesharemodeltype" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	);	

ALTER TABLE "revenuesharemodel" ADD CONSTRAINT "revenuesharemodel_pk" PRIMARY KEY ("tkey");

CREATE TABLE "revenuesharemodelhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"revenueshare" NUMERIC(5, 2) NOT NULL,
		"revenuesharemodeltype" VARCHAR(255) NOT NULL,
		"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00'
	);	

ALTER TABLE "revenuesharemodelhistory" ADD CONSTRAINT "revenuesharemodelhistory_pk" PRIMARY KEY ("tkey");

-- enhance existing tables

ALTER TABLE "marketplace" ADD COLUMN "pricemodel_tkey" BIGINT;
ALTER TABLE "marketplace" ADD CONSTRAINT "pricemodel_fk" FOREIGN KEY ("pricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");
ALTER TABLE "marketplace" ADD COLUMN "brokerpricemodel_tkey" BIGINT;
ALTER TABLE "marketplace" ADD CONSTRAINT "brokerpricemodel_fk" FOREIGN KEY ("brokerpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");
ALTER TABLE "marketplace" ADD COLUMN "resellerpricemodel_tkey" BIGINT;
ALTER TABLE "marketplace" ADD CONSTRAINT "resellerpricemodel_fk" FOREIGN KEY ("resellerpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");

ALTER TABLE "marketplacehistory" ADD COLUMN "pricemodelobjkey" BIGINT;
ALTER TABLE "marketplacehistory" ADD COLUMN "brokerpricemodelobjkey" BIGINT;
ALTER TABLE "marketplacehistory" ADD COLUMN "resellerpricemodelobjkey" BIGINT;

ALTER TABLE "catalogentry" ADD COLUMN "brokerpricemodel_tkey" BIGINT;
ALTER TABLE "catalogentry" ADD CONSTRAINT "brokerpricemodel_fk" FOREIGN KEY ("brokerpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");
ALTER TABLE "catalogentry" ADD COLUMN "resellerpricemodel_tkey" BIGINT;
ALTER TABLE "catalogentry" ADD CONSTRAINT "resellerpricemodel_fk" FOREIGN KEY ("resellerpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");

ALTER TABLE "catalogentryhistory" ADD COLUMN "brokerpricemodelobjkey" BIGINT;
ALTER TABLE "catalogentryhistory" ADD COLUMN "resellerpricemodelobjkey" BIGINT;

-- migrate existing marketplaces

CREATE TABLE "revenuesharemodel_temp" (
    "tkey" SERIAL PRIMARY KEY,
	"marketplace_tkey" BIGINT NOT NULL,
	"revenuesharemodeltype" VARCHAR(255) NOT NULL
);	

INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
  	SELECT m.tkey,'BROKER_REVENUE_SHARE' FROM marketplace m;
INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
  	SELECT m.tkey,'RESELLER_REVENUE_SHARE' FROM marketplace m;
INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
  	SELECT m.tkey,'MARKETPLACE_REVENUE_SHARE' FROM marketplace m;

INSERT INTO "revenuesharemodel" ( tkey, revenueshare, revenuesharemodeltype, version )
  	SELECT r.tkey, 0, r.revenuesharemodeltype, 0 FROM revenuesharemodel_temp r;	
INSERT INTO "revenuesharemodelhistory" ( tkey, moddate, modtype, moduser, objkey, objversion, revenueshare, revenuesharemodeltype )
  	SELECT r.tkey, now(), 'ADD', 'ANONYMOUS', r.tkey, 0, 0, r.revenuesharemodeltype FROM revenuesharemodel_temp r;	
  	
UPDATE "marketplace" AS m SET pricemodel_tkey = (
    SELECT r.tkey FROM revenuesharemodel_temp r
    WHERE r.marketplace_tkey = m.tkey AND r.revenuesharemodeltype = 'MARKETPLACE_REVENUE_SHARE');
UPDATE "marketplace" AS m SET brokerpricemodel_tkey = (
    SELECT r.tkey FROM revenuesharemodel_temp r
    WHERE r.marketplace_tkey = m.tkey AND r.revenuesharemodeltype = 'BROKER_REVENUE_SHARE');
UPDATE "marketplace" AS m SET resellerpricemodel_tkey = (
    SELECT r.tkey FROM revenuesharemodel_temp r
    WHERE r.marketplace_tkey = m.tkey AND r.revenuesharemodeltype = 'RESELLER_REVENUE_SHARE');

UPDATE "marketplacehistory" AS mh SET pricemodelobjkey = (
    SELECT m.pricemodel_tkey FROM marketplace m
    WHERE m.tkey = mh.objkey);
UPDATE "marketplacehistory" AS mh SET brokerpricemodelobjkey = (
    SELECT m.brokerpricemodel_tkey FROM marketplace m
    WHERE m.tkey = mh.objkey);
UPDATE "marketplacehistory" AS mh SET resellerpricemodelobjkey = (
    SELECT m.resellerpricemodel_tkey FROM marketplace m
    WHERE m.tkey = mh.objkey);

-- Set the revenue share models for the deleted marketplaces.     	
DELETE FROM "revenuesharemodel_temp";
INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
	SELECT DISTINCT 0,'MARKETPLACE_REVENUE_SHARE' FROM marketplacehistory mh WHERE mh.objkey NOT IN (SELECT tkey from marketplace m WHERE m.tkey = mh.objkey);
INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
	SELECT DISTINCT 0,'BROKER_REVENUE_SHARE' FROM marketplacehistory mh WHERE mh.objkey NOT IN (SELECT tkey from marketplace m WHERE m.tkey = mh.objkey);
INSERT INTO "revenuesharemodel_temp" ( marketplace_tkey, revenuesharemodeltype )
	SELECT DISTINCT 0,'RESELLER_REVENUE_SHARE' FROM marketplacehistory mh WHERE mh.objkey NOT IN (SELECT tkey from marketplace m WHERE m.tkey = mh.objkey);

  	
INSERT INTO "revenuesharemodelhistory" ( tkey, moddate, modtype, moduser, objkey, objversion, revenueshare, revenuesharemodeltype )
  	SELECT r.tkey, now(), 'ADD', 'ANONYMOUS', r.tkey, 0, 0, r.revenuesharemodeltype FROM revenuesharemodel_temp r;
  	
UPDATE "marketplacehistory" AS mh SET pricemodelobjkey = (
    	SELECT r.tkey FROM revenuesharemodel_temp r WHERE r.revenuesharemodeltype = 'MARKETPLACE_REVENUE_SHARE')
    WHERE mh.pricemodelobjkey IS NULL;   
UPDATE "marketplacehistory" AS mh SET brokerpricemodelobjkey = (
    	SELECT r.tkey FROM revenuesharemodel_temp r WHERE r.revenuesharemodeltype = 'BROKER_REVENUE_SHARE')
    WHERE mh.brokerpricemodelobjkey IS NULL;
UPDATE "marketplacehistory" AS mh SET resellerpricemodelobjkey = (
    	SELECT r.tkey FROM revenuesharemodel_temp r WHERE r.revenuesharemodeltype = 'RESELLER_REVENUE_SHARE')
    WHERE mh.resellerpricemodelobjkey IS NULL;
 
  	
DROP TABLE "revenuesharemodel_temp";

-- modify mandatory columns

ALTER TABLE "marketplace" ALTER COLUMN "pricemodel_tkey" SET NOT NULL;
ALTER TABLE "marketplace" ALTER COLUMN "brokerpricemodel_tkey" SET NOT NULL;
ALTER TABLE "marketplace" ALTER COLUMN "resellerpricemodel_tkey" SET NOT NULL;

ALTER TABLE "marketplacehistory" ALTER COLUMN "pricemodelobjkey" SET NOT NULL;
ALTER TABLE "marketplacehistory" ALTER COLUMN "brokerpricemodelobjkey" SET NOT NULL;
ALTER TABLE "marketplacehistory" ALTER COLUMN "resellerpricemodelobjkey" SET NOT NULL;

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('RevenueShareModel', (select COALESCE((MAX(case when tkey is null then 0 else tkey end)/1000),0)+10 from revenuesharemodel));
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('RevenueShareModelHistory', (select COALESCE((MAX(case when tkey is null then 0 else tkey end)/1000),0)+10 from revenuesharemodelhistory));

