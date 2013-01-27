/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class EdgeScorer {

	private final double[][][] scores;
	private final int[][] bestLabels;
	private final double[][] scoresForBestLabel;

	public EdgeScorer(Model model, CoNLLTree graph) {
		int nNodes = graph.getNNodes();

		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, graph);

		FeatureVector[][] vectorsCore = new FeatureVector[nNodes][nNodes];
		double[][] scoresCore = new double[nNodes][nNodes];

		for (int fst = 0; fst < nNodes; fst++) {
			for (int snd = fst + 1; snd < nNodes; snd++) {
				// Edge from fst to snd (RA)
				FeatureVector vectorRA = new FeatureVector();
				FeatureVectorUpdater updaterRA = new FeatureVectorUpdater(model, vectorRA);
				featurizer.featurizeCore(fst, snd, true, updaterRA);
				vectorsCore[fst][snd] = vectorRA;
				scoresCore[fst][snd] = vectorRA.getScore(model.getWeightVector());

				// Edge from snd to fst (LA)
				FeatureVector vectorLA = new FeatureVector();
				FeatureVectorUpdater updaterLA = new FeatureVectorUpdater(model, vectorLA);
				featurizer.featurizeCore(fst, snd, false, updaterLA);
				vectorsCore[snd][fst] = vectorLA;
				scoresCore[snd][fst] = vectorLA.getScore(model.getWeightVector());
			}
		}

		int nLabels = model.getNLabels();

		FeatureVector[][][][] vectorsLabeled = new FeatureVector[nNodes][nLabels][2][2];
		double[][][][] scoresLabeled = new double[nNodes][nLabels][2][2];

		for (int node = 0; node < nNodes; node++) {
			for (int label = 0; label < nLabels; label++) {
				for (int i = 0; i < 2; i++) {
					boolean isTarget = i == 0;

					FeatureVector vectorRA = new FeatureVector();
					FeatureVectorUpdater updaterRA = new FeatureVectorUpdater(model, vectorRA);
					featurizer.featurizeLabeled(node, label, true, isTarget, updaterRA);
					vectorsLabeled[node][label][0][i] = vectorRA;
					scoresLabeled[node][label][0][i] = vectorRA.getScore(model.getWeightVector());

					FeatureVector vectorLA = new FeatureVector();
					FeatureVectorUpdater updaterLA = new FeatureVectorUpdater(model, vectorLA);
					featurizer.featurizeLabeled(node, label, false, isTarget, updaterLA);
					vectorsLabeled[node][label][1][i] = vectorLA;
					scoresLabeled[node][label][1][i] = vectorLA.getScore(model.getWeightVector());
				}
			}
		}

		this.scores = new double[nNodes][nNodes][nLabels];
		this.bestLabels = new int[nNodes][nNodes];
		this.scoresForBestLabel = new double[nNodes][nNodes];
		for (int fst = 0; fst < nNodes; fst++) {
			for (int snd = fst + 1; snd < nNodes; snd++) {
				bestLabels[fst][snd] = model.getCodeForDefaultLabel();
				bestLabels[snd][fst] = model.getCodeForDefaultLabel();

				scoresForBestLabel[fst][snd] = Double.NEGATIVE_INFINITY;
				scoresForBestLabel[snd][fst] = Double.NEGATIVE_INFINITY;

				for (int lab = 0; lab < nLabels; lab++) {
					// Edge from fst to snd (RA).
					scores[fst][snd][lab] = scoresCore[fst][snd] + scoresLabeled[fst][lab][0][1] + scoresLabeled[snd][lab][0][0];
					if (scores[fst][snd][lab] > scoresForBestLabel[fst][snd]) {
						scoresForBestLabel[fst][snd] = scores[fst][snd][lab];
						bestLabels[fst][snd] = lab;
					}

					// Edge from snd to fst (LA).
					scores[snd][fst][lab] = scoresCore[snd][fst] + scoresLabeled[snd][lab][1][1] + scoresLabeled[fst][lab][1][0];
					if (scores[snd][fst][lab] > scoresForBestLabel[snd][fst]) {
						scoresForBestLabel[snd][fst] = scores[snd][fst][lab];
						bestLabels[snd][fst] = lab;
					}
				}
			}
		}
	}

	public double getScore(int src, int tgt, int label) {
		assert src != tgt;
		return scores[src][tgt][label];
	}

	public int getBestLabel(int src, int tgt) {
		assert src != tgt;
		return bestLabels[src][tgt];
	}

	public double getScoreForBestLabel(int src, int tgt) {
		assert src != tgt;
		return scoresForBestLabel[src][tgt];
	}

	private static class FeatureVectorUpdater implements FeatureHandler {

		private final Model model;
		private final FeatureVector featureVector;

		public FeatureVectorUpdater(Model model, FeatureVector featureVector) {
			this.model = model;
			this.featureVector = featureVector;
		}

		@Override
		public void handle(int[] feature) {
			int index = model.getCodeForFeature(feature);
			if (index >= 0) {
				featureVector.increment(index);
			}
		}
	}
}
