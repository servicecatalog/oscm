---------------------------------------------------------------
-- Bug 9811 - remove deprecated settings
---------------------------------------------------------------
DELETE FROM "configurationsetting" 
  WHERE information_id='MAIL_SERVER' 
    OR information_id='MAIL_PORT'
    OR information_id='MAIL_USER'
    OR information_id='MAIL_USER_PWD'
    OR information_id='MAIL_RESPONSE_ADDRESS'
    OR information_id='SEARCH_INDEX_MASTER_HOST'
    OR information_id='SEARCH_INDEX_MASTER_PORT';
    
