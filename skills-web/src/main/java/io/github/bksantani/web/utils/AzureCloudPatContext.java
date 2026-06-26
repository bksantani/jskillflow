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

package io.github.bksantani.web.utils;

public final class AzureCloudPatContext {

    private static final ThreadLocal<String> PAT_HOLDER = new ThreadLocal<>();

    private AzureCloudPatContext() {
    }

    public static void set(String pat) {
        PAT_HOLDER.set(pat);
    }

    public static String getRequired() {
        String pat = PAT_HOLDER.get();
        if (pat == null || pat.isBlank()) {
            throw new IllegalStateException("Missing required PAT header.");
        }
        return pat;
    }

    public static void clear() {
        PAT_HOLDER.remove();
    }
}

