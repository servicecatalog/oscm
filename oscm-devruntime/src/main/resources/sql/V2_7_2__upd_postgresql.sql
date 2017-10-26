---------------------
-- indexes
---------------------
--create index usergrouptoinvisibleproduct_product on usergrouptoinvisibleproduct (product_tkey desc);
create index usergrouptoinvisibleproduct_usergroup on usergrouptoinvisibleproduct (usergroup_tkey asc);
analyze usergrouptoinvisibleproduct;