package fr.xebia.xebicon.xebikart.api.application;

import fr.xebia.xebicon.xebikart.api.application.cqrs.Either;
import fr.xebia.xebicon.xebikart.api.application.cqrs.SurveyIdentifier;
import fr.xebia.xebicon.xebikart.api.application.model.OsMobile;
import fr.xebia.xebicon.xebikart.api.application.model.SurveyResult;
import fr.xebia.xebicon.xebikart.api.application.model.Universe;

public interface UniverseService {

    SurveyIdentifier createSurvey();

    void addVote(Universe universe, OsMobile fromDeviceOs);

    Either<String, SurveyResult> close();

}
