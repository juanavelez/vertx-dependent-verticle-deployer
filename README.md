# Vert.x Dependent Verticle Deployer

[![Build Status](https://travis-ci.org/juanavelez/vertx-dependent-verticle-deployer.svg?branch=master)](https://travis-ci.org/juanavelez/vertx-dependent-verticle-deployer)

An implementation of a Vert.x verticle that deploys other verticles and their dependents. This is useful when coordination is needed between several verticles, for example a verticle that serves pages which are populated by another verticle that depends on external resources cannot start until the external resources verticle has successfully started.

## Components

The main driver of deployment is the DependentVerticleDeployer verticle.

### DependentVerticleDeployer

The DependentVerticleDeployer verticle is the main entry point and as such all it does is deploy one or more verticles and their dependents. Those verticles and their dependents may have their own `DeploymentOptions`. The information on what verticles to deploy, their dependents and DeploymentOptions are all encapsulated in a `DependentsDeployment` object.

The DependentVerticleDeployer completes sucessfully its startFuture (`AbstractVerticle::start(Future<String> startFuture`) only when all  verticles (and their dependents and so on) have been successfully deployed. If any verticle fails to deploy, the startFuture is failed. Since the deployment of verticles is done "in parallel" (via `Vertx.deployVerticle(String, Handler)` or `Vertx.deployVerticle(String, DeploymentOptions, Handler)`), it is possible that even though the startFuture has failed, other verticle deployments could still be executing.

**NOTE:** It is strongly suggested that the DependentVerticleDeployer be deployed as a single instance. The DependentVerticleDeployer is not to be intended to be deployed as multiple instances and its behaviour is not guaranteed if such action is taken. This is not to say that verticles in `DependentsDeployment` cannot be deployed as multiple instances (via `DeploymentOptions.setInstances`).

### DependentsDeployment and DeploymentConfiguration

The `DependentsDeployment` is an object encapsulating one or more `DeploymentConfiguration` objects, which itself is an encapsulation of the name of the verticle (including any prefixes) to deploy, any deployment options for such verticle and the list of (any) dependents to deploy. Each `DeploymentConfiguration` object also includes the DeploymentID assigned by Vertx upon successful deployment.

Both `DependentsDeployment` and `DeploymentConfiguration` provide a way to create such objects from a JsonObject (`DependentsDeployment::fromJson` and `DeploymentConfiguration::fromJson`) as well as create a JsonObject from themselves (`DependentsDeployment::toJson` and `DeploymentConfiguration::toJson`).

## Usage ##

Add the vertx-dependent-verticle-deployer dependency to your project, in case of maven like this:

```xml
        <dependency>
            <groupId>com.chibchasoft</groupId>
            <artifactId>vertx-dependent-verticle-deployer</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
 
Configure the verticles to deploy (and their dependents) using both `DependentsDeployment` and `DeploymentConfiguration`:

```java
        // "TheDependentVerticle" needs to be deployed only if "InitialVerticle" is deployed successfully.
        DeploymentConfiguration dependentVerticleCfg = new DeploymentConfiguration();
        dependentVerticleCfg.setName("TheDependentVerticle");
        dependentVerticleCfg.setDeploymentOptions(new DeploymentOptions().setWorker(true).setInstances(2));

        DependentsDeployment innerDepDeployment = new DependentsDeployment();
        innerDepDeployment.getConfigurations().add(dependentVerticleCfg);

        // When "InitialVerticle" succeds its deployment, "TheDependentVerticle" needs to be deployed. 
        DeploymentConfiguration initialVerticleCfg = new DeploymentConfiguration();
        initialVerticleCfg.setName("InitialVerticle");
        // Add "TheDependentVerticle" DependentsDeployment as a dependent of "InitialVerticle"
        initialVerticleCfg.getDependents().add(innerDepDeployment);
        
        // The "OtherVerticle" has no dependents and can be deployed at the same time as the "InitialVerticle"
        DeploymentConfiguration otherVerticleCfg = new DeploymentConfiguration();
        otherVerticleCfg.setName("OtherVerticle");

        initialVerticleCfg.getDependents().add(innerDepDeployment);
        DependentsDeployment dependentsDeployment = new DependentsDeployment();
        dependentsDeployment.getConfigurations().add(initialVerticleCfg);
        dependentsDeployment.getConfigurations().add(otherVerticleCfg);
```

Create a new `DependentVerticleDeployer` instance and set its `depedentsDeployment ` property using the previoulsy configured `DependentsDeployment` object and deploy it:

```java
        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(dependentsDeployment);

        Vertx.vertx().deployVerticle(dependentVerticle, ar -> {
            if (ar.failed()) {
                LOG.warn("Failed to deploy " + dependentsDeployment.toJson().encode(), ar.cause());
            } else {
                LOG.info("InitialVerticle DeploymentID=" + initialVerticleCfg.getDeploymentID());
                LOG.info("OtherVerticle DeploymentID=" + otherVerticleCfg.getDeploymentID());
                LOG.info("TheDependentVerticle DeploymentID=" + dependentVerticleCfg.getDeploymentID());
            }
        });
```
