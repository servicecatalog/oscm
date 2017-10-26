------------------------------------------------------------
-- Starting new sql scripts for release 15.2.1 with this one
------------------------------------------------------------

---------------------------------------------------------------
-- RQ: GPaaS - Make the "Time slices" configurable for services
---------------------------------------------------------------

-- PRICEMODEL
ALTER TABLE pricemodel ADD "type" VARCHAR(255);
UPDATE pricemodel AS pm SET "type" = (
	SELECT CASE	WHEN (SELECT pm2.ischargeable = FALSE) THEN 'FREE_OF_CHARGE' ELSE 'PRO_RATA' END
	FROM pricemodel pm2 WHERE pm.tkey=pm2.tkey);
ALTER TABLE pricemodel DROP COLUMN ischargeable;
ALTER TABLE pricemodel ALTER COLUMN "type" SET NOT NULL;


-- PRICEMODEL HISTORY
ALTER TABLE pricemodelhistory ADD "type" VARCHAR(255);
UPDATE pricemodelhistory AS pmh SET "type" = (
	SELECT CASE	WHEN (SELECT pmh2.ischargeable = FALSE) THEN 'FREE_OF_CHARGE' ELSE 'PRO_RATA' END
	FROM pricemodelhistory pmh2 WHERE pmh.tkey=pmh2.tkey);
ALTER TABLE pricemodelhistory DROP COLUMN ischargeable;