[![Build Status](https://travis-ci.org/servicecatalog/oscm.svg?branch=master)](https://travis-ci.org/servicecatalog/oscm)

<p align="center"><h1><img height="52" src="https://avatars0.githubusercontent.com/u/14330878" alt="Open Service Catalog Manager"/>&nbsp;Open Service Catalog Manager</h1></p> 

Open Service Catalog Manager (OSCM) is an open source application with enterprise quality level. It supports a bright spectrum of use cases, from SaaS Marketplaces to Enterprise IaaS Stores. It offers ready-to-use service provisioning adapters for IaaS providers like Amazon Web Services (AWS) and OpenStack, but is also open for integrating other platforms.

Service Providers can define their services with flexible price models and publish them to an OSCM Marketplaces. The Service Provider can decide on using the OSCM Billing Engine for the service usage cost calculation, or integrate an external one. Customers can subscribe to and use the services.

Find more details on the [OSCM homepage](http://openservicecatalogmanager.org/).

## What's cool?
How about OSCM in containers, easily installed and updated, running in an Kubernetes cluster? OSCM on Apache TomEE with small session footprint, short startup and failover times? OSCM services for provisioning containerized applications with Kubernetes helm charts? 

Find what's new in [this release](https://github.com/servicecatalog/oscm/releases) and what's [coming next](https://openservicecatalogmanager.org/ui/forums/board/17/coming-next).

## Contributions
All contributions are welcome - Open Service Catalog Manager uses the Apache 2.0 license and requires the contributor to agree with the [OSCM Individual CLA (ICLA)](https://github.com/servicecatalog/development/blob/master/ICLA.txt). If the contributor submits patches on behalf of a company, then additionally the [OSCM Corporate CLA (CCLA)](https://github.com/servicecatalog/development/blob/master/CCLA.txt) must be agreed. Even if the contributor is included in such CCLA, she/he is still required to agree with the ICLA. To submit the CLAs please:
* download the [ICLA.txt](https://github.com/servicecatalog/development/blob/master/ICLA.txt) and if needed the [CCLA.txt](https://github.com/servicecatalog/development/blob/master/CCLA.txt)
* fill in the required information and sign them
* scan them as pdf files and email them to secretary-oscm@ml.css.fujitsu.com. We will reply to you as soon as possible.

## Releases
The latest releases can be found [here](https://github.com/servicecatalog/oscm/releases).

## Getting started and building from sources
Please follow this guide from top to bottom, this is the easiest way to avoid errors later on.

#### Prerequisites
* Installed [JDK 8u121](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jdk-8u121-oth-JPR)
or higher.

* [PostgreSQL 9.3](http://www.enterprisedb.com/products-services-training/pgdownload) database installer.
* [tomee-plume-7.0.4](http://tomee.apache.org/download-ng.html) server installer.

#### Setting up a workspace
1. Import the project into your IDE. You should adjust some of the preferences:
  * Set the compiler level to the installed version of Java 1.8.
  * Set UTF-8 file encoding and Unix line endings.

#### Setting up the database
1. Install the database using a path without any whitespaces for the installation directory. During installation, a system-startup service and a database-specific user should be created.
2. Update `<postgres-root-dir>/data/postgresql.conf` properties:

| Property  | Value |  Comment  |
| ------------- | ------------- | ------------- |
| `max_prepared_transactions`  | `50`  |  Sets the maximum number of transactions that can simultaneously be in the "prepared" state.  |
| `max_connections`  | `210`  |  Determines the maximum number of concurrent connections to the database server.  |
| `listen_addresses`  |  `'*'`  |  Specifies the TCP/IP address(es) on which the server is to listen for connections from client applications.  |

3. Update `<postgres-root-dir>/data/pg_hba.conf` properties:

```
host all all 127.0.0.1/32 trust
host all all 0.0.0.0/0 trust
host all all <host-ipv6>/128 trust
```

4. Confirm all changes and restart the PostgreSQL service to apply changes.

#### Setting up the mail server
1. Download and install any mail server.
2. Create any domain and at least one user account in it.

#### Setting up the application server
1. Install the Tomee plume server 7.0.4. 
2. Configure it -TBD

#### Building the application
1. Add the following arguments to JVM running Ant: `-Dhttp.proxyHost=<proxy-host> -Dhttp.proxyPort=8080`.
2. Add the following scripts to Ant view in your IDE: `/oscm-devruntime/build-oscmaas.xml`
3. Run targets Build.LIB and Build.BES

#### Deploying the application
Find details [here](https://github.com/servicecatalog/oscm-dockerbuild).


