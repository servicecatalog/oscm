-- Bug 9505
DROP INDEX billingresult_orgkey_date_nuidx;
CREATE UNIQUE INDEX billingresult_orgkey_date_sub_uidx ON billingresult (organizationtkey, periodstarttime, periodendtime, subscriptionkey);
 
-- Bug 9407
UPDATE localizedresource SET value='Bericht zur Nutzung externer Services' WHERE locale='de' AND objectkey=14 AND objecttype='REPORT_DESC';

-- Bug 9526
CREATE UNIQUE INDEX session_bk_idx ON session (sessionid, subscriptiontkey, sessiontype);
CREATE UNIQUE INDEX report_bk_idx ON report (reportname);
CREATE UNIQUE INDEX supportedcountry_bk_idx ON supportedcountry (countryisocode);
  
-- Bug 9570
UPDATE localizedresource SET value='Bericht über Abo-Nutzung' WHERE locale='de' AND objectkey=0 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='User report' WHERE locale='en' AND objectkey=0 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='Bericht über abrechenbare Ereignisse' WHERE locale='de' AND objectkey=1 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='Billable event report' WHERE locale='en' AND objectkey=1 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='Bericht über Nutzung der technischen Services' WHERE locale='de' AND objectkey=4 AND objecttype='REPORT_DESC';
UPDATE localizedresource SET value='Technical service usage report' WHERE locale='en' AND objectkey=4 AND objecttype='REPORT_DESC';
