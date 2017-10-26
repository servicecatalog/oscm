------------------------------
-- REQ Lock service parameter after successful provisioning - 2139
------------------------------
-- Alter tables by adding new column
ALTER TABLE "parameterdefinition" ADD COLUMN "modificationtype" VARCHAR(255);
ALTER TABLE "parameterdefinitionhistory" ADD COLUMN "modificationtype" VARCHAR(255);

UPDATE "parameterdefinition" SET "modificationtype" = 'STANDARD';
UPDATE "parameterdefinitionhistory" SET "modificationtype" = 'STANDARD';

ALTER TABLE "parameterdefinition" ALTER COLUMN "modificationtype" SET NOT NULL;
ALTER TABLE "parameterdefinitionhistory" ALTER COLUMN "modificationtype" SET NOT NULL;