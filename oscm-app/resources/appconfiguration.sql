# APP SETTINGS 
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'APP_BASE_URL', 'http://127.0.0.1:8180/oscm-app');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'APP_TIMER_INTERVAL', '10000'); 
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'APP_MAIL_RESOURCE','APPMail');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'BSS_WEBSERVICE_URL', 'http://127.0.0.1:8180/{SERVICE}/BASIC?wsdl');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'BSS_USER_KEY', '<userKey>');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'BSS_USER_PWD', '<password>');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'APP_ADMIN_MAIL_ADDRESS', 'admin@est.fujtsu.com');
INSERT INTO configurationsetting (controllerid, settingkey, settingvalue) VALUES ('PROXY', 'APP_KEY_PATH', './key');
