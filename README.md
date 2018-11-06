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
Basic:
* Installed [JDK 8u121](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jdk-8u121-oth-JPR)
or higher.
* [Apache Ivy 2.4.0](http://www.apache.org/dist/ant/ivy/2.4.0/) library.
* [Eclipse ECJ 4.5.1](http://mvnrepository.com/artifact/org.eclipse.jdt.core.compiler/ecj/4.5.1) library.

#### Setting up a workspace
1. Download the latest sources for [this](https://github.com/servicecatalog/oscm). 
2. Import the project into your IDE. You should adjust some of the preferences:
  * Set the compiler level to the installed version of Java 1.8.
  * Set UTF-8 file encoding and Unix line endings.
3. Import and configure the code formatting rules and code templates.
  * Download the files from the [codestyle folder](https://github.com/servicecatalog/oscm/tree/master/oscm-devruntime/javares/codestyle).
  * Import them into your Eclipse IDE ([Help](https://github.com/servicecatalog/oscm/tree/master/oscm-devruntime/javares/codestyle/README.md))
  * Configure the formatting for non-Java files ([Rules and Help](https://github.com/servicecatalog/oscm/tree/master/oscm-devruntime/javares/codestyle/README.md))

#### Setting up the mail server
1. Download and install any mail server.
2. Create any domain and at least one user account in it.

#### Building the application
1. If your network requires a proxy to access the internet you need to specify following arguments to JVM running Ant: 
   ```
   -Dhttp.proxyHost=<proxy-host> 
   -Dhttp.proxyPort=<proxy-port> 
   -Dhttps.proxyHost=<proxy-host>
   -Dhttps.proxyPort=<proxy-port>
   ```
   Fill the placeholders `<proxy-host>` and `<proxy-port>` with the respective host and port where the proxy is provided.

2. Add the following scripts to Ant view in your IDE: `/oscm-devruntime/build-oscmaas.xml`

3. Run targets `Build.LIB`, `Build.BES` and `BUILD.APP`

After the build has finished successfully you'll find the deployable artifacts in`/oscm-build/result/package`. 
You may want to deploy and test your modifications in a running OSCM environment. Simply copy or replace your build artifact into the respective container.

For example:
```
docker cp /workspace/oscm-build/result/package/oscm-app-openstack/oscm-app-openstack.ear oscm-app:/opt/apache-tomee/controllers/
```

#### Deploying the application
Find details [here](https://github.com/servicecatalog/oscm-dockerbuild).
