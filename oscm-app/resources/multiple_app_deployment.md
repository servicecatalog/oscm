**oscm-app container shall be deploy-able multiple times**

First step:
*	Move into your docker directory

```	vi docker-compose-initdb.yml ```
*	Add a new entry to initialize a second oscm-db: 

```
oscm-db2:
image: servicecatalog/oscm-db:latest
container_name: oscm-db2
env_file: var.env
volumes:
- /docker/data/oscm-db2/data:/var/lib/postgresql/data
```

-	Maybe you have to make the directory manually
-	Add new entry to initialize a second oscm-app
```
oscm-initdb-app2:
image: servicecatalog/oscm-initdb:latest
container_name: oscm-initdb-app2
env_file: var.env
environment:
- TARGET=APP
- SOURCE=INIT
- OVERWRITE=false
- DB_HOST_APP=oscm-db2
links:
- oscm-db2:oscm-db2
```

Second step:

```	vi docker-compose-oscm.yml ```
-	Add a new entry to start a second oscm-db
```
oscm-db2:
image: servicecatalog/oscm-db:latest
container_name: oscm-db2
restart: always
env_file: var.env
volumes:
- /docker/data/oscm-db2/data:/var/lib/postgresql/data
ports:
- 5532:5432
```

-	Add a new entry to start a second oscm-app and link it to the new oscm-db:
```
oscm-app2:
image: servicecatalog/oscm-app:latest
container_name: oscm-app2
restart: always
env_file: var.env
environment:
- JPDA_ADDRESS=8000
- JPDA_TRANSPORT=dt_socket
- DB_HOST_APP=oscm-db2
links:
- oscm-db2:oscm-db2
volumes:
- /docker/config/oscm-app/ssl/privkey:/import/ssl/privkey
- /docker/config/oscm-app/ssl/cert:/import/ssl/cert
- /docker/config/oscm-app/ssl/chain:/import/ssl/chain
- /docker/config/certs:/import/certs
ports:
- 9881:8881
- 9800:8000
```
-	Please make sure that you have added all necessary entries and adjusted the ports

Third step:

-	Initialize the container in the following order:

``` docker-compose -f docker-compose-initdb.yml up -d oscm-db ```

``` docker-compose -f docker-compose-initdb.yml up -d oscm-db2 ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-core ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-jms ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-app ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-app2 ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-controller-openstack ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-controller-aws ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-controller-azure ```

``` docker-compose -f docker-compose-initdb.yml up oscm-initdb-controller-vmware ```


-	Stop all the running container and remove them: 

``` docker-compose -f docker-compose-initdb.yml stop ```

``` docker-compose -f docker-compose-initdb.yml rm ```

-	Now you can start the docker container:

``` docker-compose –f docker-compose-oscm.yml up -d ```





For more information on installing a sample controller, please click the link below:

https://github.com/servicecatalog/oscm/tree/master/oscm-app-sample/
