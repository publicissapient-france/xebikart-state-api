package fr.xebia.xebicon.xebikart.api.application.model;

import fr.xebia.xebicon.xebikart.api.application.cqrs.SurveyIdentifier;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SurveyResult {

    private final SurveyIdentifier surveyIdentifier;

    private final Universe universe;

    private final Map<String, Integer> votes;

    public SurveyResult(SurveyIdentifier surveyIdentifier, Universe universe, Map<String, Integer> votes) {
        requireNonNull(surveyIdentifier, "surveyIdentifier must be defined.");
        requireNonNull(universe, "universe must be defined.");
        requireNonNull(votes, "votes must be defined.");
        this.surveyIdentifier = surveyIdentifier;
        this.universe = universe;
        this.votes = votes;
    }

    public SurveyIdentifier getSurveyIdentifier() {
        return surveyIdentifier;
    }

    public Universe getUniverse() {
        return universe;
    }

    public Map<String, Integer> getVotes() {
        return new HashMap<>(votes);
    }
}
