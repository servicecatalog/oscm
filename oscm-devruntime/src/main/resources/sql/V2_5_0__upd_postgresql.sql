------------------------------------------------------------
-- Starting new sql scripts for release 15.4 with this one
------------------------------------------------------------

ALTER TABLE "product" ADD COLUMN "configuratorurl" VARCHAR(512) DEFAULT NULL;
ALTER TABLE "producthistory" ADD COLUMN "configuratorurl" VARCHAR(512) DEFAULT NULL;