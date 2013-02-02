/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;


/**
 * Train a parsing model.
 * 
 * <p>The training method used by this class is passive–aggressive training.
 * This is described in the following paper:
 * 
 * <p>Avihai Mejer and Koby Crammer. Confidence Estimation in Structured
 * Prediction. CoRR Entry 1111.1386, Technion, Haifa, Israel, 2011.
 * 
 * <p>Passive–aggressive training applies essentially the same learning rule as
 * 1-best MIRA, but is slightly easier to implement.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class TrainerHandler {

	private static final double C = 0.1;
	private final Model model;
	private final Parser parser;
	private final double[] acc;
	private int nUpdates;

	public TrainerHandler(Model model, Parser parser) {
		this.model = model;
		int nFeatures = model.getNFeatures();
		model.setWeightVector(new double[nFeatures]);
		this.parser = parser;
		this.acc = new double[nFeatures];
	}

	public void update(CoNLLTree tree) {
		FeatureVector gold = EdgeFeaturizer.getFeatureVector(tree, model);

		int nNodes = tree.getNNodes();

		CoNLLTree input = new CoNLLTree(tree);
		for (int i = 0; i < nNodes; i++) {
			input.heads[i] = 0;
			input.deprels[i] = Model.UNKNOWN_LABEL;
		}

		CoNLLTree bestParse = parser.getBestParse(input);
		FeatureVector best = EdgeFeaturizer.getFeatureVector(bestParse, model);

		FeatureVector delta = FeatureVector.getDelta(gold, best);

		double squaredEuclidean = delta.getSquaredEuclidean();

		nUpdates++;

		if (squaredEuclidean != 0.0) {
			double above = Math.max(0, getLoss(tree, bestParse) - delta.getScore(model.getWeightVector()));
			double below = squaredEuclidean;

			double alpha = Math.min(C, above / below);

			delta.update(alpha, model.getWeightVector());
			delta.update(nUpdates * alpha, acc);
		}
	}

	public void averageWeightVector() {
		for (int i = 0; i < model.getWeightVector().length; i++) {
			model.getWeightVector()[i] -= acc[i] / (nUpdates + 1);
		}
	}

	public double[] getAveragedWeightVector() {
		double[] weightVector = model.getWeightVector();

		int nFeatures = weightVector.length;
		double[] averagedWeightVector = new double[nFeatures];
		for (int i = 0; i < nFeatures; i++) {
			averagedWeightVector[i] = weightVector[i] - acc[i] / (nUpdates + 1);
		}

		return averagedWeightVector;
	}

	private static double getLoss(CoNLLTree gold, CoNLLTree best) {
		double loss = 2 * (gold.getNNodes() - 1);

		for (int i = 1; i < gold.getNNodes(); i++) {
			if (gold.heads[i] == best.heads[i]) {
				loss -= 1;
			}
			if (gold.deprels[i].equals(best.deprels[i])) {
				loss -= 1;
			}
		}

		return loss;
	}
}
