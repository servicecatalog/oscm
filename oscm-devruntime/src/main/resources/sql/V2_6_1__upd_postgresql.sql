-- Bug11025: Remove subscription history entries, which are mistakenly created by audit logging after subscription termination 
DELETE FROM "triggerprocessparameter" tp WHERE tp.name = 'USER_ID';