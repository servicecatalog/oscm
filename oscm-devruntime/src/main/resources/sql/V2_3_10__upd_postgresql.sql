----------------------------------------------------
-- REQ Introduce revenue share for platform operator 
----------------------------------------------------

-- enhance table catalogentry by operator price model
ALTER TABLE "catalogentry" ADD COLUMN "operatorpricemodel_tkey" BIGINT;
ALTER TABLE "catalogentry" ADD CONSTRAINT "operatorpricemodel_fk" FOREIGN KEY ("operatorpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");
ALTER TABLE "catalogentryhistory" ADD COLUMN "operatorpricemodelobjkey" BIGINT;


-- migrate existing catalog entries
-- set an operator revenue share = 0 for existing TEMPLATE marketable products (in all states)
CREATE TABLE "revenuesharemodel_temp" (
    "tkey" SERIAL PRIMARY KEY,
	"catalogentry_tkey" BIGINT NOT NULL,
	"revsharemodel_tkey" BIGINT NOT NULL,
	"revsharemodelhistory_tkey" BIGINT NOT NULL
);

INSERT INTO "revenuesharemodel_temp" ( catalogentry_tkey, revsharemodel_tkey, revsharemodelhistory_tkey )
    SELECT ce.tkey, 0, 0 
    FROM catalogentry ce, product p 
    WHERE ce.product_tkey = p.tkey 
    AND p.type = 'TEMPLATE';
    
UPDATE "revenuesharemodel_temp" AS r 
    SET revsharemodel_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodel"), 0) + r.tkey,
        revsharemodelhistory_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodelhistory"), 0) + r.tkey;
        
INSERT INTO "revenuesharemodel" ( tkey, revenueshare, revenuesharemodeltype, version )
  	SELECT r.revsharemodel_tkey, 0, 'OPERATOR_REVENUE_SHARE', 0 FROM revenuesharemodel_temp r;	
INSERT INTO "revenuesharemodelhistory" ( tkey, moddate, modtype, moduser, objkey, objversion, revenueshare, revenuesharemodeltype, invocationdate )
  	SELECT r.revsharemodelhistory_tkey, '1970-01-01 00:00:00', 'ADD', 'ANONYMOUS', r.revsharemodel_tkey, 0, 0, 'OPERATOR_REVENUE_SHARE', now() FROM revenuesharemodel_temp r;
  	
UPDATE "catalogentry" AS ce
    SET operatorpricemodel_tkey = ( SELECT r.revsharemodel_tkey FROM revenuesharemodel_temp r WHERE r.catalogentry_tkey = ce.tkey )
    WHERE tkey in ( SELECT r.catalogentry_tkey FROM revenuesharemodel_temp r);
UPDATE "catalogentryhistory" AS ceh 
	SET operatorpricemodelobjkey = ( SELECT ce.operatorpricemodel_tkey FROM catalogentry ce WHERE ce.tkey = ceh.objkey )
	WHERE ceh.tkey IN ( 
		SELECT ceh2.tkey FROM catalogentryhistory ceh2, revenuesharemodel_temp r
		WHERE ceh2.objkey = r.catalogentry_tkey
	);

-- re-create the temp table to reset its tkey, needed to incremet the tkeys of RevenueShareModel and RevenueShareModelHistory
DROP TABLE "revenuesharemodel_temp";
CREATE TABLE "revenuesharemodel_temp" (
    "tkey" SERIAL PRIMARY KEY,
	"catalogentry_tkey" BIGINT NOT NULL,
	"revsharemodel_tkey" BIGINT NOT NULL,
	"revsharemodelhistory_tkey" BIGINT NOT NULL
);

-- set an operator revenue share = 0 for physically deleted TEMPLATE marketable products (based on deleted technical products)
INSERT INTO "revenuesharemodel_temp" ( catalogentry_tkey, revsharemodel_tkey, revsharemodelhistory_tkey )
    SELECT DISTINCT ceh.objkey, 0, 0 FROM catalogentryhistory AS ceh, producthistory AS ph
	WHERE ceh.productobjkey = ph.objkey
	AND ph.type = 'TEMPLATE'
	AND ph.objkey NOT IN (SELECT tkey FROM product)
	AND ceh.operatorpricemodelobjkey IS NULL;

UPDATE "revenuesharemodel_temp" AS r 
    SET revsharemodel_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodel"), 0) + r.tkey,
        revsharemodelhistory_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodelhistory"), 0) + r.tkey;

INSERT INTO "revenuesharemodelhistory" ( tkey, moddate, modtype, moduser, objkey, objversion, revenueshare, revenuesharemodeltype, invocationdate )
  	SELECT r.revsharemodelhistory_tkey, '1970-01-01 00:00:00', 'DELETE', 'ANONYMOUS', r.revsharemodel_tkey, 0, 0, 'OPERATOR_REVENUE_SHARE', now() FROM revenuesharemodel_temp r;

UPDATE "catalogentryhistory" AS ceh 
	SET operatorpricemodelobjkey = ( SELECT r.revsharemodel_tkey FROM "revenuesharemodel_temp" AS r WHERE r.catalogentry_tkey = ceh.objkey )
	WHERE ceh.objkey IN (
		SELECT catalogentry_tkey FROM revenuesharemodel_temp
	);

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "revenuesharemodel") where "sequence_name" = 'RevenueShareModel';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "revenuesharemodelhistory") where "sequence_name" = 'RevenueShareModelHistory';

DROP TABLE "revenuesharemodel_temp";