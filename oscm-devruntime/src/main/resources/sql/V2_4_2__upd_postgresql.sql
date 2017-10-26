-- Bug10208: Remove inconsistent rows in billingresult table
DELETE FROM paymentresult p WHERE p.billingresult_tkey IN (SELECT tkey FROM billingresult WHERE resultxml='');
DELETE FROM gatheredevent g WHERE g.billingresult_tkey IN (SELECT tkey FROM billingresult WHERE resultxml='');
DELETE FROM billingresult WHERE resultxml='';