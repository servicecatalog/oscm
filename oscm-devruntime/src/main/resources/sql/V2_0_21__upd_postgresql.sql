ALTER TABLE "organizationtopaymenttype" RENAME TO "organizationreftopaymenttype";

-- rename indexes
ALTER INDEX orgtopaymenttype_orgrole_nuidx RENAME TO orgreftopaymenttype_orgrole_nuidx;
ALTER INDEX orgtopaymenttype_pt_nuidx RENAME TO orgreftopaymenttype_pt_nuidx;

-- drop indexes
DROP INDEX orgtopaymenttype_bk_idx;
DROP INDEX orgtopaymenttype_org_nuidx;
DROP INDEX billingcontact_orgkey_uidx;

-- drop foreign key
ALTER TABLE "organizationreftopaymenttype" DROP CONSTRAINT organizationtopaymenttype_organization_fk;

ALTER TABLE "organizationreftopaymenttype" RENAME COLUMN "organization_tkey" TO "organizationreference_tkey";



-- migrate data

-- Migration: For table organizationreftopaymenttype change foreign key from organization to organizationreference
ALTER TABLE "organizationreftopaymenttype" ADD COLUMN "migration_cust" BIGINT;
ALTER TABLE "organizationreftopaymenttype" ADD COLUMN "migration_supp" BIGINT;


UPDATE "organizationreftopaymenttype" AS orgToPymtType  SET "migration_cust" =  (
	SELECT ref.tkey FROM organizationreference AS ref, organization AS org 
	WHERE ref.targetkey=org.tkey 
	AND orgToPymtType.organizationreference_tkey = org.tkey 
	AND ref.referencetype='SUPPLIER_TO_CUSTOMER'
	AND orgToPymtType.organizationrole_tkey='3');
UPDATE "organizationreftopaymenttype" AS orgToPymtType  SET "migration_supp" =  (
	SELECT ref.tkey FROM organizationreference AS ref, organization AS org 
	WHERE ref.targetkey=org.tkey 
	AND orgToPymtType.organizationreference_tkey = org.tkey 
	AND ref.referencetype='PLATFORM_OPERATOR_TO_SUPPLIER'
	AND orgToPymtType.organizationrole_tkey='1');

UPDATE "organizationreftopaymenttype" SET "organizationreference_tkey" = "migration_cust" WHERE "migration_cust" IS NOT null ;
UPDATE "organizationreftopaymenttype" SET "organizationreference_tkey" = "migration_supp" WHERE "migration_supp" IS NOT null;

ALTER TABLE "organizationreftopaymenttype" DROP COLUMN "migration_cust";
ALTER TABLE "organizationreftopaymenttype" DROP COLUMN "migration_supp";




-- recreate foreign key
ALTER TABLE "organizationreftopaymenttype" ADD CONSTRAINT organizationreftopaymenttype_organizationref_fk FOREIGN KEY (organizationreference_tkey)
	REFERENCES "organizationreference"(tkey) ON UPDATE NO ACTION ON DELETE NO ACTION;
	
--change the relation of paymentinfo from organization to subscription
--drop foreign key
ALTER TABLE "organization" DROP COLUMN "defaultpayment_tkey";
ALTER TABLE "organizationhistory" DROP COLUMN "defaultpaymentobjkey";

-- create object references
ALTER TABLE "billingcontact" ADD COLUMN "paymentinfo_tkey" BIGINT;
ALTER TABLE "billingcontacthistory" ADD COLUMN "paymentinfoobjkey" BIGINT;

ALTER TABLE "subscription" ADD COLUMN "paymentinfo_tkey" BIGINT;
ALTER TABLE "subscriptionhistory" ADD COLUMN "paymentinfoobjkey" BIGINT;
ALTER TABLE "paymentinfohistory" ADD COLUMN "billingcontactobjkey" BIGINT;

ALTER TABLE "billingresult" ADD COLUMN "subscriptionkey" BIGINT;

ALTER TABLE "paymentinfo" ADD COLUMN "orgreftopaymenttype_tkey" BIGINT;
ALTER TABLE "paymentinfohistory" ADD COLUMN "orgreftopaymenttypeobjkey" BIGINT;

