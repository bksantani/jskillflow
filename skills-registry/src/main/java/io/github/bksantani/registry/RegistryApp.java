/*
 * Copyright 2026 Bharat Santani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bksantani.registry;

import io.javalin.Javalin;

public class RegistryApp {
    public static void main(String[] args) {
        int port = 8080;
        System.out.println("Starting jskillflow registry server on port " + port + "...");
        
        Javalin app = Javalin.create(config -> {
            // Serve static files from the classpath '/public' directory
            config.staticFiles.add("/public");
            
            // SPA routing fallback: redirect all 404 client-side requests to index.html
            config.spaRoot.addFile("/", "/public/index.html");
            
            // Simple API health check endpoint
            config.routes.get("/api/health", ctx -> ctx.result("OK"));
        });

        app.start(port);
    }
}
