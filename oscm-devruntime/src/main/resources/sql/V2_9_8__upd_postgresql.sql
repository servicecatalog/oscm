ALTER TABLE "modifieduda" ALTER COLUMN "value" TYPE character varying(400);

ALTER TABLE "modifieduda" ADD COLUMN "encrypted" BOOLEAN NOT NULL DEFAULT FALSE;