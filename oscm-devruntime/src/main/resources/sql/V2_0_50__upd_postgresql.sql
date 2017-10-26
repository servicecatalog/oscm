ALTER TABLE organization 
ADD CONSTRAINT localmp_fk FOREIGN KEY (localmarketplace_tkey) REFERENCES marketplace (tkey);
