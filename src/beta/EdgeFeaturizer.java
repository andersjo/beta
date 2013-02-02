/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class EdgeFeaturizer {

	private static final int BEG = -1;
	private static final int END = -2;
	private static final int MID = -3;
	private final int[] w;
	private final int[] t;
	private final int[] pred_t;
	private final int[] succ_t;

	public EdgeFeaturizer(Model model, CoNLLTree tree) {
		int nNodes = tree.getNNodes();

		this.w = new int[nNodes];
		this.t = new int[nNodes];

		for (int i = 0; i < nNodes; i++) {
			w[i] = model.getCodeForWord(tree.forms[i]);
			t[i] = model.getCodeForTag(tree.postags[i]);
		}

		this.pred_t = new int[nNodes];

		pred_t[0] = BEG;
		for (int i = 1; i < nNodes; i++) {
			pred_t[i] = t[i - 1];
		}

		this.succ_t = new int[nNodes];

		succ_t[nNodes - 1] = END;
		for (int i = nNodes - 2; i >= 0; i--) {
			succ_t[i] = t[i + 1];
		}
	}

	public void featurize(int src, int tgt, int label, FeatureHandler h) {
		int fst = src < tgt ? src : tgt;
		int snd = src < tgt ? tgt : src;

		boolean isRA = src < tgt;

		featurizeCore(fst, snd, isRA, h);
		featurizeLabeled(tgt, label, isRA, true, h);
		featurizeLabeled(src, label, isRA, false, h);
	}

	public FeatureVector getFeatureVector(Model model, int src, int tgt, int label) {
		FeatureVector featureVector = new FeatureVector();
		featurize(src, tgt, label, new FeatureVectorUpdater(model, featureVector));
		return featureVector;
	}

	public static FeatureVector getFeatureVector(CoNLLTree graph, Model model) {
		FeatureVector featureVector = new FeatureVector();
		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, graph);
		FeatureVectorUpdater updater = new FeatureVectorUpdater(model, featureVector);
		for (int i = 1; i < graph.getNNodes(); i++) {
			int label = model.getCodeForLabel(graph.deprels[i]);
			featurizer.featurize(graph.heads[i], i, label, updater);
		}
		return featureVector;
	}

	public void featurizeCore(int fst, int snd, boolean isRA, FeatureHandler h) {
		int att = isRA ? 0 : 10;

		int dist = snd - fst;
		if (dist > 10) {
			dist = 6;
		} else if (dist > 5) {
			dist = 5;
		} else {
			dist = dist - 1;
		}

		int attDist = att + dist;

		// addLinearFeatures

		int fst_t = t[fst];
		int snd_t = t[snd];

		int fst_pred_t = pred_t[fst];
		int snd_succ_t = succ_t[snd];
		int fst_succ_t = fst < snd - 1 ? succ_t[fst] : MID;
		int snd_pred_t = snd > fst + 1 ? pred_t[snd] : MID;

		for (int mid = fst + 1; mid < snd; mid++) {
			int mid_t = t[mid];
			h.handle(0, fst_t, snd_t, mid_t);
			h.handle(0, fst_t, snd_t, mid_t, attDist);
		}

		// addCorePosFeatures

		h.handle(1, fst_pred_t, fst_t, snd_t);
		h.handle(1, fst_pred_t, fst_t, snd_t, attDist);

		h.handle(2, fst_pred_t, fst_t, snd_t, snd_succ_t);
		h.handle(2, fst_pred_t, fst_t, snd_t, snd_succ_t, attDist);

		h.handle(3, fst_pred_t, snd_t, snd_succ_t);
		h.handle(3, fst_pred_t, snd_t, snd_succ_t, attDist);

		h.handle(4, fst_pred_t, fst_t, snd_succ_t);
		h.handle(4, fst_pred_t, fst_t, snd_succ_t, attDist);

		h.handle(5, fst_t, snd_t, snd_succ_t);
		h.handle(5, fst_t, snd_t, snd_succ_t, attDist);

		h.handle(6, fst_t, fst_succ_t, snd_pred_t);
		h.handle(6, fst_t, fst_succ_t, snd_pred_t, attDist);

		h.handle(7, fst_t, fst_succ_t, snd_pred_t, snd_t);
		h.handle(7, fst_t, fst_succ_t, snd_pred_t, snd_t, attDist);

		h.handle(8, fst_t, fst_succ_t, snd_t);
		h.handle(8, fst_t, fst_succ_t, snd_t, attDist);

		h.handle(9, fst_t, snd_pred_t, snd_t);
		h.handle(9, fst_t, snd_pred_t, snd_t, attDist);

		h.handle(10, fst_succ_t, snd_pred_t, snd_t);
		h.handle(10, fst_succ_t, snd_pred_t, snd_t, attDist);

		h.handle(11, fst_pred_t, fst_t, snd_pred_t, snd_t);
		h.handle(11, fst_pred_t, fst_t, snd_pred_t, snd_t, attDist);

		h.handle(12, fst_t, fst_succ_t, snd_t, snd_succ_t);
		h.handle(12, fst_t, fst_succ_t, snd_t, snd_succ_t, attDist);

		//

		int src = isRA ? fst : snd;
		int tgt = isRA ? snd : fst;

		int src_w = w[src];
		int src_t = t[src];
		int tgt_w = w[tgt];
		int tgt_t = t[tgt];

		// addTwoObsFeatures

		h.handle(13, src_w);
		h.handle(13, src_w, attDist);

		h.handle(14, src_w, src_t);
		h.handle(14, src_w, src_t, attDist);

		h.handle(15, src_w, src_t, tgt_t);
		h.handle(15, src_w, src_t, tgt_t, attDist);

		h.handle(16, src_w, src_t, tgt_t, tgt_w);
		h.handle(16, src_w, src_t, tgt_t, tgt_w, attDist);

		h.handle(17, src_w, tgt_w);
		h.handle(17, src_w, tgt_w, attDist);

		h.handle(18, src_w, tgt_t);
		h.handle(18, src_w, tgt_t, attDist);

		h.handle(19, src_t, tgt_w);
		h.handle(19, src_t, tgt_w, attDist);

		h.handle(20, src_t, tgt_w, tgt_t);
		h.handle(20, src_t, tgt_w, tgt_t, attDist);

		h.handle(21, src_t, tgt_t);
		h.handle(21, src_t, tgt_t, attDist);

		h.handle(22, tgt_w, tgt_t);
		h.handle(22, tgt_w, tgt_t, attDist);

		h.handle(23, src_t);
		h.handle(23, src_t, attDist);

		h.handle(24, tgt_w);
		h.handle(24, tgt_w, attDist);

		h.handle(25, tgt_t);
		h.handle(25, tgt_t, attDist);
	}

	public void featurizeLabeled(int node, int label, boolean isRA, boolean isTarget, FeatureHandler h) {
		int suffix = (isRA ? 0 : 10) + (isTarget ? 0 : 1);

		int node_w = w[node];
		int node_t = t[node];
		int node_pred_t = pred_t[node];
		int node_succ_t = succ_t[node];

		h.handle(26, label);
		h.handle(26, label, suffix);

		h.handle(27, node_w, node_t, label);
		h.handle(27, node_w, node_t, label, suffix);

		h.handle(28, node_t, label);
		h.handle(28, node_t, label, suffix);

		h.handle(29, node_pred_t, node_t, label);
		h.handle(29, node_pred_t, node_t, label, suffix);

		h.handle(30, node_t, node_succ_t, label);
		h.handle(30, node_t, node_succ_t, label, suffix);

		h.handle(31, node_pred_t, node_t, node_succ_t, label);
		h.handle(31, node_pred_t, node_t, node_succ_t, label, suffix);

		h.handle(32, node_w, label);
		h.handle(32, node_w, label, suffix);
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
