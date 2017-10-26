-- Bug11025: Remove subscription history entries, which are mistakenly created by audit logging after subscription termination 
DELETE FROM "subscriptionhistory" h1 WHERE 
h1.status = 'DEACTIVATED' AND h1.productinstanceid = h1.subscriptionid AND 
h1.deactivationdate = (SELECT deactivationdate FROM "subscriptionhistory" h2 WHERE 
h1.tKey > h2.tkey AND h2.productinstanceid = h1.productinstanceid AND h2.status = 'DEACTIVATED');
