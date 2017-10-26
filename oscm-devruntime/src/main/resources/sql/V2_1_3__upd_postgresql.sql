ALTER TABLE "subscription" DROP CONSTRAINT "subscription_to_mpl_fk";

ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_mpl_fk" FOREIGN KEY ("marketplace_tkey")
    REFERENCES "marketplace" ("tkey"); 