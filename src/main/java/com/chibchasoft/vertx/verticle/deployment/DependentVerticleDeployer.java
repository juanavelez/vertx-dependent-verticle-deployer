/*
 * Copyright (c) 2017 chibchasoft.com
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Apache License v2.0 which accompanies
 * this distribution.
 *
 *      The Apache License v2.0 is available at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a>
 */
package com.chibchasoft.vertx.verticle.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * <p>Verticle that deploys other verticles which upon successful of themselves may deploy zero or
 * more dependent verticles (and their dependents and so on).</p>
 * <p>If a verticle fails to deploy, none of its dependent verticles are deployed, otherwise all
 * its dependents are deployed at the same time (using either
 * {@link io.vertx.core.Vertx#deployVerticle(String, Handler)} or {@link
 * io.vertx.core.Vertx#deployVerticle(String, DeploymentOptions, Handler)}).</p>
 * <p>The DependentVerticleDeployer completes the future passed to {@link #start(Future)}
 * only and if only ALL deployments (and their dependents and so on) succeed. If any verticle
 * fails to deploy, the future is failed.</p>
 * 
 * @see DependentsDeployment
 * @see DeploymentConfiguration
 * 
 * @author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a> 
 */
public class DependentVerticleDeployer extends AbstractVerticle {
    private final Logger         logger               = LoggerFactory.getLogger(this.getClass());
    private DependentsDeployment dependentsDeployment = null;

    public DependentVerticleDeployer() {
    }

    /**
     * Get the {@link DependentsDeployment} to be deployed
     * @return The {@link DependentsDeployment}
     */
    public DependentsDeployment getDependentsDeployment() {
        return dependentsDeployment;
    }

    /**
     * Sets the {@link DependentsDeployment} to be deployed
     * @param dependentsDeployment The {@link DependentsDeployment}
     */
    public void setDependentsDeployment(DependentsDeployment dependentsDeployment) {
        this.dependentsDeployment = dependentsDeployment;
    }

    @Override
    public void start(Future<Void> startFuture) {
        if (dependentsDeployment == null || dependentsDeployment.getConfigurations().isEmpty()) {
            startFuture.complete();
        } else {
            deployDependentsDeployment(startFuture);
        }
    }

    /**
     * Deploys all the verticles configured to be deployed
     * @param startFuture The future for this verticle that needs to be completed once all
     * verticles (and their dependents) are deployed. The future is failed if any verticle fails
     * to be deployed.
     */
    private void deployDependentsDeployment(Future<Void> startFuture) {
        getCompositeFuture().setHandler(ar -> {
            if (ar.failed()) {
                logger.warn("One or more verticles failed to deploy", ar.cause());
                startFuture.fail(ar.cause());
            } else {
                startFuture.complete();
            }
        });

        deployVerticles(dependentsDeployment);
    }

    /**
     * Gets a composite future using a future for each verticle to be deployed. This composite
     * future is successful if all verticles deployed and fails if any verticle fails to deploy.
     * @return The composite future
     */
    private CompositeFuture getCompositeFuture() {
        List<Future<String>> futures = new ArrayList<>();
        dependentsDeployment.getConfigurations().forEach(cfg -> {
            futures.addAll(getFutures(cfg));
        });

        return CompositeFuture.all(futures.stream().collect(Collectors.toList()));
    }

    /**
     * Adds all futures from {@link DeploymentConfiguration} and its dependents (if any).
     * @param cfg The {@link DeploymentConfiguration}
     */
    private List<Future<String>> getFutures(DeploymentConfiguration cfg) {
        List<Future<String>> futures = new ArrayList<>();
        futures.add(cfg.future);
        for (DependentsDeployment dep : cfg.getDependents()) {
            dep.getConfigurations().forEach(config -> {
                futures.addAll(getFutures(config));
            });
        }
        return futures;
    }

    /**
     * Deployes each of the verticles found in the DependentsDeployment object
     * @param depDeployment The DependentsDeployment object
     */
    private void deployVerticles(DependentsDeployment depDeployment) {
        if (depDeployment != null && !depDeployment.getConfigurations().isEmpty()) {
            depDeployment.getConfigurations().forEach(cfg -> {
                deployConfiguration(cfg);
            });
        }
    }
 
    /**
     * Using this verticle's vertx, deploy a verticle using the
     * {@link DeploymentConfiguration}. If the verticle to deploy has dependents, those
     * dependents will be deployed when the verticle's deployment succeeds (recursively)
     * @param config The {@link DeploymentConfiguration}
     */
    private void deployConfiguration(DeploymentConfiguration config) {
        String verticleName = config.getName();
        if (logger.isDebugEnabled())
            logger.debug("deploying " + verticleName);
        Handler<AsyncResult<String>> deploymentHandler = res -> {
            if (res.succeeded()) {
                config.future.complete(res.result());
                for (DependentsDeployment dep : config.getDependents()) {
                    deployVerticles(dep);
                }
            } else {
                config.future.fail(res.cause());
                logger.warn("deploying verticle " + verticleName + " failed", res.cause());
            }
        };

        if (config.getDeploymentOptions() != null)
            vertx.deployVerticle(verticleName, config.getDeploymentOptions(), deploymentHandler);
        else
            vertx.deployVerticle(verticleName, deploymentHandler);
    }
}
