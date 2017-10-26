-- the new id fields
ALTER TABLE billingcontact ADD COLUMN "billingcontactid" CHARACTER VARYING(255);
UPDATE billingcontact SET billingcontactid = substring(email from 1 for 40);

ALTER TABLE billingcontacthistory ADD COLUMN "billingcontactid" CHARACTER VARYING(255);
UPDATE billingcontacthistory SET billingcontactid = substring(email from 1 for 40);

ALTER TABLE paymentinfo ADD COLUMN "paymentinfoid" CHARACTER VARYING(255);
UPDATE paymentinfo pi SET paymentinfoid = 
	(
		SELECT	pt.paymenttypeid
			FROM	paymenttype pt
		 WHERE	pi.paymenttype_tkey = pt.tkey
	);

ALTER TABLE paymentinfohistory ADD COLUMN "paymentinfoid" CHARACTER VARYING(255);
UPDATE paymentinfohistory pi SET paymentinfoid = 
	(
		SELECT	pt.paymenttypeid
			FROM	paymenttype pt
		 WHERE	pi.paymenttypeobjkey = pt.tkey
	);

-- reference from payment info to organization
ALTER TABLE paymentinfo ADD COLUMN organization_tkey BIGINT;
UPDATE paymentinfo pi1 SET organization_tkey =
	(
		SELECT	orgref.targetkey	
			FROM	paymentinfo pi2,
						organizationreftopaymenttype opt,
						organizationreference orgref
		 WHERE	pi1.tkey = pi2.tkey
		   AND	pi2.orgreftopaymenttype_tkey = opt.tkey
		   AND	opt.organizationreference_tkey = orgref.tkey
		   AND	orgref.referencetype = 'SUPPLIER_TO_CUSTOMER'
	);

ALTER TABLE paymentinfohistory ADD COLUMN organizationobjkey BIGINT;
-- this will only update the history of still existing payment infos with the payment type enabled
UPDATE paymentinfohistory pi1 SET organizationobjkey =
	(
		SELECT	orgref.targetkey	
			FROM	paymentinfo pi2,
						organizationreftopaymenttype opt,
						organizationreference orgref
		 WHERE	pi1.objkey = pi2.tkey
		   AND	pi2.orgreftopaymenttype_tkey = opt.tkey
		   AND	opt.organizationreference_tkey = orgref.tkey
		   AND	orgref.referencetype = 'SUPPLIER_TO_CUSTOMER'
	);


-- provider and account information
ALTER TABLE paymentinfo ADD COLUMN providername CHARACTER VARYING(255);
UPDATE paymentinfo pi SET providername = 'unknown' WHERE pi.paymenttype_tkey = 1 OR pi.paymenttype_tkey = 2;
ALTER TABLE paymentinfohistory ADD COLUMN providername CHARACTER VARYING(255);
UPDATE paymentinfohistory pi SET providername = 'unknown' WHERE pi.paymenttypeobjkey = 1 OR pi.paymenttypeobjkey = 2;

ALTER TABLE paymentinfo ADD COLUMN accountnumber CHARACTER VARYING(255);
UPDATE paymentinfo pi SET accountnumber = '********' WHERE pi.paymenttype_tkey = 1 OR pi.paymenttype_tkey = 2;
ALTER TABLE paymentinfohistory ADD COLUMN accountnumber CHARACTER VARYING(255);
UPDATE paymentinfohistory pi SET accountnumber = '********' WHERE pi.paymenttypeobjkey = 1 OR pi.paymenttypeobjkey = 2;


-- reference subscription to billing contact
ALTER TABLE subscription ADD COLUMN billingcontact_tkey BIGINT;
UPDATE subscription sub1
SET billingcontact_tkey = 
 (
	SELECT bc.tkey
	  FROM	subscription sub2,
					paymentinfo pi,
					billingcontact bc			
	 WHERE	sub1.tkey = sub2.tkey 
	 	 AND	sub2.paymentinfo_tkey = pi.tkey	
	   AND	pi.tkey = bc.paymentinfo_tkey 
 );

