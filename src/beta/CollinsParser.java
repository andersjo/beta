/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import se.uu.nlp.dlib.tree.Tree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class CollinsParser implements Parser {

	private final Model model;

	public CollinsParser(Model model) {
		this.model = model;
	}

	@Override
	public Tree getBestParse(Tree input) {
		int nNodes = input.getNNodes();

		ChoiceChart choices = new ChoiceChart(nNodes);
		DoubleChart scores = new DoubleChart(nNodes);

		EdgeScorer scorer = new EdgeScorer(model, input);

		int nLabels = model.getNLabels();

		int[][] bestLabels = new int[nNodes][nNodes];
		for (int fst = 0; fst < nNodes; fst++) {
			for (int snd = fst + 1; snd < nNodes; snd++) {
				// Edge from fst to snd (RA).
				int bestLabelRA = model.getCodeForDefaultLabel();
				double bestScoreRA = Double.NEGATIVE_INFINITY;
				for (int label = 0; label < nLabels; label++) {
					double score = scorer.getScore(fst, snd, label);
					if (score > bestScoreRA) {
						bestLabelRA = label;
						bestScoreRA = score;
					}
				}
				bestLabels[fst][snd] = bestLabelRA;

				// Edge from snd to fst (LA).
				int bestLabelLA = model.getCodeForDefaultLabel();
				double bestScoreLA = Double.NEGATIVE_INFINITY;
				for (int label = 0; label < nLabels; label++) {
					double score = scorer.getScore(snd, fst, label);
					if (score > bestScoreLA) {
						bestLabelLA = label;
						bestScoreLA = score;
					}
				}
				bestLabels[snd][fst] = bestLabelLA;
			}
		}

		for (int node = 0; node < nNodes; node++) {
			choices.set(node, node, node, null);
			scores.set(node, node, node, 0.0);
		}

		for (int max = 0; max < nNodes; max++) {
			for (int min = max - 1; min >= 0; min--) {
				// Attach-Right
				for (int mid = min + 1; mid <= max; mid++) {
					for (int src = min; src < mid; src++) {
						for (int tgt = mid; tgt <= max; tgt++) {
							double scoreL = scores.get(src, min, mid - 1);
							double scoreR = scores.get(tgt, mid, max);

							int lab = bestLabels[src][tgt];

							double score = scoreL + scoreR + scorer.getScore(src, tgt, lab);

							if (score > scores.get(src, min, max)) {
								scores.set(src, min, max, score);
								Choice choiceL = choices.get(src, min, mid - 1);
								Choice choiceR = choices.get(tgt, mid, max);
								choices.set(src, min, max, new Choice(new int[]{src, tgt, lab}, choiceL, choiceR));
							}
						}
					}
				}

				// Attach-Left
				for (int mid = min + 1; mid <= max; mid++) {
					for (int src = mid; src <= max; src++) {
						for (int tgt = min; tgt < mid; tgt++) {
							double scoreL = scores.get(tgt, min, mid - 1);
							double scoreR = scores.get(src, mid, max);

							int lab = bestLabels[src][tgt];

							double score = scoreL + scoreR + scorer.getScore(src, tgt, lab);

							if (score > scores.get(src, min, max)) {
								scores.set(src, min, max, score);
								Choice choiceL = choices.get(tgt, min, mid - 1);
								Choice choiceR = choices.get(src, mid, max);
								choices.set(src, min, max, new Choice(new int[]{src, tgt, lab}, choiceL, choiceR));
							}
						}
					}
				}
			}
		}

		Choice bestChoice = choices.get(0, 0, nNodes - 1);
		if (bestChoice != null) {
			bestChoice.updateGraph(model, input);
		}
		return input;
	}

	private static class DoubleChart {

		final double[][][] chart;

		public DoubleChart(int nNodes) {
			this.chart = new double[nNodes][nNodes][nNodes];
			for (int i = 0; i < nNodes; i++) {
				for (int j = 0; j < nNodes; j++) {
					for (int k = 0; k < nNodes; k++) {
						chart[i][j][k] = Double.NEGATIVE_INFINITY;
					}
				}
			}
		}

		public double get(int head, int min, int max) {
			return chart[head][min][max];
		}

		public void set(int head, int min, int max, double value) {
			chart[head][min][max] = value;
		}
	}

	private static class Choice {

		public final int[] edge;
		public final Choice choiceL;
		public final Choice choiceR;

		public Choice(int[] edge, Choice choiceL, Choice choiceR) {
			this.edge = edge;
			this.choiceL = choiceL;
			this.choiceR = choiceR;
		}

		public void updateGraph(Model model, Tree tree) {
			if (edge != null) {
				int src = edge[0];
				int tgt = edge[1];
				int lab = edge[2];
				tree.heads[tgt] = src;
				tree.deprels[tgt] = model.getLabelForCode(lab);
			}
			if (choiceL != null) {
				choiceL.updateGraph(model, tree);
			}
			if (choiceR != null) {
				choiceR.updateGraph(model, tree);
			}
		}
	}

	private static class ChoiceChart {

		private final Choice[][][] chart;

		public ChoiceChart(int nNodes) {
			this.chart = new Choice[nNodes][nNodes][nNodes];
		}

		public Choice get(int head, int min, int max) {
			return chart[head][min][max];
		}

		public void set(int head, int min, int max, Choice choice) {
			chart[head][min][max] = choice;
		}
	}
}
