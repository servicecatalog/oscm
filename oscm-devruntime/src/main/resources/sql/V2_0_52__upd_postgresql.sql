ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_mpl_fk" FOREIGN KEY ("marketplace_tkey")
    REFERENCES "marketplace" ("tkey") ON DELETE SET NULL; 