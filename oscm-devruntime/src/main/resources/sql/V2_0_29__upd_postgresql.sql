-----------------------------------------------------
-- create tables: ProductReview, ProductReviewHistory, ProductFeedback
-----------------------------------------------------

CREATE TABLE "productreview" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"rating" INTEGER NOT NULL,
		"title" VARCHAR(255) NOT NULL,
        "comment" VARCHAR(2000) NOT NULL,
        "modificationdate" BIGINT NOT NULL,
        "platformuser_tkey" BIGINT NOT NULL,
        "productfeedback_tkey" BIGINT NOT NULL
);

CREATE TABLE "productreviewhistory" (
		"tkey" BIGINT NOT NULL,
        "objversion" BIGINT NOT NULL,
        "objkey" BIGINT NOT NULL,
        "invocationdate" TIMESTAMP NOT NULL,
        "moddate" TIMESTAMP NOT NULL,
        "modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"rating" INTEGER NOT NULL,
		"title" VARCHAR(255) NOT NULL,
        "comment" VARCHAR(2000) NOT NULL,
        "modificationdate" BIGINT NOT NULL,
        "platformuserobjkey" BIGINT NOT NULL,
        "productfeedbackobjkey" BIGINT NOT NULL
);

CREATE TABLE "productfeedback" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"averagerating" NUMERIC(3,2) NOT NULL,
        "product_tkey" BIGINT NOT NULL
);

---------------------
-- primary keys
---------------------

ALTER TABLE "productreview" ADD CONSTRAINT "productreview_pk" PRIMARY KEY ("tkey");
ALTER TABLE "productreviewhistory" ADD CONSTRAINT "productreviewhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "productfeedback" ADD CONSTRAINT "productfeedback_pk" PRIMARY KEY ("tkey");

---------------------
-- foreign keys
---------------------

ALTER TABLE "productreview" ADD CONSTRAINT "productreview_platformuser_fk" FOREIGN KEY ("platformuser_tkey")
	REFERENCES "platformuser" ("tkey");
ALTER TABLE "productreview" ADD CONSTRAINT "productreview_productfeedback_fk" FOREIGN KEY ("productfeedback_tkey")
	REFERENCES "productfeedback" ("tkey");
	
ALTER TABLE "productfeedback" ADD CONSTRAINT "productfeedback_product_fk" FOREIGN KEY ("product_tkey")
	REFERENCES "product" ("tkey");
	
---------------------
-- indexes
---------------------
	
CREATE UNIQUE INDEX "productreview_usrrts_uidx" ON "productreview" ("platformuser_tkey" ASC, "productfeedback_tkey" ASC);

---------------------
-- Hibernate
---------------------

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductReview', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductReviewHistory', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('ProductFeedback', 10);
