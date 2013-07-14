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
public class Trainer {

	private final Model model;
	private final Parser parser;
	private final double[] acc;
	private int nUpdates;

	public Trainer(Model model, Parser parser) {
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

		nUpdates++;

		gold.addTo(model.getWeightVector());
		best.subtractFrom(model.getWeightVector());

		gold.addTo(nUpdates, acc);
		best.subtractFrom(nUpdates, acc);
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
}
