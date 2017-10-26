-------------------------------------------------------------------
-- unify description and descriptionurl of product and technicalproduct
-- see RQ 2087 - Insert links and images in service descriptions
-------------------------------------------------------------------

update
 localizedresource
set
 value = value || ' <a href="' || descriptionurl || '" target="_blank">' ||
 case
  when locale = 'de' then 'Weitere Informationen'
  when locale = 'en' then 'Tell me more'
  when locale = 'ja' then '詳細'
  else descriptionurl
 end
 || '</a>'
from
 product p
where
 objecttype = 'PRODUCT_MARKETING_DESC'
 and objectkey = p.tkey
 and length(p.descriptionurl) > 0;

update
 localizedresource
set
 value = value || ' <a href="' || descriptionurl || '" target="_blank">' ||
 case
  when locale = 'de' then 'Funktionen'
  when locale = 'en' then 'Features'
  when locale = 'ja' then '機能'
  else descriptionurl
 end
 || '</a>'
from
 technicalproduct p
where
 objecttype = 'TEC_PRODUCT_TECHNICAL_DESC'
 and objectkey = p.tkey
 and length(p.descriptionurl) > 0;


/*
MATRIX :
"Description of technical product"		"Description of marketable product"			Result							Reason
----------------------------------------------------------------------------------------------------------------------------------------------
TP1_EN									P1_EN										P1_EN<BR/>TP1_EN				same language. Action = UPDATE
TP1_DE									P1_DE										P1_DE<BR/>TP1_DE				same language. Action = UPDATE
TP1_DE									P1_FR										P1_FR							not same language. Action = UPDATE
NULL									P1_EN										P1_EN							description for tp not set. Action = UPDATE
TP1_EN AND TP1_DE						NULL										TP1_EN							two new localized resources created. Action = INSERT
																					TP1_DE
NULL									NULL										IGNORE							nothing happens
 */

UPDATE localizedresource lr
   SET "value" = 
       (
         lr."value"
         || 
		 COALESCE('<BR/>' ||
         (
	       SELECT 	lr2."value"
	         FROM 	localizedresource AS lr2,
					technicalproduct AS tp2,
					product AS p2
	        WHERE	lr2.objecttype = 'TEC_PRODUCT_TECHNICAL_DESC'
              AND	lr2.objectkey = tp2.tkey
			  AND	tp2.tkey = p2.technicalproduct_tkey
			  AND	p2.tkey = lr.objectkey
			  AND	lr.locale = lr2.locale
	     ), '')
       )
 WHERE lr.objecttype = 'PRODUCT_MARKETING_DESC';

   
INSERT INTO localizedresource (locale, objectkey, objecttype, "value", "version")
SELECT 	lr2.locale, p2.tkey, 'PRODUCT_MARKETING_DESC', lr2."value", '0'
  FROM	localizedresource as lr2,
		technicalproduct AS tp2,
		product AS p2
 WHERE	lr2.objecttype = 'TEC_PRODUCT_TECHNICAL_DESC'
   AND	lr2.objectkey = tp2.tkey
   AND	tp2.tkey = p2.technicalproduct_tkey
   AND	NOT EXISTS (
	  SELECT *
	    FROM localizedresource as lr3
	   WHERE lr3.objecttype = 'PRODUCT_MARKETING_DESC'
	     AND lr3.objectkey = p2.tkey
	     AND lr2.locale = lr3.locale
        );
