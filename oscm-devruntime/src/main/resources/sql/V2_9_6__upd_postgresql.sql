ALTER TABLE "udadefinition" ADD COLUMN "encrypted" BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE "udadefinitionhistory" ADD COLUMN "encrypted" BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE "udadefinition" ADD COLUMN "controllerid" character varying(255) DEFAULT NULL;
ALTER TABLE "udadefinitionhistory" ADD COLUMN "controllerid" character varying(255) DEFAULT NULL;