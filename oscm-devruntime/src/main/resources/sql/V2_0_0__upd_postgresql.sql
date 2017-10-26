----------------------------------------------------------
-- Starting new sql scripts for release 15.0 with this one
----------------------------------------------------------

CREATE TABLE "organizationtocountry" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"organization_tkey" BIGINT,
		"supportedcountry_tkey" BIGINT 
	)
;

CREATE TABLE "organizationtocountryhistory" (
		"tkey" BIGINT NOT NULL,
		"moddate" TIMESTAMP NOT NULL,
		"modtype" VARCHAR(255) NOT NULL,
		"moduser" VARCHAR(255) NOT NULL,
		"objkey" BIGINT NOT NULL,
		"objversion" BIGINT NOT NULL,
		"organizationobjkey" BIGINT,
		"supportedcountryobjkey" BIGINT
	)
;

ALTER TABLE "organization" ADD COLUMN "domicilecountry_tkey" BIGINT;
ALTER TABLE "organizationhistory" ADD COLUMN "domicilecountryobjkey" BIGINT;

CREATE TABLE "supportedcountry" (
		"tkey" BIGINT NOT NULL,
		"version" INTEGER NOT NULL,
		"countryisocode" VARCHAR(2)
	)
;

---------------------
-- indexes
---------------------

CREATE UNIQUE INDEX "organizationtocountry_orgctr_nuidx" ON "organizationtocountry" ("organization_tkey" asc, "supportedcountry_tkey" asc);

---------------------
-- primary keys
---------------------

ALTER TABLE "organizationtocountry" ADD CONSTRAINT "organizationtocountry_pk" PRIMARY KEY ("tkey");
ALTER TABLE "organizationtocountryhistory" ADD CONSTRAINT "organizationtocountryhistory_pk" PRIMARY KEY ("tkey");
ALTER TABLE "supportedcountry" ADD CONSTRAINT "supportedcountry_pk" PRIMARY KEY ("tkey");


---------------------
-- foreign keys
---------------------

ALTER TABLE "organizationtocountry" ADD CONSTRAINT "organizationtocountry_organization_fk" FOREIGN KEY ("organization_tkey")
	REFERENCES "organization" ("tkey");
ALTER TABLE "organizationtocountry" ADD CONSTRAINT "organizationtocountry_supportedcountry_fk" FOREIGN KEY ("supportedcountry_tkey")
	REFERENCES "supportedcountry" ("tkey");



INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('0','0','AD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('1','0','AE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('2','0','AF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('3','0','AG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('4','0','AI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('5','0','AL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('6','0','AM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('7','0','AN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('8','0','AO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('9','0','AQ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('10','0','AR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('11','0','AS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('12','0','AT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('13','0','AU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('14','0','AW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('15','0','AX');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('16','0','AZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('17','0','BA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('18','0','BB');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('19','0','BD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('20','0','BE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('21','0','BF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('22','0','BG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('23','0','BH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('24','0','BI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('25','0','BJ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('26','0','BM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('27','0','BN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('28','0','BO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('29','0','BR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('30','0','BS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('31','0','BT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('32','0','BV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('33','0','BW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('34','0','BY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('35','0','BZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('36','0','CA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('37','0','CC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('38','0','CD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('39','0','CF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('40','0','CG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('41','0','CH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('42','0','CI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('43','0','CK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('44','0','CL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('45','0','CM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('46','0','CN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('47','0','CO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('48','0','CR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('49','0','CS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('50','0','CU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('51','0','CV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('52','0','CX');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('53','0','CY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('54','0','CZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('55','0','DE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('56','0','DJ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('57','0','DK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('58','0','DM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('59','0','DO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('60','0','DZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('61','0','EC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('62','0','EE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('63','0','EG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('64','0','EH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('65','0','ER');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('66','0','ES');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('67','0','ET');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('68','0','FI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('69','0','FJ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('70','0','FK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('71','0','FM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('72','0','FO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('73','0','FR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('74','0','GA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('75','0','GB');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('76','0','GD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('77','0','GE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('78','0','GF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('79','0','GH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('80','0','GI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('81','0','GL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('82','0','GM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('83','0','GN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('84','0','GP');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('85','0','GQ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('86','0','GR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('87','0','GS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('88','0','GT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('89','0','GU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('90','0','GW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('91','0','GY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('92','0','HK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('93','0','HM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('94','0','HN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('95','0','HR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('96','0','HT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('97','0','HU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('98','0','ID');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('99','0','IE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('100','0','IL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('101','0','IN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('102','0','IO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('103','0','IQ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('104','0','IR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('105','0','IS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('106','0','IT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('107','0','JM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('108','0','JO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('109','0','JP');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('110','0','KE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('111','0','KG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('112','0','KH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('113','0','KI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('114','0','KM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('115','0','KN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('116','0','KP');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('117','0','KR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('118','0','KW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('119','0','KY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('120','0','KZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('121','0','LA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('122','0','LB');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('123','0','LC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('124','0','LI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('125','0','LK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('126','0','LR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('127','0','LS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('128','0','LT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('129','0','LU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('130','0','LV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('131','0','LY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('132','0','MA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('133','0','MC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('134','0','MD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('135','0','MG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('136','0','MH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('137','0','MK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('138','0','ML');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('139','0','MM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('140','0','MN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('141','0','MO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('142','0','MP');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('143','0','MQ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('144','0','MR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('145','0','MS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('146','0','MT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('147','0','MU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('148','0','MV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('149','0','MW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('150','0','MX');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('151','0','MY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('152','0','MZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('153','0','NA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('154','0','NC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('155','0','NE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('156','0','NF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('157','0','NG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('158','0','NI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('159','0','NL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('160','0','NO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('161','0','NP');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('162','0','NR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('163','0','NU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('164','0','NZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('165','0','OM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('166','0','PA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('167','0','PE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('168','0','PF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('169','0','PG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('170','0','PH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('171','0','PK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('172','0','PL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('173','0','PM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('174','0','PN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('175','0','PR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('176','0','PS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('177','0','PT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('178','0','PW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('179','0','PY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('180','0','QA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('181','0','RE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('182','0','RO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('183','0','RU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('184','0','RW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('185','0','SA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('186','0','SB');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('187','0','SC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('188','0','SD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('189','0','SE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('190','0','SG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('191','0','SH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('192','0','SI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('193','0','SJ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('194','0','SK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('195','0','SL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('196','0','SM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('197','0','SN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('198','0','SO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('199','0','SR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('200','0','ST');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('201','0','SV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('202','0','SY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('203','0','SZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('204','0','TC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('205','0','TD');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('206','0','TF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('207','0','TG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('208','0','TH');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('209','0','TJ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('210','0','TK');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('211','0','TL');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('212','0','TM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('213','0','TN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('214','0','TO');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('215','0','TR');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('216','0','TT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('217','0','TV');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('218','0','TW');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('219','0','TZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('220','0','UA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('221','0','UG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('222','0','UM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('223','0','US');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('224','0','UY');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('225','0','UZ');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('226','0','VA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('227','0','VC');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('228','0','VE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('229','0','VG');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('230','0','VI');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('231','0','VN');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('232','0','VU');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('233','0','WF');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('234','0','WS');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('235','0','YE');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('236','0','YT');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('237','0','ZA');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('238','0','ZM');
INSERT INTO "supportedcountry" ("tkey", "version","countryisocode") VALUES ('239','0','ZW');


------------------
-- Migrate: Fill organizationtocountry for all existing organizations
------------------

create table temp (tkey serial, version integer,  countrykey bigint, orgkey bigint );
insert into temp ("version", "countrykey", "orgkey") Select 0, s.tkey, o.tkey from "supportedcountry" s, "organization" o order by o.tkey asc, s.tkey asc;
insert into organizationtocountry ("tkey", "version", "supportedcountry_tkey", "organization_tkey") select tkey, version, countrykey, orgkey from temp;
insert into organizationtocountryhistory ("tkey", "moddate", "modtype", "moduser", "objkey", "objversion", "organizationobjkey", "supportedcountryobjkey" ) select tkey, now(), 'ADD', '1000', tkey, 0, orgkey, countrykey from temp;
drop table temp;

------------------
-- Hibernate sequence
------------------

insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") values ('SupportedCountry','10');
insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'OrganizationToCountry', COALESCE((MAX(tkey)/1000),0)+10 from organizationtocountry;
insert into hibernate_sequences ("sequence_name", "sequence_next_hi_value") select 'OrganizationToCountryHistory', COALESCE((MAX(tkey)/1000),0)+10 from organizationtocountryhistory;

