-------------------------------------------------------------------
-- unify description and descriptionurl of product and technicalproduct
-- see RQ 2087 - Insert links and images in service descriptions
-- unification is done in upd_postgresql_02_00_31.sql, here we just remove the columns
-------------------------------------------------------------------

alter table product drop column descriptionurl;
alter table producthistory drop column descriptionurl;
alter table technicalproduct drop column descriptionurl;
alter table technicalproducthistory drop column descriptionurl;