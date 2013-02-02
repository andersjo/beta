/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.io.CoNLLTree;

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
			h.handle(100, fst_t, snd_t, mid_t);
			h.handle(100, fst_t, snd_t, mid_t, attDist);
		}

		// addCorePosFeatures

		h.handle(101, fst_pred_t, fst_t, snd_t);
		h.handle(101, fst_pred_t, fst_t, snd_t, attDist);

		h.handle(102, fst_pred_t, fst_t, snd_t, snd_succ_t);
		h.handle(102, fst_pred_t, fst_t, snd_t, snd_succ_t, attDist);

		h.handle(103, fst_pred_t, snd_t, snd_succ_t);
		h.handle(103, fst_pred_t, snd_t, snd_succ_t, attDist);

		h.handle(104, fst_pred_t, fst_t, snd_succ_t);
		h.handle(104, fst_pred_t, fst_t, snd_succ_t, attDist);

		h.handle(105, fst_t, snd_t, snd_succ_t);
		h.handle(105, fst_t, snd_t, snd_succ_t, attDist);

		h.handle(106, fst_t, fst_succ_t, snd_pred_t);
		h.handle(106, fst_t, fst_succ_t, snd_pred_t, attDist);

		h.handle(107, fst_t, fst_succ_t, snd_pred_t, snd_t);
		h.handle(107, fst_t, fst_succ_t, snd_pred_t, snd_t, attDist);

		h.handle(108, fst_t, fst_succ_t, snd_t);
		h.handle(108, fst_t, fst_succ_t, snd_t, attDist);

		h.handle(109, fst_t, snd_pred_t, snd_t);
		h.handle(109, fst_t, snd_pred_t, snd_t, attDist);

		h.handle(110, fst_succ_t, snd_pred_t, snd_t);
		h.handle(110, fst_succ_t, snd_pred_t, snd_t, attDist);

		h.handle(111, fst_pred_t, fst_t, snd_pred_t, snd_t);
		h.handle(111, fst_pred_t, fst_t, snd_pred_t, snd_t, attDist);

		h.handle(112, fst_t, fst_succ_t, snd_t, snd_succ_t);
		h.handle(112, fst_t, fst_succ_t, snd_t, snd_succ_t, attDist);

		//

		int src = isRA ? fst : snd;
		int tgt = isRA ? snd : fst;

		int src_w = w[src];
		int src_t = t[src];
		int tgt_w = w[tgt];
		int tgt_t = t[tgt];

		// addTwoObsFeatures

		h.handle(113, src_w);
		h.handle(113, src_w, attDist);

		h.handle(114, src_w, src_t);
		h.handle(114, src_w, src_t, attDist);

		h.handle(115, src_w, src_t, tgt_t);
		h.handle(115, src_w, src_t, tgt_t, attDist);

		h.handle(116, src_w, src_t, tgt_t, tgt_w);
		h.handle(116, src_w, src_t, tgt_t, tgt_w, attDist);

		h.handle(117, src_w, tgt_w);
		h.handle(117, src_w, tgt_w, attDist);

		h.handle(118, src_w, tgt_t);
		h.handle(118, src_w, tgt_t, attDist);

		h.handle(119, src_t, tgt_w);
		h.handle(119, src_t, tgt_w, attDist);

		h.handle(120, src_t, tgt_w, tgt_t);
		h.handle(120, src_t, tgt_w, tgt_t, attDist);

		h.handle(121, src_t, tgt_t);
		h.handle(121, src_t, tgt_t, attDist);

		h.handle(122, tgt_w, tgt_t);
		h.handle(122, tgt_w, tgt_t, attDist);

		h.handle(123, src_t);
		h.handle(123, src_t, attDist);

		h.handle(124, tgt_w);
		h.handle(124, tgt_w, attDist);

		h.handle(125, tgt_t);
		h.handle(125, tgt_t, attDist);
	}

	public void featurizeLabeled(int node, int label, boolean isRA, boolean isTarget, FeatureHandler h) {
		int suffix = (isRA ? 0 : 10) + (isTarget ? 0 : 1);

		int node_w = w[node];
		int node_t = t[node];
		int node_pred_t = pred_t[node];
		int node_succ_t = succ_t[node];

		h.handle(200, label);
		h.handle(200, label, suffix);

		h.handle(201, node_w, node_t, label);
		h.handle(201, node_w, node_t, label, suffix);

		h.handle(202, node_t, label);
		h.handle(202, node_t, label, suffix);

		h.handle(203, node_pred_t, node_t, label);
		h.handle(203, node_pred_t, node_t, label, suffix);

		h.handle(204, node_t, node_succ_t, label);
		h.handle(204, node_t, node_succ_t, label, suffix);

		h.handle(205, node_pred_t, node_t, node_succ_t, label);
		h.handle(205, node_pred_t, node_t, node_succ_t, label, suffix);

		h.handle(206, node_w, label);
		h.handle(206, node_w, label, suffix);
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
