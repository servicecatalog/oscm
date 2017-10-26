------------------------------------------------------------
-- Modifications for FCIP. New landing page for enterprise 
------------------------------------------------------------
ALTER TABLE landingpage RENAME TO "publiclandingpage";
ALTER TABLE marketplace RENAME COLUMN "landingpage_tkey" TO "publiclandingpage_tkey";
ALTER TABLE marketplace ALTER COLUMN "publiclandingpage_tkey" DROP NOT NULL;
UPDATE hibernate_sequences SET sequence_name = 'PublicLandingpage' WHERE sequence_name = 'Landingpage';

CREATE TABLE "enterpriselandingpage" (
  "tkey" BIGINT NOT NULL,
  "version" INTEGER NOT NULL,
  CONSTRAINT "enterpriselandingpage_pk" PRIMARY KEY ("tkey")
 );
ALTER TABLE "marketplace" ADD COLUMN "enterpriselandingpage_tkey" BIGINT DEFAULT NULL;
ALTER TABLE "marketplace" ADD CONSTRAINT "marketplace_enterpriselandingpage_fk" FOREIGN KEY ("enterpriselandingpage_tkey") REFERENCES "enterpriselandingpage" ("tkey");
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('EnterpriseLandingpage', 10);