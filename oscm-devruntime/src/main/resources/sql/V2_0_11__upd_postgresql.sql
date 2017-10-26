------------------------------------------
-- Migrate table Discount
-- 1) change foreign key organization_tkey (organization table) to organizationreference_tkey (organizationreference table)
--
-- Migrate table Discounthistory
-- 1) rename organizationobjkey to organizationreferenceobjkey
------------------------------------------


-- schema
DROP INDEX "discount_orgkey_uidx";
DROP INDEX "discount_orgkey_nuidx";
ALTER TABLE "discount" DROP CONSTRAINT "discount_organization_fk";
ALTER TABLE "discount" RENAME COLUMN "organization_tkey" TO "organizationreference_tkey";
ALTER TABLE "discounthistory" RENAME COLUMN "organizationobjkey" TO "organizationreferenceobjkey";
	
-- migrate data	
ALTER TABLE "discount" ADD COLUMN "migration" BIGINT;	
UPDATE "discount" AS discount
	SET "migration" =  (SELECT orgref.tkey FROM organizationreference AS orgref, organization AS org 
		WHERE orgref.targetkey=org.tkey AND discount.organizationreference_tkey = org.tkey);
UPDATE "discount" SET "organizationreference_tkey" = "migration";

ALTER TABLE "discounthistory" ADD COLUMN "migration" BIGINT;
UPDATE "discounthistory" AS dishist SET "migration" = (
	SELECT orgref.tkey FROM organizationreference AS orgref, organization AS org 
	WHERE orgref.targetkey=org.tkey AND dishist.organizationreferenceobjkey = org.tkey ); 
UPDATE "discounthistory" SET "organizationreferenceobjkey" = "migration";

-- add constraints after data migration
ALTER TABLE "discount" ADD CONSTRAINT "discount_organizationreference_fk" 
	FOREIGN KEY ("organizationreference_tkey")	REFERENCES "organizationreference" ("tkey");

-- clean up
ALTER TABLE "discount" DROP COLUMN "migration";
ALTER TABLE "discounthistory" DROP COLUMN "migration";