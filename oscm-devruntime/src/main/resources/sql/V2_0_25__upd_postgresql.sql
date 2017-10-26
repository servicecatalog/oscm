-----------------------------------------------------
-- Create new table for tagging
-----------------------------------------------------
CREATE TABLE "tag" (
		"tkey" BIGINT NOT NULL,
		"locale" VARCHAR(255) NOT NULL,
		"value" VARCHAR(255) NOT NULL,
		"version" INTEGER NOT NULL
	)
;

CREATE TABLE "technicalproducttag" (
		"tkey" BIGINT NOT NULL,
		"tag_tkey" BIGINT NOT NULL,
		"technicalproduct_tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL
	)
;

ALTER TABLE "tag" ADD CONSTRAINT "tag_pk" PRIMARY KEY ("tkey");
ALTER TABLE "technicalproducttag" ADD CONSTRAINT "technicalproducttag_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "tag_bk_idx" ON "tag" ("locale" asc, "value" asc);
CREATE UNIQUE INDEX "technicalproducttag_uc_idx" ON "technicalproducttag" ("technicalproduct_tkey" asc, "tag_tkey" asc);

ALTER TABLE "technicalproducttag" ADD CONSTRAINT "technicalproducttag_to_tag_fk" FOREIGN KEY ("tag_tkey")
	REFERENCES "tag" ("tkey");	
ALTER TABLE "technicalproducttag" ADD CONSTRAINT "technicalproducttag_to_tp_fk" FOREIGN KEY ("technicalproduct_tkey")
	REFERENCES "technicalproduct" ("tkey");	
	
---------------------
-- Hibernate
---------------------

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Tag', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TechnicalProductTag', 10);
