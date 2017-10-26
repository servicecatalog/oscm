-- drop unused colum (Bug 9318)
ALTER TABLE "product" DROP COLUMN "paramconfigurl";
ALTER TABLE "producthistory" DROP COLUMN "paramconfigurl";