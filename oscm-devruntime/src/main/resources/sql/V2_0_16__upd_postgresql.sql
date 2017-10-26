------------------------------------------------
-- Migrate Subscription and SubscriptionHistory
------------------------------------------------

ALTER TABLE "subscription" ADD COLUMN "marketplace_tkey" BIGINT;
ALTER TABLE "subscriptionhistory" ADD COLUMN "marketplaceobjkey" BIGINT;

UPDATE "subscription" AS sub SET "marketplace_tkey" = (
	SELECT mp.tkey FROM  organization org, organizationreference ref, marketplace mp
    WHERE sub.organizationkey= org.tkey AND ref.targetkey = org.tkey AND mp.organization_tkey=ref.sourcekey);
    
UPDATE "subscriptionhistory" AS sub SET "marketplaceobjkey" = (
	SELECT mp.tkey FROM  organization org, organizationreference ref, marketplace mp
    WHERE sub.organizationobjkey= org.tkey AND ref.targetkey = org.tkey AND mp.organization_tkey=ref.sourcekey);
    