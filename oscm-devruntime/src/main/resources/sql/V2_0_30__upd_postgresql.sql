-----------------------------------------------------
-- Enhance: Organization Data, Imageresource
-----------------------------------------------------

ALTER TABLE organization ADD COLUMN "url" VARCHAR(255);
ALTER TABLE organizationhistory ADD COLUMN "url" VARCHAR(255);

ALTER TABLE imageresource ADD CONSTRAINT "imageresource_pk" PRIMARY KEY ("imagetype", "objectkey");