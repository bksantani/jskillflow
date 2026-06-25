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

