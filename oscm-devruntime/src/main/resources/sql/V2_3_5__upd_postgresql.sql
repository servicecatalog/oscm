ALTER TABLE "platformuser" ADD COLUMN "passwordrecoverystartdate" BIGINT NOT NULL DEFAULT 0;
ALTER TABLE "platformuserhistory" ADD COLUMN "passwordrecoverystartdate" BIGINT NOT NULL DEFAULT 0;
