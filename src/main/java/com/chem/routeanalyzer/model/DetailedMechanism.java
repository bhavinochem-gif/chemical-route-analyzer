package com.chem.routeanalyzer.model;
import java.util.List;

public class DetailedMechanism {
    public static class Step {
        public int stepNumber;
        public String stepName;
        public String intermediateSmiles;
        public String fmoInteraction;
        public String drivingForce;
        public String electronMovement;
        public List<String> bondsFormed;
        public List<String> bondsBroken;
    }

    public String reactionName;
    public String overallRationalization;
    public List<Step> steps;
}
