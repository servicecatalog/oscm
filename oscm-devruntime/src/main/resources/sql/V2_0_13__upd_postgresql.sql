------------------------------------------------
-- Migrate Organization
------------------------------------------------


ALTER TABLE "organization" ADD COLUMN "localmarketplace_tkey" BIGINT;
ALTER TABLE "organizationhistory" ADD COLUMN "localmarketplaceobjkey" BIGINT;

UPDATE "organization" AS org SET "localmarketplace_tkey" =  (
	SELECT mp.tkey FROM marketplace AS mp
	WHERE mp.organization_tkey=org.tkey AND mp.global = 'false');
	
UPDATE "organizationhistory" AS oh SET "localmarketplaceobjkey" =  (
	SELECT mp.tkey FROM marketplace AS mp
	WHERE mp.organization_tkey=oh.objkey AND mp.global = 'false');