/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.tree.Tree;

/**
 * Passive-aggressive training. This is based on the following:
 *
 * Avihai Mejer and Koby Crammer. Confidence Estimation in Structured
 * Prediction. CoRR abs/1111.1386, 2011.
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

	public void update(Tree tree) {
		FeatureVector gold = EdgeFeaturizer.getFeatureVector(tree, model);

		int nNodes = tree.getNNodes();

		Tree input = new Tree(tree);
		for (int i = 0; i < nNodes; i++) {
			input.heads[i] = 0;
			input.deprels[i] = Model.UNKNOWN_LABEL;
		}

		Tree bestParse = parser.getBestParse(input);
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

	private static double getLoss(Tree gold, Tree best) {
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
