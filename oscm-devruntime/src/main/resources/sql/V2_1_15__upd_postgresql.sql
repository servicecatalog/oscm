ALTER TABLE "marketplace" ADD COLUMN "categoriesenabled" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE "marketplacehistory" ADD COLUMN "categoriesenabled" BOOLEAN NOT NULL DEFAULT TRUE;