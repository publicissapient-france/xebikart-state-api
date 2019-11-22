package fr.xebia.xebicon.xebikart.api.application;

import fr.xebia.xebicon.xebikart.api.application.model.CarVideoFrame;
import io.reactivex.Flowable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class LocalFileVideoFetcher implements VideoFetcher {

    private static final Logger LOGGER = getLogger(LocalFileVideoFetcher.class);

    private final File videoDirectory;

    @Inject
    public LocalFileVideoFetcher(File videoDirectory) {
        requireNonNull(videoDirectory, "videoDirectory must be defined.");
        this.videoDirectory = videoDirectory;
    }

    @Override
    public Flowable<CarVideoFrame> video() {
        var files = videoDirectory
                .listFiles(pathname -> pathname.getName().endsWith(".jpg"));
        if (files == null) {
            return Flowable.empty();
        }
        var videoFrames = Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName))
                .map(file -> {
                    try (var input = new FileInputStream(file)) {
                        return new CarVideoFrame(file.getName(), 0, IOUtils.toByteArray(input));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull);

        return Flowable.fromArray(videoFrames.toArray(CarVideoFrame[]::new));
    }
}
