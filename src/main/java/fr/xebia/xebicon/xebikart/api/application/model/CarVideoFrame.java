package fr.xebia.xebicon.xebikart.api.application.model;

public class CarVideoFrame {

    private final String filename;

    private final byte[] content;

    public CarVideoFrame(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }
}
