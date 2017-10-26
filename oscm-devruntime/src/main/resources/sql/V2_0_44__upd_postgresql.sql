----------------------------------------------------------------------------
-- new relation (product <-> payment type)
----------------------------------------------------------------------------
CREATE TABLE producttopaymenttype (
	tkey BIGINT NOT NULL,
	version INTEGER NOT NULL,
	product_tkey BIGINT NOT NULL,
	paymenttype_tkey BIGINT NOT NULL
);

-- add constrains and index
ALTER TABLE producttopaymenttype ADD CONSTRAINT producttopaymenttype_pk PRIMARY KEY (tkey);
ALTER TABLE producttopaymenttype ADD CONSTRAINT producttopaymenttype_product_pk FOREIGN KEY (product_tkey)
	REFERENCES product (tkey);
ALTER TABLE producttopaymenttype ADD CONSTRAINT producttopaymenttype_paymenttype_pk FOREIGN KEY (paymenttype_tkey)
	REFERENCES paymenttype (tkey);

CREATE UNIQUE INDEX producttopaymenttype_bk_idx ON producttopaymenttype (product_tkey asc, paymenttype_tkey asc);

----------------------------------------------------------------------------
CREATE TABLE producttopaymenttypehistory (
	tkey BIGINT NOT NULL,
	moddate TIMESTAMP NOT NULL,
	invocationdate TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
	modtype VARCHAR(255) NOT NULL,
	moduser VARCHAR(255) NOT NULL,
	objkey BIGINT NOT NULL,
	objversion BIGINT NOT NULL,
	productobjkey BIGINT NOT NULL,
	paymenttypeobjkey BIGINT NOT NULL
);	

-- add constrains and index
ALTER TABLE producttopaymenttypehistory ADD CONSTRAINT producttopaymenttypehistory_pk PRIMARY KEY (tkey);



-- update hibernate sequences
INSERT INTO hibernate_sequences (sequence_name, sequence_next_hi_value) 
	SELECT 'ProductToPaymentType', COALESCE((MAX(tkey)/1000),0)+10 FROM producttopaymenttype;
INSERT INTO hibernate_sequences (sequence_name, sequence_next_hi_value) 
	SELECT 'ProductToPaymentTypeHistory', COALESCE((MAX(tkey)/1000),0)+10 FROM producttopaymenttypehistory;

	
----------------------------------------------------------------------------
-- new column to define the default behaviour for the product payment type
----------------------------------------------------------------------------
ALTER TABLE organizationreftopaymenttype ADD COLUMN usedasservicedefault BOOLEAN NOT NULL DEFAULT TRUE;


----------------------------------------------------------------------------
-- add relation to already existing products
----------------------------------------------------------------------------
CREATE TABLE producttopaymenttypetemp (
	tkey serial,
	version INTEGER DEFAULT 0,
    product_tkey BIGINT NOT NULL,
	paymenttype_tkey BIGINT NOT NULL
);
	
INSERT INTO producttopaymenttypetemp(product_tkey, paymenttype_tkey)
SELECT pr.tkey, orgreftopt.paymenttype_tkey
FROM product pr, organizationreference orgref, organizationreftopaymenttype orgreftopt
WHERE (pr.supplierkey = orgref.targetkey AND orgref.referencetype = 'PLATFORM_OPERATOR_TO_SUPPLIER' AND orgref.tkey = orgreftopt.organizationreference_tkey)
ORDER BY pr.tkey, orgreftopt.paymenttype_tkey;

INSERT INTO producttopaymenttype(tkey, version, product_tkey, paymenttype_tkey) 
SELECT tkey, version, product_tkey, paymenttype_tkey FROM producttopaymenttypetemp;


----------------------------------------------------------------------------
-- create history
----------------------------------------------------------------------------
CREATE TABLE producttopaymenttypehistorytemp (
	tkey serial,
	moddate TIMESTAMP NOT NULL,
	invocationdate TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00',
	modtype VARCHAR(255) NOT NULL,
	moduser VARCHAR(255) NOT NULL,
	objkey BIGINT NOT NULL,
	objversion BIGINT NOT NULL,
	productobjkey BIGINT NOT NULL,
	paymenttypeobjkey BIGINT NOT NULL
);

INSERT INTO producttopaymenttypehistorytemp (moddate, modtype, moduser, objkey, objversion, productobjkey, paymenttypeobjkey) 
SELECT now(),'ADD', 'ANONYMOUS', tkey, version, product_tkey, paymenttype_tkey FROM producttopaymenttype;
	
INSERT INTO producttopaymenttypehistory (tkey, moddate, invocationdate, modtype, moduser, objkey, objversion, productobjkey, paymenttypeobjkey) 
SELECT tkey, moddate, invocationdate, modtype, moduser, objkey, objversion, productobjkey, paymenttypeobjkey FROM producttopaymenttypehistorytemp;
	

----------------------------------------------------------------------------
-- drop temporary tables
----------------------------------------------------------------------------
DROP TABLE producttopaymenttypetemp;
DROP TABLE producttopaymenttypehistorytemp;


