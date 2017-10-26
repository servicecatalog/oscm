ALTER TABLE "subscription" ADD COLUMN "uuid" uuid;
ALTER TABLE "subscription" ADD COLUMN "eventpublished" boolean NOT NULL DEFAULT false;
ALTER TABLE "subscriptionhistory" ADD "eventpublished" boolean NOT NULL DEFAULT false;
