CREATE TABLE "lrtemp" (
		"locale" VARCHAR(255) NOT NULL,
		"objectkey" BIGINT NOT NULL,
		"objecttype" VARCHAR(255) NOT NULL,
		"value" TEXT NOT NULL,
		"version" INTEGER NOT NULL DEFAULT 0
	);	
	
INSERT INTO "lrtemp" ("locale", "objectkey", "objecttype", "value") 
	SELECT lr."locale", pm."tkey", 'PRICEMODEL_LICENSE' AS "objecttype", lr."value" 
	FROM "pricemodel" pm, "localizedresource" lr , "product" p, "technicalproduct" tp
	WHERE lr."objecttype" = 'PRODUCT_LICENSE_DESC'
		AND lr."objectkey" = tp."tkey"
		AND tp."tkey" = p."technicalproduct_tkey"
		AND p."pricemodel_tkey" = pm."tkey";

INSERT INTO "localizedresource" ("locale", "objectkey", "objecttype", "value") 
	SELECT "locale", "objectkey", "objecttype", "value" 
	FROM "lrtemp";

DROP TABLE "lrtemp";