--add the new foreign key
ALTER TABLE "subscription" ADD CONSTRAINT "subscription_to_paymentinfo_fk" FOREIGN KEY ("paymentinfo_tkey")
	REFERENCES "paymentinfo" ("tkey");
ALTER TABLE "billingcontact" ADD CONSTRAINT "billingcontact_to_paymentinfo_fk" FOREIGN KEY ("paymentinfo_tkey")
	REFERENCES "paymentinfo" ("tkey");
ALTER TABLE "paymentinfo" ADD CONSTRAINT "paymentinfo_to_orgreftopt_fk" FOREIGN KEY ("orgreftopaymenttype_tkey")
	REFERENCES "organizationreftopaymenttype" ("tkey");
	
-- recreate indexes
CREATE UNIQUE INDEX "billingcontact_paymentinfo_uidx" ON "billingcontact" ("paymentinfo_tkey" asc);

-- migrate paymentinfo
UPDATE "paymentinfo" AS pi SET "orgreftopaymenttype_tkey" = (
	SELECT or2pt.tkey 
	FROM organizationreftopaymenttype or2pt, organizationreference oref
	WHERE pi.organizationkey = oref.targetkey
    	AND oref.tkey = or2pt.organizationreference_tkey
    	AND or2pt.organizationrole_tkey = 3
    	AND pi.paymenttype_tkey = or2pt.paymenttype_tkey);
    	
-- migrate paymentinfohistory
UPDATE "paymentinfohistory" AS pih SET "orgreftopaymenttypeobjkey" = (
    SELECT or2pt.tkey
    FROM organizationreftopaymenttype or2pt, organizationreferencehistory orh
    WHERE pih.organizationobjkey = orh.targetobjkey
        AND or2pt.organizationreference_tkey = orh.objkey
        AND orh.referencetype = 'SUPPLIER_TO_CUSTOMER'
        AND orh.moddate <= pih.moddate
    ORDER BY orh.moddate DESC LIMIT 1);
    
UPDATE "paymentinfohistory" AS pih SET "billingcontactobjkey" = (
    SELECT bch.objkey 
    FROM billingcontacthistory bch
    WHERE bch.organizationobjkey = pih.organizationobjkey
        AND bch.moddate <= pih.moddate
    ORDER BY bch.moddate DESC LIMIT 1);
    
-- migrate billingcontact
UPDATE "billingcontact" AS bc SET "paymentinfo_tkey" = (
	SELECT pi.tkey FROM paymentinfo pi
	WHERE pi.organizationkey = bc.organization_tkey);
	
-- migrate billingcontacthistory
UPDATE "billingcontacthistory" AS bch SET "paymentinfoobjkey" = (
    SELECT pih.objkey 
    FROM paymentinfohistory pih
    WHERE bch.organizationobjkey = pih.organizationobjkey
        AND pih.moddate <= bch.moddate
    ORDER BY pih.moddate DESC LIMIT 1);

-- Migration: For table subscription change foreign key from organization to paymentinfo
UPDATE "subscription" AS sub SET paymentinfo_tkey = (
    SELECT payInf.tkey FROM paymentinfo AS payInf
    WHERE payInf.organizationkey=sub.organizationkey);
    
UPDATE "subscriptionhistory" AS subHist SET paymentinfoobjkey = (
    SELECT payInfHist.objkey FROM paymentinfohistory AS payInfHist
    WHERE payInfHist.organizationobjkey=subHist.organizationobjkey
      and payInfHist.moddate <= subHist.moddate ORDER BY payInfHist.moddate DESC LIMIT 1);
    
-- drop columns only after the data is migrated
ALTER TABLE "paymentinfo" DROP COLUMN "organizationkey";
ALTER TABLE "paymentinfohistory" DROP COLUMN "organizationobjkey";

-- hibernate

UPDATE "hibernate_sequences" SET sequence_name = 'OrganizationRefToPaymentType' WHERE sequence_name = 'OrganizationToPaymentType';


