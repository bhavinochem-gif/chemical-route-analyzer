package com.chem.routeanalyzer.model;

public class MoleculeMetric {
    private String smiles;
    private String molecularFormula;
    private double molecularWeight;
    private int atomCount;

    public MoleculeMetric(String smiles, String formula, double weight, int atoms) {
        this.smiles = smiles; this.molecularFormula = formula;
        this.molecularWeight = weight; this.atomCount = atoms;
    }
    public String getSmiles() { return smiles; }
    public String getMolecularFormula() { return molecularFormula; }
    public double getMolecularWeight() { return molecularWeight; }
    public int getAtomCount() { return atomCount; }
}
