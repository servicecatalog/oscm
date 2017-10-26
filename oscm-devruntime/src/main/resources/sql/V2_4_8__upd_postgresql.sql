ALTER TABLE "product" ALTER COLUMN "autoassignuserenabled" DROP NOT NULL;
ALTER TABLE "producthistory" ALTER COLUMN "autoassignuserenabled" DROP NOT NULL;
UPDATE "product" SET "autoassignuserenabled" = NULL where "type" = 'PARTNER_TEMPLATE';
UPDATE "producthistory" SET "autoassignuserenabled" = NULL where "type" = 'PARTNER_TEMPLATE';
