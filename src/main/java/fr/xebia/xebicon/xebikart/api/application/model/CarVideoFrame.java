package fr.xebia.xebicon.xebikart.api.application.model;

public class CarVideoFrame {

    private final String origin;

    private final int carId;

    private final byte[] content;

    public CarVideoFrame(String origin, int carId, byte[] content) {
        this.origin = origin;
        this.carId = carId;
        this.content = content;
    }


    public int getCarId() {
        return carId;
    }

    public String getOrigin() {
        return origin;
    }

    public byte[] getContent() {
        return content;
    }
}
