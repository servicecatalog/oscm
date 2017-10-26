------------------------------------------------------------------------------
-- configurationsetting TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION migration tasks
------------------------------------------------------------------------------
UPDATE "configurationsetting" 
	SET "env_value"='86400000' 
	WHERE "information_id"='TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION' and "env_value"='0';