package fr.xebia.xebicon.xebikart.api.application.model;

public class CarVideoFrame {

    private final String origin;

    private final byte[] content;

    public CarVideoFrame(String origin, byte[] content) {
        this.origin = origin;
        this.content = content;
    }

    public String getOrigin() {
        return origin;
    }

    public byte[] getContent() {
        return content;
    }
}
