CREATE TABLE "category" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"marketplacekey" BIGINT NOT NULL,
		"categoryid" VARCHAR(255) NOT NULL
	)
;

CREATE TABLE "categoryhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"invocationdate" TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
		"marketplaceobjkey" BIGINT NOT NULL,
		"categoryid" VARCHAR(255) NOT NULL
	)
;

ALTER TABLE "category" ADD CONSTRAINT "category_pk" PRIMARY KEY ("tkey");

ALTER TABLE "categoryhistory" ADD CONSTRAINT "categoryhistory_pk" PRIMARY KEY ("tkey");

CREATE UNIQUE INDEX "category_marketplace_id_unidx" ON "category" ("marketplacekey", "categoryid");

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('Category', 10);
INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('CategoryHistory', 10);