ALTER TABLE subscriptionhistory ADD COLUMN billingcontactobjkey BIGINT;

-- drop old columns
ALTER TABLE billingcontact DROP COLUMN paymentinfo_tkey;
ALTER TABLE billingcontacthistory DROP COLUMN paymentinfoobjkey;

ALTER TABLE paymentinfo DROP COLUMN orgreftopaymenttype_tkey;

ALTER TABLE paymentinfohistory DROP COLUMN billingcontactobjkey;
ALTER TABLE paymentinfohistory DROP COLUMN orgreftopaymenttypeobjkey;

-- contraints
ALTER TABLE "paymentinfo" ADD CONSTRAINT "paymentinfo_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");

ALTER TABLE paymentinfo ALTER COLUMN "paymentinfoid" SET NOT NULL;
ALTER TABLE paymentinfo ALTER COLUMN "organization_tkey" SET NOT NULL;
ALTER TABLE paymentinfo ADD CONSTRAINT pi_orgkey_paymentinfoid_uk UNIQUE (organization_tkey, "paymentinfoid");
ALTER TABLE billingcontact ALTER COLUMN "billingcontactid" SET NOT NULL;
ALTER TABLE billingcontact ADD CONSTRAINT bc_orgkey_billingcontactid_uk UNIQUE (organization_tkey, "billingcontactid");


--------------------------------------------------------------------------------
-- create payment info of type INVOICE for all organizations that don't have one
--------------------------------------------------------------------------------

-- table to remember the orgs without payment info invoice
CREATE TABLE "paytemp" (
		"paykey" SERIAL,
		"orgkey" BIGINT NOT NULL
	);
	
-- table to remember the org key and new payment info key for history
CREATE TABLE "payhisttemp" (
		"histkey" SERIAL,
		"paykey" BIGINT NOT NULL,
		"orgkey" BIGINT NOT NULL
	);

-- get the orgs without payment info INVOICE
INSERT INTO "paytemp" ("orgkey") SELECT "tkey" FROM "organization" AS "org" 
WHERE NOT EXISTS (
	SELECT "tkey" FROM "paymentinfo" AS "pi" 
	WHERE "pi"."organization_tkey" = "org"."tkey" AND "pi"."paymenttype_tkey" = 3);

-- update the keys for the new payment infos
UPDATE "paytemp" SET "paykey" = "paykey" + (SELECT COALESCE ((SELECT max("tkey") FROM "paymentinfo"), 1));

-- get the payment info keys for the hitsory togeter with  new keys for the history itself
INSERT INTO "payhisttemp" ("paykey", "orgkey")
	SELECT "paykey", "orgkey" FROM "paytemp";
	
-- update the keys for the new history entries
UPDATE "payhisttemp" SET "histkey" = "histkey" + (SELECT COALESCE ((SELECT max("tkey") FROM "paymentinfohistory"), 1));

-- insert the new payment infos
INSERT INTO "paymentinfo" ("tkey", "organization_tkey", "creationtime", "version", "paymenttype_tkey", "paymentinfoid") 
	SELECT "paykey", "orgkey", date_part('epoch', now())*1000, 1, 3, 'INVOICE' FROM "paytemp";

-- insert the related history entries
INSERT INTO "paymentinfohistory" ("tkey", "objkey", "organizationobjkey", "creationtime", "moddate", "modtype", "moduser", "objversion", "paymenttypeobjkey", "invocationdate", "paymentinfoid") 
	SELECT "histkey", "paykey", "orgkey", date_part('epoch', now())*1000, now(), 'ADD', 'ANONYMOUS', 1, 3, now(), 'INVOICE'
	FROM "payhisttemp";
	
	
-- update hibernate sequences
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = ( 
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "paymentinfo") WHERE "sequence_name" = 'PaymentInfo';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "paymentinfohistory") WHERE "sequence_name" = 'PaymentInfoHistory';

-- drop temporary tables
DROP TABLE "paytemp";
DROP TABLE "payhisttemp";


