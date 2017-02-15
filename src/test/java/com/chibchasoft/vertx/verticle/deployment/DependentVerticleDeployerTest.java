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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.vertx.core.DeploymentOptions;
import io.vertx.test.core.VertxTestBase;

/**
 * Test DependentVerticleDeployer.
 * 
 * @author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a> 
 */
public class DependentVerticleDeployerTest extends VertxTestBase {
    /**
     * In this test, the DependentVerticleDeployer is deployed but itself has nothing to deploy
     */
    @Test
    public void testNothingToDeploy() {
        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        assertNull(dependentVerticle.getDependentsDeployment());

        vertx.deployVerticle(dependentVerticle, ar -> {
            assertTrue(ar.succeeded());
            assertNotNull(ar.result());
            assertNull(dependentVerticle.getDependentsDeployment());
            testComplete();
        });
        await();
    }

    /**
     * In this test, the DependentVerticleDeployer deploys a single verticle that has no dependents
     */
    @Test
    public void testOneVerticleWithNoDependents() {
        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertTrue(ar.succeeded());
                                 assertNotNull(ar.result());
                                 assertTrue(testVerticleCfg.succeeded() &&
                                            testVerticleCfg.failCause() == null &&
                                            testVerticleCfg.getDeploymentID() != null);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys a single non-existent verticle that
     * has no dependents
     */
    @Test
    public void testOneNonExistentVerticleWithNoDependents() {
        DeploymentConfiguration iDontExistVerticleCfg = new DeploymentConfiguration();
        iDontExistVerticleCfg.setName("IDon'tExist");
        assertNull(iDontExistVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(iDontExistVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertFalse(ar.succeeded());
                                 assertTrue(iDontExistVerticleCfg.failed() &&
                                            iDontExistVerticleCfg.failCause() != null &&
                                            iDontExistVerticleCfg.getDeploymentID() == null);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys a single verticle with several instances
     * that has no dependents
     */
    @Test
    public void testOneVerticleSeveralInstancesWithNoDependents() {
        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        testVerticleCfg.setDeploymentOptions(new DeploymentOptions().setInstances(3));
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        AtomicInteger deployCount = new AtomicInteger();
        vertx.eventBus().consumer("TestVerticleStarted", m -> {
            deployCount.incrementAndGet();
        });

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertTrue(ar.succeeded());
                                 assertNotNull(ar.result());
                                 assertTrue(testVerticleCfg.succeeded() &&
                                            testVerticleCfg.failCause() == null &&
                                            testVerticleCfg.getDeploymentID() != null);
                                 // The DependentVerticleDeployer and TestVerticle
                                 assertEquals(2, vertx.deploymentIDs().size());
                                 waitUntil(() -> deployCount.get() == 3);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys the same verticle twice but none
     * of the verticles has dependents
     */
    @Test
    public void testSameVerticleTwiceWithNoDependents() {
        DeploymentConfiguration testVerticleCfg1 = new DeploymentConfiguration();
        testVerticleCfg1.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg1.getDeploymentID());

        DeploymentConfiguration testVerticleCfg2 = new DeploymentConfiguration();
        testVerticleCfg2.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg2.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg1);
        depDeployment.getConfigurations().add(testVerticleCfg2);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        AtomicInteger deployCount = new AtomicInteger();
        vertx.eventBus().consumer("TestVerticleStarted", m -> {
            deployCount.incrementAndGet();
        });

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertTrue(ar.succeeded());
                                 assertNotNull(ar.result());
                                 assertTrue(testVerticleCfg1.succeeded() &&
                                            testVerticleCfg1.failCause() == null &&
                                            testVerticleCfg1.getDeploymentID() != null);
                                 assertTrue(testVerticleCfg2.succeeded() &&
                                            testVerticleCfg2.failCause() == null &&
                                            testVerticleCfg2.getDeploymentID() != null);
                                 // The DependentVerticleDeployer and TestVerticle twice
                                 assertEquals(3, vertx.deploymentIDs().size());
                                 waitUntil(() -> deployCount.get() == 2);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys two verticles (each with no dependents)
     * but first one fails
     */
    @Test
    public void testTwoVerticlesWithNoDependentsDeployedInSequenceButFirstOneFails() {
        DeploymentConfiguration iDontExistVerticleCfg = new DeploymentConfiguration();
        iDontExistVerticleCfg.setName("IDon'tExist");
        assertNull(iDontExistVerticleCfg.getDeploymentID());

        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(iDontExistVerticleCfg);
        depDeployment.getConfigurations().add(testVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertFalse(ar.succeeded());
                                 assertNull(ar.result());
                                 assertTrue(iDontExistVerticleCfg.failed() &&
                                            iDontExistVerticleCfg.failCause() != null &&
                                            iDontExistVerticleCfg.failCause().toString().toLowerCase().contains("classnotfoundexception") &&
                                            iDontExistVerticleCfg.getDeploymentID() == null);
                                 waitUntil(() -> testVerticleCfg.succeeded() &&
                                                 testVerticleCfg.failCause() == null &&
                                                 testVerticleCfg.getDeploymentID() != null);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys two verticles (each with no dependents)
     * in sequence but last one fails
     */
    @Test
    public void testTwoVerticlesWithNoDependentsDeployedInSequenceButLastOneFails() {
        DeploymentConfiguration iDontExistVerticleCfg = new DeploymentConfiguration();
        iDontExistVerticleCfg.setName("IDon'tExist");
        assertNull(iDontExistVerticleCfg.getDeploymentID());

        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg);
        depDeployment.getConfigurations().add(iDontExistVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertFalse(ar.succeeded());
                                 assertNull(ar.result());
                                 assertTrue(iDontExistVerticleCfg.failed() &&
                                            iDontExistVerticleCfg.failCause() != null &&
                                            iDontExistVerticleCfg.failCause().toString().toLowerCase().contains("classnotfoundexception") &&
                                            iDontExistVerticleCfg.getDeploymentID() == null);
                                 waitUntil(() -> testVerticleCfg.succeeded() &&
                                                 testVerticleCfg.failCause() == null &&
                                                 testVerticleCfg.getDeploymentID() != null);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys a single verticle that has one dependent
     */
    @Test
    public void testOneVerticleWithOneDependent() {
        DeploymentConfiguration dependentTestVerticleCfg = new DeploymentConfiguration();
        dependentTestVerticleCfg.setName(DependentTestVerticle.class.getCanonicalName());
        assertNull(dependentTestVerticleCfg.getDeploymentID());

        DependentsDeployment innerDepDeployment = new DependentsDeployment();
        innerDepDeployment.getConfigurations().add(dependentTestVerticleCfg);

        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        testVerticleCfg.getDependents().add(innerDepDeployment);
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertTrue(ar.succeeded());
                                 assertNotNull(ar.result());
                                 assertTrue(dependentTestVerticleCfg.succeeded() &&
                                            dependentTestVerticleCfg.failCause() == null &&
                                            dependentTestVerticleCfg.getDeploymentID() != null);
                                 assertTrue(testVerticleCfg.succeeded() &&
                                            testVerticleCfg.failCause() == null &&
                                            testVerticleCfg.getDeploymentID() != null);
                                 testComplete();
                             });
        await();
    };

    /**
     * In this test, the DependentVerticleDeployer deploys a single verticle that has one dependent
     * but such dependent verticle does not exist
     */
    @Test
    public void testOneVerticleWithNonExistentDependent() {
        DeploymentConfiguration iDontExistVerticleCfg = new DeploymentConfiguration();
        iDontExistVerticleCfg.setName("IDon'tExist");
        assertNull(iDontExistVerticleCfg.getDeploymentID());

        DependentsDeployment innerDepDeployment = new DependentsDeployment();
        innerDepDeployment.getConfigurations().add(iDontExistVerticleCfg);

        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        testVerticleCfg.getDependents().add(innerDepDeployment);
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment = new DependentsDeployment();
        depDeployment.getConfigurations().add(testVerticleCfg);

        DependentVerticleDeployer dependentVerticle = new DependentVerticleDeployer();
        dependentVerticle.setDependentsDeployment(depDeployment);

        vertx.deployVerticle(dependentVerticle,
                             ar -> {
                                 assertFalse(ar.succeeded());
                                 assertNull(ar.result());
                                 assertTrue(testVerticleCfg.succeeded() &&
                                            testVerticleCfg.failCause() == null &&
                                            testVerticleCfg.getDeploymentID() != null);
                                 assertTrue(iDontExistVerticleCfg.failed() &&
                                            iDontExistVerticleCfg.failCause() != null &&
                                            iDontExistVerticleCfg.failCause().toString().toLowerCase().contains("classnotfoundexception") &&
                                            iDontExistVerticleCfg.getDeploymentID() == null);
                                 testComplete();
                             });
        await();
    };
}
