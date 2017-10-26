-------------------------------------------------------------------
-- Schema and data migration changes for any decimal places support
-------------------------------------------------------------------
ALTER TABLE "pricedevent" ALTER COLUMN "eventprice" TYPE NUMERIC;
UPDATE "pricedevent" SET "eventprice" = ( 
	"eventprice"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedeventhistory" ALTER COLUMN "eventprice" TYPE NUMERIC;
UPDATE "pricedeventhistory" SET "eventprice" = ( 
	"eventprice"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );
	
ALTER TABLE "pricedoption" ALTER COLUMN "pricepersubscription" TYPE NUMERIC;
UPDATE "pricedoption" SET "pricepersubscription" = ( 
	"pricepersubscription"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );
	
ALTER TABLE "pricedoption" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedoption" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedoptionhistory" ALTER COLUMN "pricepersubscription" TYPE NUMERIC;
UPDATE "pricedoptionhistory" SET "pricepersubscription" = ( 
	"pricepersubscription"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedoptionhistory" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedoptionhistory" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedparameter" ALTER COLUMN "pricepersubscription" TYPE NUMERIC;
UPDATE "pricedparameter" SET "pricepersubscription" = ( 
	"pricepersubscription"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedparameter" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedparameter" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedparameterhistory" ALTER COLUMN "pricepersubscription" TYPE NUMERIC;
UPDATE "pricedparameterhistory" SET "pricepersubscription" = ( 
	"pricepersubscription"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedparameterhistory" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedparameterhistory" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedproductrole" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedproductrole" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricedproductrolehistory" ALTER COLUMN "priceperuser" TYPE NUMERIC;
UPDATE "pricedproductrolehistory" SET "priceperuser" = ( 
	"priceperuser"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodel" ALTER COLUMN "onetimefee" TYPE NUMERIC;
UPDATE "pricemodel" SET "onetimefee" = ( 
	"onetimefee"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodel" ALTER COLUMN "priceperperiod" TYPE NUMERIC;
UPDATE "pricemodel" SET "priceperperiod" = ( 
	"priceperperiod"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodel" ALTER COLUMN "priceperuserassignment" TYPE NUMERIC;
UPDATE "pricemodel" SET "priceperuserassignment" = ( 
	"priceperuserassignment"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodelhistory" ALTER COLUMN "onetimefee" TYPE NUMERIC;
UPDATE "pricemodelhistory" SET "onetimefee" = ( 
	"onetimefee"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodelhistory" ALTER COLUMN "priceperperiod" TYPE NUMERIC;
UPDATE "pricemodelhistory" SET "priceperperiod" = ( 
	"priceperperiod"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "pricemodelhistory" ALTER COLUMN "priceperuserassignment" TYPE NUMERIC;
UPDATE "pricemodelhistory" SET "priceperuserassignment" = ( 
	"priceperuserassignment"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "steppedprice" ALTER COLUMN "price" TYPE NUMERIC;
UPDATE "steppedprice" SET "price" = ( 
	"price"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "steppedprice" ALTER COLUMN "additionalprice" TYPE NUMERIC;
UPDATE "steppedprice" SET "additionalprice" = ( 
	"additionalprice"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "steppedpricehistory" ALTER COLUMN "price" TYPE NUMERIC;
UPDATE "steppedpricehistory" SET "price" = ( 
	"price"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "steppedpricehistory" ALTER COLUMN "additionalprice" TYPE NUMERIC;
UPDATE "steppedpricehistory" SET "additionalprice" = ( 
	"additionalprice"/(10 ^ COALESCE(CAST((SELECT "env_value" FROM "configurationsetting" WHERE information_id = 'DECIMAL_PLACES') AS  NUMERIC),2)) );

ALTER TABLE "steppedprice" RENAME COLUMN "freeamount" TO "freeentitycount";
ALTER TABLE "steppedpricehistory" RENAME COLUMN "freeamount" TO "freeentitycount";
