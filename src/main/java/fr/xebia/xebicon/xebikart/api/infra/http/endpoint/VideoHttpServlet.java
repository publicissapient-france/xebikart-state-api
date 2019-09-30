package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import fr.xebia.xebicon.xebikart.api.application.VideoFetcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class VideoHttpServlet extends HttpServlet {

    private static final Logger LOGGER = getLogger(VideoHttpServlet.class);

    private static final String CONTENT_TYPE_FORMAT = "multipart/x-mixed-replace; boundary=%s";
    private static final String CONTENT_LENGTH_FORMAT = "Content-length: %s";
    private static final String CONTENT_TYPE_FRAME = "Content-type: image/jpeg";

    private final VideoFetcher videoFetcher;

    @Inject
    public VideoHttpServlet(VideoFetcher videoFetcher) {
        requireNonNull(videoFetcher, "videoFetcher must be defined.");
        this.videoFetcher = videoFetcher;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.trace("Client request video.");
        var boundary = RandomStringUtils.randomAlphabetic(10);
        resp.addHeader("Content-Type", String.format(
                CONTENT_TYPE_FORMAT,
                boundary
        ));
        resp.flushBuffer();

        var out = resp.getOutputStream();
        out.println("--" + boundary);

        videoFetcher.video()
                .subscribe(videoFrame -> {
                    //LOGGER.trace("Sending content of {} which size {}", videoFrame.getFilename(), videoFrame.getContent().length);
                    out.println(CONTENT_TYPE_FRAME);
                    out.println(String.format(CONTENT_LENGTH_FORMAT, videoFrame.getContent().length));
                    out.println();
                    out.write(videoFrame.getContent());
                    out.println();
                    out.println("--" + boundary);
                    out.flush();

                }).isDisposed();
        out.println();
        out.println("--" + boundary);
        out.flush();
        out.close();
        LOGGER.trace("Stream video ended, close the connection");
    }
}
