package com.chem.routeanalyzer.model;
import java.util.List;

public class AnalysisResponse {
    private String routeTitle;
    private int totalSteps;
    private double overallAtomEconomyPercent;
    private List<ReactionStepAnalysis> stepAnalyses;

    public AnalysisResponse(String title, int steps, double ae, List<ReactionStepAnalysis> stepAnalyses) {
        this.routeTitle = title; this.totalSteps = steps;
        this.overallAtomEconomyPercent = ae; this.stepAnalyses = stepAnalyses;
    }
    public String getRouteTitle() { return routeTitle; }
    public int getTotalSteps() { return totalSteps; }
    public double getOverallAtomEconomyPercent() { return overallAtomEconomyPercent; }
    public List<ReactionStepAnalysis> getStepAnalyses() { return stepAnalyses; }
}
