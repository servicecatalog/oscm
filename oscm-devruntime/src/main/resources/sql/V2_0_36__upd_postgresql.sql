-------------------------------------------------------------------
-- report migration tasks
-------------------------------------------------------------------
UPDATE "report" 
	SET "reportname" = 'Customer_PaymentPreview' 
	WHERE "reportname"='Customer_CurrentBillingDetails';