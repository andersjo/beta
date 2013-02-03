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
	private final int[] form;
	private final int[] postag;
	private final int[] pred_postag;
	private final int[] succ_postag;
	private final int[] cpostag;
	private final int[] pred_cpostag;
	private final int[] succ_cpostag;
	private final int[] lemma;
	private final int[] pred_lemma;
	private final int[] succ_lemma;
	private final boolean useExtendedFeatures = false;

	public EdgeFeaturizer(Model model, CoNLLTree tree) {
		int nNodes = tree.getNNodes();

		this.form = new int[nNodes];
		this.lemma = new int[nNodes];
		this.cpostag = new int[nNodes];
		this.postag = new int[nNodes];

		for (int i = 0; i < nNodes; i++) {
			form[i] = model.getCodeForForm(tree.forms[i]);
			lemma[i] = model.getCodeForForm(tree.lemmas[i]);
			cpostag[i] = model.getCodeForPOSTag(tree.cpostags[i]);
			postag[i] = model.getCodeForPOSTag(tree.postags[i]);
		}

		this.pred_lemma = new int[nNodes];
		this.pred_cpostag = new int[nNodes];
		this.pred_postag = new int[nNodes];

		pred_lemma[0] = BEG;
		pred_cpostag[0] = BEG;
		pred_postag[0] = BEG;
		for (int i = 1; i < nNodes; i++) {
			pred_lemma[i] = lemma[i - 1];
			pred_cpostag[i] = cpostag[i - 1];
			pred_postag[i] = postag[i - 1];
		}

		this.succ_lemma = new int[nNodes];
		this.succ_cpostag = new int[nNodes];
		this.succ_postag = new int[nNodes];

		succ_lemma[nNodes - 1] = END;
		succ_cpostag[nNodes - 1] = END;
		succ_postag[nNodes - 1] = END;
		for (int i = nNodes - 2; i >= 0; i--) {
			succ_lemma[i] = lemma[i + 1];
			succ_cpostag[i] = cpostag[i + 1];
			succ_postag[i] = postag[i + 1];
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
			int label = model.getCodeForDeprel(graph.deprels[i]);
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

		featurize1(0, postag, pred_postag, succ_postag, fst, snd, isRA, attDist, h);
		featurize2(13, form, postag, fst, snd, isRA, attDist, h);

		if (useExtendedFeatures) {
			featurize1(33, cpostag, pred_cpostag, succ_cpostag, fst, snd, isRA, attDist, h);
			featurize2(46, form, cpostag, fst, snd, isRA, attDist, h);
			featurize2(59, lemma, postag, fst, snd, isRA, attDist, h);
			featurize2(72, lemma, cpostag, fst, snd, isRA, attDist, h);
		}
	}

	private void featurize1(int offset, int[] x, int[] pred_x, int[] succ_x, int fst, int snd, boolean isRA, int attDist, FeatureHandler h) {
		int fst_x = x[fst];
		int snd_x = x[snd];

		int fst_pred_x = pred_x[fst];
		int snd_succ_x = succ_x[snd];
		int fst_succ_x = fst < snd - 1 ? succ_x[fst] : MID;
		int snd_pred_x = snd > fst + 1 ? pred_x[snd] : MID;

		for (int mid = fst + 1; mid < snd; mid++) {
			int mid_t = x[mid];
			h.handle(offset + 0, fst_x, snd_x, mid_t);
			h.handle(offset + 0, fst_x, snd_x, mid_t, attDist);
		}

		h.handle(offset + 1, fst_pred_x, fst_x, snd_x);
		h.handle(offset + 1, fst_pred_x, fst_x, snd_x, attDist);

		h.handle(offset + 2, fst_pred_x, fst_x, snd_x, snd_succ_x);
		h.handle(offset + 2, fst_pred_x, fst_x, snd_x, snd_succ_x, attDist);

		h.handle(offset + 3, fst_pred_x, snd_x, snd_succ_x);
		h.handle(offset + 3, fst_pred_x, snd_x, snd_succ_x, attDist);

		h.handle(offset + 4, fst_pred_x, fst_x, snd_succ_x);
		h.handle(offset + 4, fst_pred_x, fst_x, snd_succ_x, attDist);

		h.handle(offset + 5, fst_x, snd_x, snd_succ_x);
		h.handle(offset + 5, fst_x, snd_x, snd_succ_x, attDist);

		h.handle(offset + 6, fst_x, fst_succ_x, snd_pred_x);
		h.handle(offset + 6, fst_x, fst_succ_x, snd_pred_x, attDist);

		h.handle(offset + 7, fst_x, fst_succ_x, snd_pred_x, snd_x);
		h.handle(offset + 7, fst_x, fst_succ_x, snd_pred_x, snd_x, attDist);

		h.handle(offset + 8, fst_x, fst_succ_x, snd_x);
		h.handle(offset + 8, fst_x, fst_succ_x, snd_x, attDist);

		h.handle(offset + 9, fst_x, snd_pred_x, snd_x);
		h.handle(offset + 9, fst_x, snd_pred_x, snd_x, attDist);

		h.handle(offset + 10, fst_succ_x, snd_pred_x, snd_x);
		h.handle(offset + 10, fst_succ_x, snd_pred_x, snd_x, attDist);

		h.handle(offset + 11, fst_pred_x, fst_x, snd_pred_x, snd_x);
		h.handle(offset + 11, fst_pred_x, fst_x, snd_pred_x, snd_x, attDist);

		h.handle(offset + 12, fst_x, fst_succ_x, snd_x, snd_succ_x);
		h.handle(offset + 12, fst_x, fst_succ_x, snd_x, snd_succ_x, attDist);
	}

	private void featurize2(int offset, int[] a, int[] b, int fst, int snd, boolean isRA, int attDist, FeatureHandler h) {
		int src = isRA ? fst : snd;
		int tgt = isRA ? snd : fst;

		int src_a = a[src];
		int src_b = b[src];
		int tgt_a = a[tgt];
		int tgt_b = b[tgt];

		h.handle(offset + 0, src_a);
		h.handle(offset + 0, src_a, attDist);

		h.handle(offset + 1, src_a, src_b);
		h.handle(offset + 1, src_a, src_b, attDist);

		h.handle(offset + 2, src_a, src_b, tgt_b);
		h.handle(offset + 2, src_a, src_b, tgt_b, attDist);

		h.handle(offset + 3, src_a, src_b, tgt_b, tgt_a);
		h.handle(offset + 3, src_a, src_b, tgt_b, tgt_a, attDist);

		h.handle(offset + 4, src_a, tgt_a);
		h.handle(offset + 4, src_a, tgt_a, attDist);

		h.handle(offset + 5, src_a, tgt_b);
		h.handle(offset + 5, src_a, tgt_b, attDist);

		h.handle(offset + 6, src_b, tgt_a);
		h.handle(offset + 6, src_b, tgt_a, attDist);

		h.handle(offset + 7, src_b, tgt_a, tgt_b);
		h.handle(offset + 7, src_b, tgt_a, tgt_b, attDist);

		h.handle(offset + 8, src_b, tgt_b);
		h.handle(offset + 8, src_b, tgt_b, attDist);

		h.handle(offset + 9, tgt_a, tgt_b);
		h.handle(offset + 9, tgt_a, tgt_b, attDist);

		h.handle(offset + 10, src_b);
		h.handle(offset + 10, src_b, attDist);

		h.handle(offset + 11, tgt_a);
		h.handle(offset + 11, tgt_a, attDist);

		h.handle(offset + 12, tgt_b);
		h.handle(offset + 12, tgt_b, attDist);
	}

	public void featurizeLabeled(int node, int label, boolean isRA, boolean isTarget, FeatureHandler h) {
		int suffix = (isRA ? 0 : 10) + (isTarget ? 0 : 1);

		int node_w = form[node];
		int node_t = postag[node];
		int node_pred_t = pred_postag[node];
		int node_succ_t = succ_postag[node];

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
