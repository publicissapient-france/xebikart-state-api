package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class RootEndpoint extends HttpServlet {

    private static final Logger LOGGER = getLogger(RootEndpoint.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.trace("Healthcheck called on '{}'", req.getPathInfo());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
