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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Verticle used for testing
 * @author juanavelez
 */
public class TestVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start(Future<Void> startFuture) {
        logger.info("Starting this verticle");
        vertx.eventBus().send("TestVerticleStarted", "started");
        startFuture.complete();
    }
}
