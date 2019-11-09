package fr.xebia.xebicon.xebikart.api.application.model;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;

public enum OsMobile {
    UNKNOWN,
    WINDOWS,
    ANDROID,
    IOS;

    public static OsMobile fromString(String value) {
        if (isBlank(value)) {
            return UNKNOWN;
        }
        var osMobileUpperCase = value.toUpperCase();
        if (Arrays.stream(OsMobile.values()).anyMatch(item -> osMobileUpperCase.equals(item.name()))) {
            return OsMobile.valueOf(osMobileUpperCase);
        }
        return UNKNOWN;
    }
}