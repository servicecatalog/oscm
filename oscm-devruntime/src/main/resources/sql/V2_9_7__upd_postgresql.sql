ALTER TABLE "udadefinition" ALTER COLUMN "defaultvalue" TYPE character varying(400);
ALTER TABLE "udadefinitionhistory" ALTER COLUMN "defaultvalue" TYPE character varying(400);

ALTER TABLE "uda" ALTER COLUMN "udavalue" TYPE character varying(400);
ALTER TABLE "udahistory" ALTER COLUMN "udavalue" TYPE character varying(400);

ALTER TABLE "parameterdefinition" ALTER COLUMN "defaultvalue" TYPE character varying(400);
ALTER TABLE "parameterdefinitionhistory" ALTER COLUMN "defaultvalue" TYPE character varying(400);

ALTER TABLE "parameter" ALTER COLUMN "value" TYPE character varying(400);
ALTER TABLE "parameterhistory" ALTER COLUMN "value" TYPE character varying(400);