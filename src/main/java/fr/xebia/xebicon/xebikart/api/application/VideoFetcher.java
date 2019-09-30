package fr.xebia.xebicon.xebikart.api.application;

import fr.xebia.xebicon.xebikart.api.application.model.CarVideoFrame;
import io.reactivex.Flowable;

public interface VideoFetcher {

    Flowable<CarVideoFrame> video();

}
