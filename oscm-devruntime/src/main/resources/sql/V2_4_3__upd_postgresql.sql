-- Bug10328
ALTER TABLE "subscription" ALTER COLUMN "accessinfo" TYPE VARCHAR(4096);
ALTER TABLE "subscriptionhistory" ALTER COLUMN "accessinfo" TYPE VARCHAR(4096);