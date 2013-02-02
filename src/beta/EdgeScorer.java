/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.io.CoNLLTree;


/**
 * Score an edge in a dependency graph.
 *
 * <p>This class implements the core of the edge-factored scoring model: a
 * scoring function for labeled edges.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class EdgeScorer {

	/**
	 * The scores for all labeled arcs.
	 */
	private final double[][][] scores;
	/**
	 * The best labels for all (source, target) pairs.
	 */
	private final int[][] bestLabels;

	/**
	 * Create a new scorer for the specified graph based on the specified model.
	 *
	 * @param model the model based on which the arcs in the specified graph
	 * should be scored
	 * @param graph the graph whose arcs should be scored
	 */
	public EdgeScorer(Model model, CoNLLTree graph) {
		int nNodes = graph.getNNodes();

		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, graph);
		FeatureVectorUpdater updater = new FeatureVectorUpdater(model);

		// Step 1: Compute the core (unlabeled) scores.

		double[][] scoresCore = new double[nNodes][nNodes];

		for (int fst = 0; fst < nNodes; fst++) {
			for (int snd = fst + 1; snd < nNodes; snd++) {
				// Arc from fst to snd (right arc; RA).
				FeatureVector vectorRA = new FeatureVector();
				updater.setClient(vectorRA);
				featurizer.featurizeCore(fst, snd, true, updater);
				scoresCore[fst][snd] = vectorRA.getScore(model.getWeightVector());

				// Arc from snd to fst (left arc; LA).
				FeatureVector vectorLA = new FeatureVector();
				updater.setClient(vectorLA);
				featurizer.featurizeCore(fst, snd, false, updater);
				scoresCore[snd][fst] = vectorLA.getScore(model.getWeightVector());
			}
		}

		// Step 2: Compute the labeled scores. One might expect that this would
		// use three loops: fst, snd, label. Instead, the score of a labeled arc
		// (fst, snd, label) is decomposed into two parts according to the
		// scheme (node, label, dir, tgt). The dir component specifies whether
		// the arc is a RA or a LA. The tgt component specifies whether the
		// node is the target or the source of the edge.

		int nLabels = model.getNLabels();

		double[][][][] scoresLabeled = new double[nNodes][nLabels][2][2];

		for (int node = 0; node < nNodes; node++) {
			for (int label = 0; label < nLabels; label++) {
				for (int i = 0; i < 2; i++) {
					boolean isTarget = i == 0;

					FeatureVector vectorRA = new FeatureVector();
					updater.setClient(vectorRA);
					featurizer.featurizeLabeled(node, label, true, isTarget, updater);
					scoresLabeled[node][label][0][i] = vectorRA.getScore(model.getWeightVector());

					FeatureVector vectorLA = new FeatureVector();
					updater.setClient(vectorLA);
					featurizer.featurizeLabeled(node, label, false, isTarget, updater);
					scoresLabeled[node][label][1][i] = vectorLA.getScore(model.getWeightVector());
				}
			}
		}

		// Step 3: Compute the full scores and the labels that yield them.

		this.scores = new double[nNodes][nNodes][nLabels];
		this.bestLabels = new int[nNodes][nNodes];

		for (int fst = 0; fst < nNodes; fst++) {
			for (int snd = fst + 1; snd < nNodes; snd++) {
				bestLabels[fst][snd] = model.getCodeForDefaultLabel();
				bestLabels[snd][fst] = model.getCodeForDefaultLabel();

				double bestScoreRA = Double.NEGATIVE_INFINITY;
				double bestScoreLA = Double.NEGATIVE_INFINITY;

				for (int lab = 0; lab < nLabels; lab++) {
					// Edge from fst to snd (RA).
					scores[fst][snd][lab] = scoresCore[fst][snd] + scoresLabeled[fst][lab][0][1] + scoresLabeled[snd][lab][0][0];
					if (scores[fst][snd][lab] > bestScoreRA) {
						bestScoreRA = scores[fst][snd][lab];
						bestLabels[fst][snd] = lab;
					}

					// Edge from snd to fst (LA).
					scores[snd][fst][lab] = scoresCore[snd][fst] + scoresLabeled[snd][lab][1][1] + scoresLabeled[fst][lab][1][0];
					if (scores[snd][fst][lab] > bestScoreLA) {
						bestScoreLA = scores[snd][fst][lab];
						bestLabels[snd][fst] = lab;
					}
				}
			}
		}
	}

	/**
	 * Returns the score for the specified arc.
	 *
	 * @param src the source node of the arc
	 * @param tgt the target node of the arc
	 * @param label the label of the arc
	 * @return the score for the specified arc
	 */
	public double getScore(int src, int tgt, int label) {
		assert src != tgt;
		return scores[src][tgt][label];
	}

	/**
	 * Returns the best label for the specified arc. This is the label that
	 * argmaximizes the score of the arc (src, tgt, label).
	 *
	 * @param src the source node of the arc
	 * @param tgt the target node of the arc
	 * @return the best label for the specified arc
	 */
	public int getBestLabel(int src, int tgt) {
		assert src != tgt;
		return bestLabels[src][tgt];
	}

	/**
	 * Returns the highest possible score for the specified arc.
	 *
	 * @param src the source node of the arc
	 * @param tgt the target node of the arc
	 * @return the highest possible score for the specified arc
	 */
	public double getScoreForBestLabel(int src, int tgt) {
		assert src != tgt;
		return scores[src][tgt][bestLabels[src][tgt]];
	}

	/**
	 * Update a client feature vector.
	 */
	private static class FeatureVectorUpdater implements FeatureHandler {

		private final Model model;
		private FeatureVector client;

		public FeatureVectorUpdater(Model model) {
			this.model = model;
		}

		public void setClient(FeatureVector featureVector) {
			this.client = featureVector;
		}

		@Override
		public void handle(int[] feature) {
			int index = model.getCodeForFeature(feature);
			if (index >= 0) {
				client.increment(index);
			}
		}
	}
}
