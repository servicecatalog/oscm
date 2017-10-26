-------------------
-- Migrate Product
-------------------

-- Schema
ALTER TABLE "product" RENAME COLUMN "supplierkey" TO "vendorkey";

-------------------------
-- Migrate ProductHistory
-------------------------

-- Schema
ALTER TABLE "producthistory" RENAME COLUMN "supplierobjkey" TO "vendorobjkey";
