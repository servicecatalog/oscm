ALTER TABLE "tenant" DROP COLUMN "idp";
DROP TABLE "tenantsetting"; 
DELETE FROM hibernate_sequences WHERE sequence_name = 'TenantSetting';