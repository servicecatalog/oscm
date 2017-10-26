------------------------------------------------------------------------------
-- RQ: ESS FTS - Configurable marketplace functionality
------------------------------------------------------------------------------

ALTER TABLE "marketplace" ADD COLUMN "taggingenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplace" ADD COLUMN "reviewenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplace" ADD COLUMN "socialbookmarkenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplace" ADD COLUMN "brandingurl" VARCHAR(255);

ALTER TABLE "marketplacehistory" ADD COLUMN "taggingenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplacehistory" ADD COLUMN "reviewenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplacehistory" ADD COLUMN "socialbookmarkenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplacehistory" ADD COLUMN "brandingurl" VARCHAR(255);