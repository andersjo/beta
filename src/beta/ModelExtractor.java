/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.conll.CoNLLReader;
import se.uu.nlp.dlib.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class ModelExtractor {

	private final Model model;
	private final ModelUpdater modelUpdater;
	private int nTrees;
	private int nTokens;

	public ModelExtractor() {
		this.model = new Model();
		this.modelUpdater = new ModelUpdater(model);
	}

	public ModelExtractor(Model model) {
		this.model = model;
		this.modelUpdater = new ModelUpdater(model);
	}

	public Model getModel() {
		return model;
	}

	public int getNTrees() {
		return nTrees;
	}

	public int getNTokens() {
		return nTokens;
	}

	public CoNLLTree next(CoNLLTree tree) {
		CoNLLTree input = new CoNLLTree(tree);
		for (int i = 0; i < tree.getNNodes(); i++) {
			input.forms[i] = CoNLLReader.normalize(tree.forms[i]);
			input.lemmas[i] = CoNLLReader.normalize(tree.lemmas[i]);

			model.addWord(input.forms[i]);
			model.addTag(input.postags[i]);
			model.addLabel(input.deprels[i]);
		}

		nTrees++;
		nTokens += input.getNNodes() - 1;

		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, input);
		for (int i = 1; i < input.getNNodes(); i++) {
			int label = model.getCodeForLabel(input.deprels[i]);
			featurizer.featurize(input.heads[i], i, label, modelUpdater);
		}

		return input;
	}

	private static class ModelUpdater implements FeatureHandler {

		private final Model model;

		public ModelUpdater(Model model) {
			this.model = model;
		}

		@Override
		public void handle(int[] feature) {
			model.addFeature(feature);
		}
	}
}
