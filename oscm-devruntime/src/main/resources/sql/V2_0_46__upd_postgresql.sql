ALTER TABLE "pricemodel" ADD COLUMN "freeperiod" INTEGER NOT NULL DEFAULT '0';
ALTER TABLE "pricemodelhistory" ADD COLUMN "freeperiod" INTEGER NOT NULL DEFAULT '0';