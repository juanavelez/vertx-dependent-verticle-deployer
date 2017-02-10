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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * <p>Settings for configuring one or more verticle deployments. Each verticle deployment may have
 * zero or more verticles(dependents) that will be deployed upon successful deployment of this
 * verticle.</p>
 * <p>Each verticle may have its own {@link DeploymentOptions} and a list of zero or more
 * dependent {@code DependentDeployment}</p>
 * <p>It provides capabilities to convert from/to JsonObject.</p>
 *
 * @see DeploymentConfiguration
 * 
 * @author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a>
 */
public class DependentsDeployment {
    private List<DeploymentConfiguration> configurations = new ArrayList<>();

    /**
     * Default constructor
     */
    public DependentsDeployment() {
    }

    /**
     * Constructor for creating a instance from JSON
     *
     * @param json  the JSON
     */
    public DependentsDeployment(JsonObject json) {
        fromJson(json);
    }

    /**
     * Returns the list of {@link DeploymentConfiguration}s
     * @return The list of {@link DeploymentConfiguration}s
     */
    public List<DeploymentConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Sets the list of {@link DeploymentConfiguration}s
     * @param configurations The list of {@link DeploymentConfiguration}s
     * @return a reference to this, so the API can be used fluently
     */
    public DependentsDeployment setConfigurations(List<DeploymentConfiguration> configurations) {
        this.configurations = configurations;
        return this;
    }

    /**
     * Populates this object with the information from the supplied JsonObject
     * @param json The JSON Object
     */
    public void fromJson(JsonObject json) {
        if (json.getValue("configurations") instanceof JsonArray) {
            json.getJsonArray("configurations").forEach(item -> {
                if (item instanceof JsonObject) {
                    DeploymentConfiguration cfg = new DeploymentConfiguration();
                    cfg.fromJson((JsonObject) item);
                    getConfigurations().add(cfg);
                }
            });
        }
    }

    /**
     * Returns a JsonObject populated with the information from this object
     * @return The JsonObject
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (this.getConfigurations() != null) {
            JsonArray array = new JsonArray();
            this.getConfigurations().forEach(item -> array.add(item.toJson()));
            json.put("configurations", array);
        }
        return json;
    }
}
