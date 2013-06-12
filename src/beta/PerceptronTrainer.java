/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;

/**
 * Perceptron training.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class PerceptronTrainer implements Trainer {

	private final Model model;
	private final Parser parser;
	private final double[] acc;
	private int nUpdates;

	public PerceptronTrainer(Model model, Parser parser) {
		this.model = model;
		this.parser = parser;
		this.acc = new double[model.getWeightVector().length];
	}

	@Override
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

		nUpdates++;

		delta.update(1, model.getWeightVector());
		delta.update(nUpdates, acc);
	}

	@Override
	public void averageWeightVector() {
		for (int i = 0; i < model.getWeightVector().length; i++) {
			model.getWeightVector()[i] -= acc[i] / (nUpdates + 1);
		}
	}

	@Override
	public double[] getAveragedWeightVector() {
		double[] weightVector = model.getWeightVector();

		int nFeatures = weightVector.length;
		double[] averagedWeightVector = new double[nFeatures];
		for (int i = 0; i < nFeatures; i++) {
			averagedWeightVector[i] = weightVector[i] - acc[i] / (nUpdates + 1);
		}

		return averagedWeightVector;
	}
}
