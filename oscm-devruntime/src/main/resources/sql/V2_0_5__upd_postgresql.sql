UPDATE "parameter" param 
	SET "value" = CEILING( CAST(param."value" AS BIGINT) / CAST (86400000 AS REAL)) * 86400000
	WHERE EXISTS (SELECT paramdef."tkey" 
					FROM "parameterdefinition" paramdef 
					WHERE paramdef."tkey" = param."parameterdefinitionkey" 
						AND paramdef."valuetype" = 'DURATION'
						AND param."value" IS NOT NULL);
						
UPDATE "parameterhistory" paramh 
	SET "value" = CEILING( CAST(paramh."value" AS BIGINT) / CAST (86400000 AS REAL)) * 86400000
	WHERE EXISTS (SELECT paramdefh."tkey" 
					FROM "parameterdefinitionhistory" paramdefh 
					WHERE paramdefh."objkey" = paramh."parameterdefinitionobjkey" 
						AND paramdefh."valuetype" = 'DURATION'
						AND paramh."value" IS NOT NULL);