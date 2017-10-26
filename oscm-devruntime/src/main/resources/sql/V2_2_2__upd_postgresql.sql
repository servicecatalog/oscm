CREATE TABLE "landingpage" (
  "tkey" BIGINT NOT NULL,
  "numberservices" INTEGER NOT NULL,
  "fillincriterion" VARCHAR(25) NOT NULL,
  "version" INTEGER NOT NULL,
  CONSTRAINT "landingpage_pk" PRIMARY KEY ("tkey")
 );

CREATE TABLE "landingpageproduct" (
  "tkey" BIGINT NOT NULL,
  "landingpage_tkey" BIGINT NOT NULL,
  "product_tkey" BIGINT NOT NULL,
  "position" INTEGER NOT NULL,
  "version" INTEGER NOT NULL,
  CONSTRAINT "landingpageproduct_pk" PRIMARY KEY ("tkey"),
  CONSTRAINT "landingpageproduct_landingpage_fk" FOREIGN KEY ("landingpage_tkey")
      REFERENCES "landingpage" ("tkey")
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT "landingpageproduct_product_fk" FOREIGN KEY ("product_tkey")
      REFERENCES "product" ("tkey")
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE "marketplace" ADD COLUMN "landingpage_tkey" BIGINT;
ALTER TABLE "marketplace" ADD CONSTRAINT "marketplace_landingpage_fk" FOREIGN KEY ("landingpage_tkey") REFERENCES "landingpage" ("tkey") ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO "landingpage" ("tkey", "numberservices", "fillincriterion", "version")
SELECT tkey, 6, 'ACTIVATION_DESCENDING', 0
  FROM "marketplace";

UPDATE "marketplace"
   SET "landingpage_tkey" = "tkey";

ALTER TABLE "marketplace" ALTER COLUMN "landingpage_tkey" SET NOT NULL;
 
INSERT INTO hibernate_sequences("sequence_name", "sequence_next_hi_value") SELECT 'Landingpage', COALESCE((MAX(tkey) / 1000), 0) + 10 FROM landingpage;
INSERT INTO hibernate_sequences("sequence_name", "sequence_next_hi_value") VALUES('LandingpageProduct', 10);
