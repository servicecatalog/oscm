CREATE TABLE "platformsetting" (
		"tkey" BIGINT NOT NULL,
		"settingtype" VARCHAR(255) NOT NULL,
		"settingvalue" VARCHAR(255),
		"version" INTEGER NOT NULL
	);	

ALTER TABLE "platformsetting" ADD CONSTRAINT "platformsetting_pk" PRIMARY KEY ("tkey");

INSERT INTO "hibernate_sequences" ("sequence_name", "sequence_next_hi_value") VALUES ('PlatformSetting', 10);

CREATE UNIQUE INDEX "platformsetting_type_uidx" ON "platformsetting" ("settingtype");
CREATE UNIQUE INDEX "organizationsetting_orgtype_uidx" ON "organizationsetting" ("organization_tkey", "settingtype");

ALTER TABLE "organization" ADD COLUMN remoteldapactive BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "organizationhistory" ADD COLUMN remoteldapactive BOOLEAN NOT NULL DEFAULT false;

UPDATE "organization" org SET "remoteldapactive" = true WHERE EXISTS (SELECT "tkey" FROM "organizationsetting" WHERE "organization_tkey" = org.tkey);
UPDATE "organizationhistory" orghist SET "remoteldapactive" = true WHERE EXISTS (SELECT "tkey" FROM "organization" org WHERE org.tkey = orghist.objkey AND org.remoteldapactive = true);