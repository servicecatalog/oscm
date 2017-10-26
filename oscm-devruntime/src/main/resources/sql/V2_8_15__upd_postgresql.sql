-- add new column forallusers to usergrouptoinvisibleproduct
ALTER TABLE usergrouptoinvisibleproduct ADD COLUMN forallusers BOOLEAN NOT NULL DEFAULT TRUE;
