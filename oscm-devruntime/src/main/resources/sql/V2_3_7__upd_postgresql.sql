----------------------------------------------------
-- REQ Introduce revenue share for platform operator 
----------------------------------------------------

-- enhance table organization by operator price model
ALTER TABLE "organization" ADD COLUMN "operatorpricemodel_tkey" BIGINT;
ALTER TABLE "organization" ADD CONSTRAINT "operatorpricemodel_fk" FOREIGN KEY ("operatorpricemodel_tkey") REFERENCES "revenuesharemodel" ("tkey");
ALTER TABLE "organizationhistory" ADD COLUMN "operatorpricemodelobjkey" BIGINT;

-- migrate existing supplier organizations
-- the temp table stores the keys of all supplier organizations and the keys of 
-- the corresponding new revsharemodel/-history entries that have to be created  
CREATE TABLE "revenuesharemodel_temp" (
    "tkey" SERIAL PRIMARY KEY,
	"organization_tkey" BIGINT NOT NULL,
	"revsharemodel_tkey" BIGINT NOT NULL,
	"revsharemodelhistory_tkey" BIGINT NOT NULL
);	
INSERT INTO "revenuesharemodel_temp" ( organization_tkey, revsharemodel_tkey, revsharemodelhistory_tkey )
    SELECT org.tkey, 0, 0 
    FROM organization org, organizationtorole orgtorole, organizationrole orgrole 
    WHERE orgtorole.organization_tkey = org.tkey AND orgtorole.organizationrole_tkey = orgrole.tkey
           AND orgrole.rolename = 'SUPPLIER';
UPDATE "revenuesharemodel_temp" AS r 
    SET revsharemodel_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodel"), 0) + r.tkey,
        revsharemodelhistory_tkey = COALESCE((SELECT max("tkey") FROM "revenuesharemodelhistory"), 0) + r.tkey;

INSERT INTO "revenuesharemodel" ( tkey, revenueshare, revenuesharemodeltype, version )
  	SELECT r.revsharemodel_tkey, 0, 'OPERATOR_REVENUE_SHARE', 0 FROM revenuesharemodel_temp r;	
INSERT INTO "revenuesharemodelhistory" ( tkey, moddate, modtype, moduser, objkey, objversion, revenueshare, revenuesharemodeltype, invocationdate )
  	SELECT r.revsharemodelhistory_tkey, '1970-01-01 00:00:00', 'ADD', 'ANONYMOUS', r.revsharemodel_tkey, 0, 0, 'OPERATOR_REVENUE_SHARE', now() FROM revenuesharemodel_temp r;	

UPDATE "organization" AS o
    SET operatorpricemodel_tkey = ( SELECT r.revsharemodel_tkey FROM revenuesharemodel_temp r WHERE r.organization_tkey = o.tkey );

DROP TABLE "revenuesharemodel_temp";
    
-- migrate organization history entries: set operator revenue share for every entry,
-- that was created at the same time or after a supplier role was created 
UPDATE "organizationhistory" AS oh 
SET operatorpricemodelobjkey = ( SELECT o.operatorpricemodel_tkey FROM organization o WHERE o.tkey = oh.objkey )
WHERE oh.tkey IN ( 
    SELECT orgh.tkey FROM organizationhistory orgh, organizationtorolehistory orgtrh, organizationrole orgrole
    WHERE orgh.objkey = orgtrh.organizationtkey AND orgtrh.organizationroletkey = orgrole.tkey
           AND orgrole.rolename = 'SUPPLIER' AND orgh.moddate >= orgtrh.moddate 
);

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "revenuesharemodel") where "sequence_name" = 'RevenueShareModel';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "revenuesharemodelhistory") where "sequence_name" = 'RevenueShareModelHistory';
