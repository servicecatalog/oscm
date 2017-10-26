INSERT INTO "userrole" ("tkey", "version", "rolename" ) VALUES (8, 0, 'SUBSCRIPTION_MANAGER');

ALTER TABLE "subscription" ADD COLUMN "owner_tkey" BIGINT;

ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_platformuser_fk" FOREIGN KEY ("owner_tkey")
    REFERENCES "platformuser" ("tkey") ON DELETE SET NULL;
    
ALTER TABLE "subscriptionhistory" ADD COLUMN "ownerobjkey" BIGINT;
    

