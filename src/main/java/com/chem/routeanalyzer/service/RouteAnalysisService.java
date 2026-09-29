package com.chem.routeanalyzer.service;

import com.chem.routeanalyzer.model.*;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteAnalysisService {
    private final SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    public AnalysisResponse analyzeRoute(AnalysisRequest request) {
        List<ReactionStepAnalysis> steps = new ArrayList<>();
        double accumulatedAtomEconomy = 0.0;
        int stepIndex = 1;

        for (String rxnSmiles : request.getReactionSmilesSteps()) {
            if (rxnSmiles == null || rxnSmiles.trim().isEmpty()) continue;
            ReactionStepAnalysis stepAnalysis = analyzeSingleStep(stepIndex++, rxnSmiles.trim());
            steps.add(stepAnalysis);
            accumulatedAtomEconomy += stepAnalysis.getAtomEconomyPercent();
        }

        double averageAtomEconomy = steps.isEmpty() ? 0.0 : accumulatedAtomEconomy / steps.size();
        return new AnalysisResponse(
                request.getRouteTitle() != null ? request.getRouteTitle() : "Synthesis Route Analysis",
                steps.size(), Math.round(averageAtomEconomy * 100.0) / 100.0, steps
        );
    }

    private ReactionStepAnalysis analyzeSingleStep(int stepNum, String rxnSmiles) {
        String[] sections = rxnSmiles.split(">", -1);
        String reactantBlock = sections.length > 0 ? sections[0] : "";
        String agentBlock = sections.length > 2 ? sections[1] : "";
        String productBlock = sections.length == 3 ? sections[2] : (sections.length == 2 ? sections[1] : "");

        List<MoleculeMetric> reactants = parseMoleculeBlock(reactantBlock);
        List<MoleculeMetric> agents = parseMoleculeBlock(agentBlock);
        List<MoleculeMetric> products = parseMoleculeBlock(productBlock);

        double totalReactantMass = reactants.stream().mapToDouble(MoleculeMetric::getMolecularWeight).sum();
        double totalProductMass = products.stream().mapToDouble(MoleculeMetric::getMolecularWeight).sum();
        double targetProductMass = products.isEmpty() ? 0.0 : products.get(0).getMolecularWeight();
        double atomEconomy = (totalReactantMass > 0) ? (targetProductMass / totalReactantMass) * 100.0 : 0.0;
        double massDiff = Math.abs(totalReactantMass - totalProductMass);

        return new ReactionStepAnalysis(
                stepNum, rxnSmiles, reactants, agents, products,
                Math.round(totalReactantMass * 100.0) / 100.0,
                Math.round(totalProductMass * 100.0) / 100.0,
                Math.round(atomEconomy * 100.0) / 100.0,
                Math.round(massDiff * 100.0) / 100.0,
                massDiff < 1.0
        );
    }

    private List<MoleculeMetric> parseMoleculeBlock(String block) {
        List<MoleculeMetric> metrics = new ArrayList<>();
        if (block == null || block.trim().isEmpty()) return metrics;
        for (String smi : block.split("\\.")) {
            if (smi.trim().isEmpty()) continue;
            try {
                IAtomContainer container = smilesParser.parseSmiles(smi.trim());
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
                double mw = AtomContainerManipulator.getMass(container, AtomContainerManipulator.MonoIsotopic);
                String formula = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(container));
                metrics.add(new MoleculeMetric(smi.trim(), formula, Math.round(mw * 100.0) / 100.0, container.getAtomCount()));
            } catch (Exception e) {
                metrics.add(new MoleculeMetric(smi.trim(), "Unparsed", 0.0, 0));
            }
        }
        return metrics;
    }
}
