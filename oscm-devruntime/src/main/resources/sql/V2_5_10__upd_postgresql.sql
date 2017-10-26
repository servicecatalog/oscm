----------------------------------------------------------------------
-- Price model flag for indicating finish of asynchronous provisioning
----------------------------------------------------------------------
ALTER TABLE pricemodel ADD "provisioningcompleted" BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE pricemodelhistory ADD "provisioningcompleted" BOOLEAN NOT NULL DEFAULT TRUE;