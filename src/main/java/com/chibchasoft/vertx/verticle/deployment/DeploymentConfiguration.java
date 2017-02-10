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
import io.vertx.core.DeploymentOptionsConverter;
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
    private String                     deploymentID;
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
        this.deploymentOptions = deploymentOptions;
        return this;
    }

    /**
     * Get the Deployment ID for this verticle
     * @return The Deployment ID for this verticle
     */
    public String getDeploymentID() {
        return deploymentID;
    }

    /**
     * Sets the Deployment ID for this verticle
     * @param deploymentID The deployment ID
     * @return a reference to this, so the API can be used fluently
     */
    public DeploymentConfiguration setDeploymentID(String deploymentID) {
        this.deploymentID = deploymentID;
        return this;
    }

    /**
     * Get the list of deployments which depend on the successful deployment of this
     * verticle.
     * @return The list of deployments
     */
    public List<DependentsDeployment> getDependents() {
        return dependents;
    }

    /**
     * Set the list of deployments which depend on the successful deployment of this
     * verticle.
     * @param dependents The list of deployments
     * @return a reference to this, so the API can be used fluently
     */
    public DeploymentConfiguration setDependents(List<DependentsDeployment> dependents) {
        this.dependents = dependents;
        return this;
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
        if (json.getValue("name") instanceof String)
            setName((String) json.getValue("name"));
        if (json.getValue("deploymentID") instanceof String)
            setDeploymentID((String) json.getValue("deploymentID"));
        if (json.getValue("deploymentOptions") instanceof JsonObject) {
            setDeploymentOptions(new DeploymentOptions());
            DeploymentOptionsConverter.fromJson((JsonObject) json.getValue("deploymentOptions"), this.getDeploymentOptions());
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
        if (deploymentID != null)
            json.put("deploymentId", deploymentID);
        if (deploymentOptions != null) {
            JsonObject depOptJson = new JsonObject();
            DeploymentOptionsConverter.toJson(deploymentOptions, depOptJson);
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
               + deploymentOptions + ", deploymentID=" + deploymentID + ", dependents="
               + dependents + "]";
    }
}
