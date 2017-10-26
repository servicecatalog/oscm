CREATE TABLE "catalogentry" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"position" INTEGER NOT NULL,
		"product_tkey" BIGINT,
		"organization_tkey" BIGINT 
	);
	
	
CREATE TABLE "catalogentryhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"position" INTEGER NOT NULL,
		"productobjkey" BIGINT,
		"organizationobjkey" BIGINT
	);

--CREATE UNIQUE INDEX "catalogentry_position_uidx" ON "catalogentry" ("organization_tkey", "position");

ALTER TABLE "catalogentry" ADD CONSTRAINT "catalogentry_pk" PRIMARY KEY ("tkey");
ALTER TABLE "catalogentryhistory" ADD CONSTRAINT "catalogentryhistory_pk" PRIMARY KEY ("tkey");

ALTER TABLE "catalogentry" ADD CONSTRAINT "catalogentry_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "catalogentry" ADD CONSTRAINT "catalogentry_product_fk" FOREIGN KEY ("product_tkey")
	REFERENCES "product" ("tkey");
	
CREATE TABLE temp (
	"tkey" BIGINT NOT NULL,
	"version" INTEGER NOT NULL,
	"position" serial,
	"organization_tkey" BIGINT NOT NULL,
	"product_tkey" BIGINT NOT NULL
	);

INSERT INTO "temp" ("tkey", "version", "product_tkey", "organization_tkey")
	SELECT product.tkey, 0, product.tkey, sup.tkey
	FROM "organization" sup, "product" product
	WHERE sup.tkey = product.supplierkey
	ORDER BY sup.tkey ASC, product.tkey ASC;
	
INSERT INTO "catalogentry"("tkey",	"version", "position", "product_tkey", "organization_tkey")
	SELECT temp.tkey, temp.version, 
	(temp.position - 
	(SELECT COALESCE((
		SELECT SUM(entrycount) 
		FROM (SELECT COUNT(*) AS ENTRYCOUNT, supplierkey FROM product GROUP BY supplierkey ORDER BY supplierkey) counter 
		WHERE supplierkey < temp.organization_tkey), 0))), 
	temp.product_tkey, temp.organization_tkey
	FROM "temp" temp;
DROP TABLE "temp";

INSERT INTO "catalogentryhistory"("tkey", "moduser", "modtype", "moddate", "objkey", "objversion", "position", "productobjkey", "organizationobjkey")
	SELECT orig.tkey, '1000', 'ADD', now(), orig.tkey, orig.version, orig.position, orig.product_tkey, orig.organization_tkey
	FROM "catalogentry" orig;
	
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'CatalogEntry', COALESCE((MAX(tkey)/1000),0)+10 FROM "catalogentry";
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") SELECT 'CatalogEntryHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM "catalogentryhistory";