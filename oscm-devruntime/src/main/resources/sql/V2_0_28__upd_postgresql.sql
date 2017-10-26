-----------------------------------------------------
-- Add the visibility flag to the catalogentry and catalog history
-----------------------------------------------------

ALTER TABLE catalogentry ADD COLUMN visibleincatalog BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE catalogentryhistory ADD COLUMN visibleincatalog BOOLEAN NOT NULL DEFAULT TRUE;