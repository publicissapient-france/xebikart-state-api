package fr.xebia.xebicon.xebikart.api.infra.http.endpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class VideoHtmlPage extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var out = resp.getOutputStream();
        String content = "<html>\n" +
                "  <head>\n" +
                "    <title>Video Streaming Demonstration</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Video Streaming Demonstration</h1>\n" +
                "    <img src=\"/car/video\">\n" +
                "  </body>\n" +
                "</html>";
        out.write(content.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
