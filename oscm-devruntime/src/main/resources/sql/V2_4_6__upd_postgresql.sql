ALTER TABLE triggerprocessparameter ADD COLUMN serialized boolean NOT NULL DEFAULT false;

CREATE INDEX subscriptionhistory_objkey_idx ON subscriptionhistory (objkey);
