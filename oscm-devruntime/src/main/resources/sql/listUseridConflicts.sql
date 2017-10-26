SELECT puser.userid, puser.firstname, puser.lastname, puser.email, org.name AS organizationname, org.organizationid
FROM platformuser puser, organization org
WHERE (puser.organizationkey=org.tkey) AND
      ((SELECT COUNT(*) FROM platformuser iuser WHERE iuser.userid=puser.userid) > 1)
