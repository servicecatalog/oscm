ALTER TABLE "catalogentryhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "catalogentryhistory" SET "invocationdate" = "moddate";

ALTER TABLE "organizationtocountryhistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "organizationtocountryhistory" SET "invocationdate" = "moddate";

ALTER TABLE "vatratehistory" ADD COLUMN "invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE "vatratehistory" SET "invocationdate" = "moddate";

