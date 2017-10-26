------------------------------------------------------------------------------
-- RQ: MPL - Unify "local" and "global" marketplaces
------------------------------------------------------------------------------

ALTER TABLE "marketplace" DROP COLUMN "global";
ALTER TABLE "marketplace" DROP COLUMN "skinproperties";
ALTER TABLE "marketplacehistory" DROP COLUMN "global";
ALTER TABLE "marketplacehistory" DROP COLUMN "skinproperties";

ALTER TABLE "catalogentry" DROP COLUMN "position";
ALTER TABLE "catalogentryhistory" DROP COLUMN "position";

ALTER TABLE "organization" DROP COLUMN "localmarketplace_tkey";
ALTER TABLE "organizationhistory" DROP COLUMN "localmarketplaceobjkey";

DELETE FROM "localizedresource" WHERE "objecttype" = 'PRICEMODEL_SHORT_DESCRIPTION';