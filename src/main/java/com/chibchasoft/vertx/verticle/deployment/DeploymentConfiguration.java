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
import java.util.Objects;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * The configuration to deploy a verticle and its dependents
 * 
 * @author <a href="mailto:jvelez@chibchasoft.com">Juan Velez</a> 
 */
public class DeploymentConfiguration {
    private String                     name;
    private DeploymentOptions          deploymentOptions;
    private List<DependentsDeployment> dependents = new ArrayList<>();

    Future<String>                     future     = Future.future();

    public DeploymentConfiguration() {
        
    }

    /**
     * The name of this verticle. This is the name needed to be deployed including any prefixes
     * @return The name of the verticle to be deployed
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this verticle. This is the name needed to be deployed including any prefixes
     * @param name The name of the verticle to be deployed
     * @return a reference to this, so the API can be used fluently
     */
    public DeploymentConfiguration setName(String name) {
        Objects.requireNonNull(name, "Name is required");
        this.name = name;
        return this;
    }

    /**
     * Get the deployment options for this verticle
     * @return The deployment options for this verticle
     */
    public DeploymentOptions getDeploymentOptions() {
        return deploymentOptions;
    }

    /**
     * Sets the deployment options for this verticle
     * @param deploymentOptions The deployment options for this verticle
     * @return a reference to this, so the API can be used fluently
     */
    public DeploymentConfiguration setDeploymentOptions(DeploymentOptions deploymentOptions) {
        Objects.requireNonNull(deploymentOptions, "deploymentOptions is required");
        this.deploymentOptions = deploymentOptions;
        return this;
    }

    /**
     * Get the Deployment ID for this verticle
     * @return The Deployment ID for this verticle, null if it has not been deployed yet or failed to deploy
     */
    public String getDeploymentID() {
        return future.result();
    }

    /**
     * Get the list of deployments which depend on the successful deployment of this
     * verticle.
     * @return The (non-null) list of deployments
     */
    public List<DependentsDeployment> getDependents() {
        return dependents;
    }

    /**
     * Has the deployments completed? It's completed if it's either succeeded or failed.
     * @return true if completed, false if not
     */
    public boolean isComplete() {
        return future.isComplete();
    }

    /**
     * Did it succeed?
     * @return true if it succeeded or false otherwise 
     */
    public boolean succeeded() {
        return future.succeeded();
    }

    /**
     * Did it fail?
     * @return true if it failed or false otherwise
     */
    public boolean failed() {
        return future.failed();
    }

    /**
     * A Throwable describing failure. This will be null if the deployment has not started or if it succeeded.
     * @return the failure cause or null if the deployment has not started or if it succeeded
     */
    public Throwable failCause() {
        return future.cause();
    }

    /**
     * Constructor for creating a instance from JSON
     *
     * @param json  the JSON
     */
    public DeploymentConfiguration(JsonObject json) {
        fromJson(json);
    }

    /**
     * Populates this object with the information from the supplied JsonObject
     * @param json The JSON Object
     */
    public void fromJson(JsonObject json) {
        Objects.requireNonNull(json, "json is required");
        if (json.getValue("name") instanceof String)
            setName((String) json.getValue("name"));
        if (json.getValue("deploymentOptions") instanceof JsonObject) {
            setDeploymentOptions(new DeploymentOptions((JsonObject) json.getValue("deploymentOptions")));
        }
        if (json.getValue("dependents") instanceof JsonArray) {
            json.getJsonArray("dependents").forEach(item -> {
                if (item instanceof JsonObject) {
                    DependentsDeployment deps = new DependentsDeployment();
                    deps.fromJson((JsonObject) item);
                    getDependents().add(deps);
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
        json.put("name", name);
        if (deploymentOptions != null) {
            JsonObject depOptJson = deploymentOptions.toJson();
            json.put("deploymentOptions", depOptJson);
        }
        if (this.getDependents() != null) {
            JsonArray array = new JsonArray();
            this.getDependents().forEach(item -> array.add(item.toJson()));
            json.put("dependents", array);
        }
        return json;
    }

    @Override
    public String toString() {
        return "DeploymentConfiguration [name=" + name + ", deploymentOptions="
               + deploymentOptions + ", deploymentID=" + future.result() + ", dependents="
               + dependents + ", isComplete=" + future.isComplete() + ", succeeded="
               + future.succeeded() + ", failed=" + future.failed() + ", failCause="
               + future.cause() + "]";
    }
}
