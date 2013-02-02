/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;


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
		for (int i = 0; i < tree.getNNodes(); i++) {
			model.addWord(tree.forms[i]);
			model.addTag(tree.postags[i]);
			if (i > 0) {
				model.addLabel(tree.deprels[i]);
			}
		}
		nTrees++;
		nTokens += tree.getNNodes() - 1;

		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, tree);
		for (int i = 1; i < tree.getNNodes(); i++) {
			int label = model.getCodeForLabel(tree.deprels[i]);
			featurizer.featurize(tree.heads[i], i, label, modelUpdater);
		}

		return tree;
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
