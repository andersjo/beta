/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

import se.liu.ida.nlp.beta.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class EdgeFeaturizer {

	private static final int[] LIMITS = new int[]{1, 2, 3, 4, 5, 6, 11};
	private final int T_OFF;
	private final int W_OFF;
	private final int L_OFF;
	private final int BEG_T;
	private final int END_T;
	private final int MID_T;
	private final int[] w;
	private final int[] t;
	private final int[] pred_t;
	private final int[] succ_t;

	private static int getNBits(long x) {
		return (int) Math.ceil(Math.log(x) / Math.log(2));
	}

	public EdgeFeaturizer(Model model, CoNLLTree tree) {
		int nForms = model.getNForms();
		nForms += 1; // unknown word form
		this.W_OFF = getNBits(nForms);

		int nTags = model.getNPOSTags();
		nTags += 1; // unknown word form
		this.BEG_T = nTags + 0;
		this.END_T = nTags + 1;
		this.MID_T = nTags + 2;
		nTags += 3;
		this.T_OFF = getNBits(nTags);

		int nLabels = model.getNDeprels();
		this.L_OFF = getNBits(nLabels);

		int nNodes = tree.getNNodes();

		this.w = new int[nNodes];
		this.t = new int[nNodes];

		for (int i = 0; i < nNodes; i++) {
			w[i] = model.getCodeForForm(tree.forms[i]);
			t[i] = model.getCodeForPOSTag(tree.postags[i]);
		}

		this.pred_t = new int[nNodes];
		pred_t[0] = BEG_T;
		for (int i = 1; i < nNodes; i++) {
			pred_t[i] = t[i - 1];
		}

		this.succ_t = new int[nNodes];
		succ_t[nNodes - 1] = END_T;
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

	public static FeatureVector getFeatureVector(CoNLLTree graph, Model model) {
		FeatureVector featureVector = new FeatureVector();
		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, graph);
		FeatureVectorUpdater updater = new FeatureVectorUpdater(model, featureVector);
		for (int i = 1; i < graph.getNNodes(); i++) {
			int label = model.getCodeForDeprel(graph.deprels[i]);
			featurizer.featurize(graph.heads[i], i, label, updater);
		}
		return featureVector;
	}
	private static final int TMP_OFF = getNBits(33);
	private static final int TMP_00 = 0;
	private static final int TMP_01 = 1;
	private static final int TMP_02 = 2;
	private static final int TMP_03 = 3;
	private static final int TMP_04 = 4;
	private static final int TMP_05 = 5;
	private static final int TMP_06 = 6;
	private static final int TMP_07 = 7;
	private static final int TMP_08 = 8;
	private static final int TMP_09 = 9;
	private static final int TMP_10 = 10;
	private static final int TMP_11 = 11;
	private static final int TMP_12 = 12;
	private static final int TMP_13 = 13;
	private static final int TMP_14 = 14;
	private static final int TMP_15 = 15;
	private static final int TMP_16 = 16;
	private static final int TMP_17 = 17;
	private static final int TMP_18 = 18;
	private static final int TMP_19 = 19;
	private static final int TMP_20 = 20;
	private static final int TMP_21 = 21;
	private static final int TMP_22 = 22;
	private static final int TMP_23 = 23;
	private static final int TMP_24 = 24;
	private static final int TMP_25 = 25;
	private static final int TMP_26 = 26;
	private static final int TMP_27 = 27;
	private static final int TMP_28 = 28;
	private static final int TMP_29 = 29;
	private static final int TMP_30 = 30;
	private static final int TMP_31 = 31;
	private static final int TMP_32 = 32;

	public void featurizeCore(int fst, int snd, boolean isRA, FeatureHandler h) {
		long attDist = makePair(isRA, quantize(snd - fst, LIMITS));

		int fst_t = t[fst];
		int snd_t = t[snd];

		int fst_pred_t = pred_t[fst];
		int snd_succ_t = succ_t[snd];
		int fst_succ_t = fst < snd - 1 ? succ_t[fst] : MID_T;
		int snd_pred_t = snd > fst + 1 ? pred_t[snd] : MID_T;

		for (int mid = fst + 1; mid < snd; mid++) {
			int mid_t = t[mid];
			h.handle(TMP_00, TMP_OFF, fst_t, T_OFF, snd_t, T_OFF, mid_t);
			h.handle(TMP_00, TMP_OFF, fst_t, T_OFF, snd_t, T_OFF, mid_t, T_OFF, attDist);
		}

		h.handle(TMP_01, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_t);
		h.handle(TMP_01, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_02, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_t, T_OFF, snd_succ_t);
		h.handle(TMP_02, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_t, T_OFF, snd_succ_t, T_OFF, attDist);

		h.handle(TMP_03, TMP_OFF, fst_pred_t, T_OFF, snd_t, T_OFF, snd_succ_t);
		h.handle(TMP_03, TMP_OFF, fst_pred_t, T_OFF, snd_t, T_OFF, snd_succ_t, T_OFF, attDist);

		h.handle(TMP_04, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_succ_t);
		h.handle(TMP_04, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_succ_t, T_OFF, attDist);

		h.handle(TMP_05, TMP_OFF, fst_t, T_OFF, snd_t, T_OFF, snd_succ_t);
		h.handle(TMP_05, TMP_OFF, fst_t, T_OFF, snd_t, T_OFF, snd_succ_t, T_OFF, attDist);

		h.handle(TMP_06, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_pred_t);
		h.handle(TMP_06, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_pred_t, T_OFF, attDist);

		h.handle(TMP_07, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_pred_t, T_OFF, snd_t);
		h.handle(TMP_07, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_pred_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_08, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_t);
		h.handle(TMP_08, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_09, TMP_OFF, fst_t, T_OFF, snd_pred_t, T_OFF, snd_t);
		h.handle(TMP_09, TMP_OFF, fst_t, T_OFF, snd_pred_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_10, TMP_OFF, fst_succ_t, T_OFF, snd_pred_t, T_OFF, snd_t);
		h.handle(TMP_10, TMP_OFF, fst_succ_t, T_OFF, snd_pred_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_11, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_pred_t, T_OFF, snd_t);
		h.handle(TMP_11, TMP_OFF, fst_pred_t, T_OFF, fst_t, T_OFF, snd_pred_t, T_OFF, snd_t, T_OFF, attDist);

		h.handle(TMP_12, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_t, T_OFF, snd_succ_t);
		h.handle(TMP_12, TMP_OFF, fst_t, T_OFF, fst_succ_t, T_OFF, snd_t, T_OFF, snd_succ_t, T_OFF, attDist);

		int src = isRA ? fst : snd;
		int tgt = isRA ? snd : fst;

		int src_w = w[src];
		int src_t = t[src];
		int tgt_w = w[tgt];
		int tgt_t = t[tgt];

		h.handle(TMP_13, TMP_OFF, src_w);
		h.handle(TMP_13, TMP_OFF, src_w, W_OFF, attDist);

		h.handle(TMP_14, TMP_OFF, src_w, W_OFF, src_t);
		h.handle(TMP_14, TMP_OFF, src_w, W_OFF, src_t, T_OFF, attDist);

		h.handle(TMP_15, TMP_OFF, src_w, W_OFF, src_t, T_OFF, tgt_t);
		h.handle(TMP_15, TMP_OFF, src_w, W_OFF, src_t, T_OFF, tgt_t, T_OFF, attDist);

		h.handle(TMP_16, TMP_OFF, src_w, W_OFF, src_t, T_OFF, tgt_t, T_OFF, tgt_w);
		h.handle(TMP_16, TMP_OFF, src_w, W_OFF, src_t, T_OFF, tgt_t, T_OFF, tgt_w, W_OFF, attDist);

		h.handle(TMP_17, TMP_OFF, src_w, W_OFF, tgt_w);
		h.handle(TMP_17, TMP_OFF, src_w, W_OFF, tgt_w, W_OFF, attDist);

		h.handle(TMP_18, TMP_OFF, src_w, W_OFF, tgt_t);
		h.handle(TMP_18, TMP_OFF, src_w, W_OFF, tgt_t, T_OFF, attDist);

		h.handle(TMP_19, TMP_OFF, src_t, T_OFF, tgt_w);
		h.handle(TMP_19, TMP_OFF, src_t, T_OFF, tgt_w, W_OFF, attDist);

		h.handle(TMP_20, TMP_OFF, src_t, T_OFF, tgt_w, W_OFF, tgt_t);
		h.handle(TMP_20, TMP_OFF, src_t, T_OFF, tgt_w, W_OFF, tgt_t, T_OFF, attDist);

		h.handle(TMP_21, TMP_OFF, src_t, T_OFF, tgt_t);
		h.handle(TMP_21, TMP_OFF, src_t, T_OFF, tgt_t, T_OFF, attDist);

		h.handle(TMP_22, TMP_OFF, tgt_w, W_OFF, tgt_t);
		h.handle(TMP_22, TMP_OFF, tgt_w, W_OFF, tgt_t, T_OFF, attDist);

		h.handle(TMP_23, TMP_OFF, src_t);
		h.handle(TMP_23, TMP_OFF, src_t, T_OFF, attDist);

		h.handle(TMP_24, TMP_OFF, tgt_w);
		h.handle(TMP_24, TMP_OFF, tgt_w, W_OFF, attDist);

		h.handle(TMP_25, TMP_OFF, tgt_t);
		h.handle(TMP_25, TMP_OFF, tgt_t, T_OFF, attDist);
	}

	public void featurizeLabeled(int node, int label, boolean isRA, boolean isTarget, FeatureHandler h) {
		long suffix = makePair(isRA, isTarget);

		int node_w = w[node];
		int node_t = t[node];
		int node_pred_t = pred_t[node];
		int node_succ_t = succ_t[node];

		h.handle(TMP_26, TMP_OFF, label);
		h.handle(TMP_26, TMP_OFF, label, L_OFF, suffix);

		h.handle(TMP_27, TMP_OFF, node_w, W_OFF, node_t, T_OFF, label);
		h.handle(TMP_27, TMP_OFF, node_w, W_OFF, node_t, T_OFF, label, L_OFF, suffix);

		h.handle(TMP_28, TMP_OFF, node_t, T_OFF, label);
		h.handle(TMP_28, TMP_OFF, node_t, T_OFF, label, L_OFF, suffix);

		h.handle(TMP_29, TMP_OFF, node_pred_t, T_OFF, node_t, T_OFF, label);
		h.handle(TMP_29, TMP_OFF, node_pred_t, T_OFF, node_t, T_OFF, label, L_OFF, suffix);

		h.handle(TMP_30, TMP_OFF, node_t, T_OFF, node_succ_t, T_OFF, label);
		h.handle(TMP_30, TMP_OFF, node_t, T_OFF, node_succ_t, T_OFF, label, L_OFF, suffix);

		h.handle(TMP_31, TMP_OFF, node_pred_t, T_OFF, node_t, T_OFF, node_succ_t, T_OFF, label);
		h.handle(TMP_31, TMP_OFF, node_pred_t, T_OFF, node_t, T_OFF, node_succ_t, T_OFF, label, L_OFF, suffix);

		h.handle(TMP_32, TMP_OFF, node_w, W_OFF, label);
		h.handle(TMP_32, TMP_OFF, node_w, W_OFF, label, L_OFF, suffix);
	}

	private static class FeatureVectorUpdater implements FeatureHandler {

		private final Model model;
		private final FeatureVector featureVector;

		public FeatureVectorUpdater(Model model, FeatureVector featureVector) {
			this.model = model;
			this.featureVector = featureVector;
		}

		@Override
		public void handle(long feature) {
			int index = model.getCodeForFeature(feature);
			if (index >= 0) {
				featureVector.increment(index);
			}
		}
	}

	/**
	 * Quantize an integer value relative to a partition of intervals. This
	 * returns the unique index {@code i} such that {@code value} is contained
	 * in the interval that starts at {@code limits[i]}, or -1 if value is
	 * smaller than {@code limits[0]}.
	 *
	 * @param value the value to be quantized
	 * @param limits the left endpoints of the intervals
	 * @return the index of the interval to which the specified value belongs
	 */
	private static int quantize(int value, int[] limits) {
		if (limits.length == 0 || value < limits[0]) {
			return -1;
		} else {
			for (int i = 1; i < limits.length; i++) {
				if (value < limits[i]) {
					return i - 1;
				}
			}
			return limits.length - 1;
		}
	}

	private static long makePair(boolean b, long i) {
		return i << 1 | ((long) (b ? 1 : 0));
	}

	private static long makePair(boolean b1, boolean b2) {
		long x1 = (long) (b1 ? 1 : 0);
		long x2 = (long) (b2 ? 1 : 0);
		return x2 << 1 | x1;
	}
}
