CREATE TABLE "triggerprocessidentifier" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"triggerprocess_tkey" BIGINT NOT NULL,
		"name" VARCHAR(255) NOT NULL,
		"value" VARCHAR(255) NOT NULL
	)
;

ALTER TABLE "triggerprocessidentifier" ADD CONSTRAINT "triggerprocessidentifier_pk" PRIMARY KEY ("tkey");

ALTER TABLE "triggerprocessidentifier" ADD CONSTRAINT "triggerprocessidentifier_triggerprocess_fk" 
	FOREIGN KEY ("triggerprocess_tkey") REFERENCES "triggerprocess" ("tkey");
	
CREATE INDEX "triggerprocessidentifier_identity_nuidx" ON "triggerprocessidentifier" 
	("triggerprocess_tkey" asc, "name" asc, "value" asc);

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES('TriggerProcessIdentifier', 10);