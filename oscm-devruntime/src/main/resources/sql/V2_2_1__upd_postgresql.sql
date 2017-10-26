----------------------------------------------------------
-- Starting new sql scripts for release 15.2 with this one
----------------------------------------------------------

ALTER TABLE "organization" ADD COLUMN "supportemail" VARCHAR(255);
ALTER TABLE "organizationhistory" ADD COLUMN "supportemail" VARCHAR(255);