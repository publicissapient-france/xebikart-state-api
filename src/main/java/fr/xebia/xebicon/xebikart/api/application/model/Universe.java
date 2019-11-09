package fr.xebia.xebicon.xebikart.api.application.model;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Universe {

    private final String name;

    public Universe(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined and be non blank.");
        }

        this.name = name;
    }

    public String getName() {
        return name;
    }

}
