package fr.xebia.xebicon.xebikart.api.application.configuration;

import javax.servlet.Servlet;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SSEConfiguration {

    private final String path;

    private final Servlet servlet;

    public SSEConfiguration(String path, Servlet servlet) {
        if (isBlank(path)) {
            throw new IllegalArgumentException("path must be defined and be non blank.");
        }
        requireNonNull(servlet, "servlet must be defined.");
        this.path = path;
        this.servlet = servlet;
    }

    public String getPath() {
        return path;
    }

    public Servlet getServlet() {
        return servlet;
    }

}
