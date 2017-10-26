ALTER TABLE "subscription" ADD COLUMN "asynctempproductkey" BIGINT DEFAULT NULL;
ALTER TABLE "subscriptionhistory" ADD COLUMN "asynctempproductobjkey" BIGINT DEFAULT NULL;