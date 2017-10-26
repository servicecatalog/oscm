-- Bug 9990
INSERT INTO pspsetting (tkey, version, settingkey, settingvalue, psp_tkey) VALUES ((SELECT MAX(tkey) + 1 FROM pspsetting), 0, 'PSP_FRONTEND_JS_PATH', '', 2);
INSERT INTO pspsettinghistory (tkey, objversion, objkey, invocationdate, moddate, modtype, moduser, settingkey, settingvalue, pspobjkey) 
	VALUES ((SELECT MAX(tkey) + 1 FROM pspsettinghistory), 0, (SELECT MAX(tkey) FROM pspsetting), now(), now(), 'ADD', '1000', 'PSP_FRONTEND_JS_PATH', '', 2);
	
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "pspsetting") where "sequence_name" = 'PSPSetting';
UPDATE "hibernate_sequences" SET "sequence_next_hi_value" = (select COALESCE((MAX(tkey)/1000),0)+10 FROM "pspsettinghistory") where "sequence_name" = 'PSPSettingHistory';
	