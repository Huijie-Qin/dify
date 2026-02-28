package com.dify.core.plugin.entities.parameters;

public final class PluginParameterUtils {
    private PluginParameterUtils() {}

    public static String asNormalType(String value) {
        if ("secret-input".equals(value) || "select".equals(value) || "checkbox".equals(value)) {
            return "string";
        }
        return value;
    }

    public static Object castParameterValue(String type, Object value) {
        return value;
    }

    public static Object initFrontendParameter(PluginParameter rule, String type, Object value) {
        if (value == null) {
            return rule.getDefaultValue();
        }
        return value;
    }
}
