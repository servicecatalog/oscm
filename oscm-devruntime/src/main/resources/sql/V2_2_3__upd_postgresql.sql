ALTER TABLE "udadefinition" ADD COLUMN "configurationtype" VARCHAR(255);
ALTER TABLE "udadefinitionhistory" ADD COLUMN "configurationtype" VARCHAR(255);
	
UPDATE "udadefinition" SET "configurationtype" = 'SUPPLIER';
UPDATE "udadefinitionhistory" SET "configurationtype" = 'SUPPLIER';

ALTER TABLE "udadefinition" ALTER COLUMN "configurationtype" SET NOT NULL;
ALTER TABLE "udadefinitionhistory" ALTER COLUMN "configurationtype" SET NOT NULL;