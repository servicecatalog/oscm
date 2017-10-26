-----------------------------------------------------------
-- Schema and data migration changes on localized resources
-----------------------------------------------------------

CREATE TABLE "localizedresourcetemp" (
	tkey serial,
	version INTEGER NOT NULL,
	locale VARCHAR(255) NOT NULL,
	objecttype VARCHAR(255) NOT NULL,
	objectkey BIGINT NOT NULL,
	value TEXT NOT NULL
);

INSERT INTO "localizedresourcetemp" (version, locale, objecttype, objectkey, value) SELECT version, locale, objecttype, objectkey, value FROM "localizedresource" ORDER BY objectkey ASC; 

DROP TABLE "localizedresource";

CREATE TABLE "localizedresource" (
	tkey BIGINT NOT NULL,
	version INTEGER NOT NULL,
	locale VARCHAR(255) NOT NULL,
	objecttype VARCHAR(255) NOT NULL,
	objectkey BIGINT NOT NULL,
	value TEXT NOT NULL
);

INSERT INTO "localizedresource" (tkey, version, locale, objecttype, objectkey, value) SELECT tkey, version, locale, objecttype, objectkey, value FROM "localizedresourcetemp";

DROP TABLE "localizedresourcetemp";

ALTER TABLE "localizedresource" ADD CONSTRAINT "localizedresource_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "localizedresource_bk_uidx" ON "localizedresource" ("objectkey" ASC, "locale" ASC, "objecttype" ASC);

-------------------------------------------------------
-- Schema and data migration changes on image resources
-------------------------------------------------------

CREATE TABLE "imageresourcetemp" (
	tkey serial,
	imagetype VARCHAR(255) NOT NULL,
	objectkey BIGINT NOT NULL,
	buffer OID NOT NULL,
	contenttype VARCHAR(255)
);

INSERT INTO "imageresourcetemp" (imagetype, objectkey, buffer, contenttype) SELECT imagetype, objectkey, buffer, contenttype FROM "imageresource"; 

DROP TABLE "imageresource";

CREATE TABLE "imageresource" (
	tkey BIGINT NOT NULL,
	version INTEGER NOT NULL,
	imagetype VARCHAR(255) NOT NULL,
	objectkey BIGINT NOT NULL,
	buffer OID NOT NULL,
	contenttype VARCHAR(255)
);

INSERT INTO "imageresource" (tkey, imagetype, objectkey, buffer, contenttype, version) SELECT tkey, imagetype, objectkey, buffer, contenttype, 0 FROM "imageresourcetemp"; 

DROP TABLE "imageresourcetemp";

ALTER TABLE "imageresource" ADD CONSTRAINT "imageresource_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "imageresourceresource_bk_uidx" ON "imageresource" ("objectkey" ASC, "imagetype" ASC);

UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = ( 
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "localizedresource") WHERE "sequence_name" = 'LocalizedResource';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (
	SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM "imageresource") WHERE "sequence_name" = 'ImageResource';