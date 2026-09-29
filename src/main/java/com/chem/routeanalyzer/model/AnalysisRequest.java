package com.chem.routeanalyzer.model;
import java.util.List;

public class AnalysisRequest {
    private String routeTitle;
    private List<String> reactionSmilesSteps;

    public AnalysisRequest() {}
    public String getRouteTitle() { return routeTitle; }
    public void setRouteTitle(String routeTitle) { this.routeTitle = routeTitle; }
    public List<String> getReactionSmilesSteps() { return reactionSmilesSteps; }
    public void setReactionSmilesSteps(List<String> reactionSmilesSteps) { this.reactionSmilesSteps = reactionSmilesSteps; }
}
