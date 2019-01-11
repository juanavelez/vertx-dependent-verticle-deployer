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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test DependentsDeployment.
 * 
 * @author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a> 
 */
public class DependentsDeploymentTest {
    @Test
    public void testNoConfigurations() {
        DependentsDeployment depDeployment = new DependentsDeployment();
        assertTrue(depDeployment.getConfigurations() != null && depDeployment.getConfigurations().size() == 0);
    }

    @Test
    public void testOneVerticleWithNoDependentsUsingJson() {
        JsonObject config = new JsonObject();
        config.put("dependents", new JsonArray());
        config.put("name", TestVerticle.class.getCanonicalName());
        JsonArray configurations = new JsonArray();
        configurations.add(config);
        JsonObject depDeploymentAsJson = new JsonObject();
        depDeploymentAsJson.put("configurations", configurations);
        DependentsDeployment depDeployment = new DependentsDeployment(depDeploymentAsJson);

        assertTrue(depDeployment.getConfigurations() != null && depDeployment.getConfigurations().size() == 1);
        assertEquals(depDeployment.getConfigurations().get(0).getName(), TestVerticle.class.getCanonicalName());
        assertEquals(0, depDeployment.getConfigurations().get(0).getDependents().size());
    }

    @Test
    public void testToJsonSameAsDeclarative() {
        DeploymentConfiguration testVerticleCfg = new DeploymentConfiguration();
        testVerticleCfg.setName(TestVerticle.class.getCanonicalName());
        assertNull(testVerticleCfg.getDeploymentID());

        DependentsDeployment depDeployment1 = new DependentsDeployment();
        depDeployment1.getConfigurations().add(testVerticleCfg);

        JsonObject config = new JsonObject();
        config.put("dependents", new JsonArray());
        config.put("name", TestVerticle.class.getCanonicalName());
        JsonArray configurations = new JsonArray();
        configurations.add(config);
        JsonObject depDeploymentAsJson = new JsonObject();
        depDeploymentAsJson.put("configurations", configurations);
        DependentsDeployment depDeployment2 = new DependentsDeployment(depDeploymentAsJson);

        assertEquals(depDeployment1.toJson(), depDeployment2.toJson());
    }
}
