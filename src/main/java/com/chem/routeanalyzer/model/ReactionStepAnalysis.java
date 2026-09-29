package com.chem.routeanalyzer.model;
import java.util.List;

public class ReactionStepAnalysis {
    private int stepNumber;
    private String rawReactionSmiles;
    private List<MoleculeMetric> reactants, agents, products;
    private double totalReactantMass, totalProductMass, atomEconomyPercent, massBalanceDifferential;
    private boolean isMassBalanced;

    public ReactionStepAnalysis(int stepNumber, String rawReactionSmiles, List<MoleculeMetric> reactants, 
                                List<MoleculeMetric> agents, List<MoleculeMetric> products, double rMass, 
                                double pMass, double aePercent, double mbDiff, boolean isBalanced) {
        this.stepNumber = stepNumber; this.rawReactionSmiles = rawReactionSmiles;
        this.reactants = reactants; this.agents = agents; this.products = products;
        this.totalReactantMass = rMass; this.totalProductMass = pMass;
        this.atomEconomyPercent = aePercent; this.massBalanceDifferential = mbDiff; this.isMassBalanced = isBalanced;
    }
    public int getStepNumber() { return stepNumber; }
    public String getRawReactionSmiles() { return rawReactionSmiles; }
    public List<MoleculeMetric> getReactants() { return reactants; }
    public List<MoleculeMetric> getAgents() { return agents; }
    public List<MoleculeMetric> getProducts() { return products; }
    public double getTotalReactantMass() { return totalReactantMass; }
    public double getTotalProductMass() { return totalProductMass; }
    public double getAtomEconomyPercent() { return atomEconomyPercent; }
    public double getMassBalanceDifferential() { return massBalanceDifferential; }
    public boolean isMassBalanced() { return isMassBalanced; }
}
