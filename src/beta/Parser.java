/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import beta.conll.CoNLLTree;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Parser {

	private final Model model;

	public Parser(Model model) {
		this.model = model;
	}

	public CoNLLTree getBestParse(CoNLLTree input) {
		int nNodes = input.getNNodes();

		ChoiceChart chart1 = new ChoiceChart(nNodes);
		ChoiceChart chart2 = new ChoiceChart(nNodes);
		ChoiceChart chart3 = new ChoiceChart(nNodes);
		ChoiceChart chart4 = new ChoiceChart(nNodes);

		DoubleChart score1 = new DoubleChart(nNodes);
		DoubleChart score2 = new DoubleChart(nNodes);
		DoubleChart score3 = new DoubleChart(nNodes);
		DoubleChart score4 = new DoubleChart(nNodes);

		EdgeScorer scorer = new EdgeScorer(model, input);

		int nLabels = model.getNDeprels();

		for (int h = 0; h < nNodes; h++) {
			score1.set(h, h, 0.0);
			score2.set(h, h, 0.0);
		}

		for (int max = 1; max < nNodes; max++) {
			for (int min = max - 1; min >= 0; min--) {
				int labMinMax = scorer.getBestLabel(min, max);
				double scoreMinMax = scorer.getScoreForBestLabel(min, max);
				int labMaxMin = scorer.getBestLabel(max, min);
				double scoreMaxMin = scorer.getScoreForBestLabel(max, min);

				// Attach-Right
				// creates an edge min -> max
				double bestScore1 = Double.NEGATIVE_INFINITY;
				int bestMid1 = -1;
				for (int mid = min + 1; mid <= max; mid++) {
					double scoreL = score1.get(min, mid - 1);
					double scoreR = score2.get(mid, max);

					double score = scoreL + scoreR + scoreMinMax;

					if (score > bestScore1) {
						bestScore1 = score;
						bestMid1 = mid;
					}
				}
				if (bestMid1 >= 0) {
					score3.set(min, max, bestScore1);
					Choice choiceL = chart1.get(min, bestMid1 - 1);
					Choice choiceR = chart2.get(bestMid1, max);
					chart3.set(min, max, new Choice(min, max, labMinMax, choiceL, choiceR));
				}

				// Attach-Left
				// creates an edge max -> min
				double bestScore2 = Double.NEGATIVE_INFINITY;
				int bestMid2 = -1;
				for (int mid = min; mid < max; mid++) {
					double scoreL = score1.get(min, mid);
					double scoreR = score2.get(mid + 1, max);

					double score = scoreL + scoreR + scoreMaxMin;

					if (score > bestScore2) {
						bestScore2 = score;
						bestMid2 = mid;
					}
				}
				if (bestMid2 >= 0) {
					score4.set(min, max, bestScore2);
					Choice choiceL = chart1.get(min, bestMid2);
					Choice choiceR = chart2.get(bestMid2 + 1, max);
					chart4.set(min, max, new Choice(max, min, labMaxMin, choiceL, choiceR));
				}

				// Complete-Right
				// creates no edge
				double bestScore3 = Double.NEGATIVE_INFINITY;
				int bestMid3 = -1;
				for (int mid = min + 1; mid <= max; mid++) {
					double scoreL = score3.get(min, mid);
					double scoreR = score1.get(mid, max);

					double score = scoreL + scoreR;

					if (score > bestScore3) {
						bestScore3 = score;
						bestMid3 = mid;
					}
				}
				if (bestMid3 >= 0) {
					score1.set(min, max, bestScore3);
					Choice choiceL = chart3.get(min, bestMid3);
					Choice choiceR = chart1.get(bestMid3, max);
					chart1.set(min, max, new Choice(choiceL, choiceR));
				}

				// Complete-Left
				// creates no edge
				double bestScore4 = Double.NEGATIVE_INFINITY;
				int bestMid4 = -1;
				for (int mid = min; mid < max; mid++) {
					double scoreL = score2.get(min, mid);
					double scoreR = score4.get(mid, max);

					double score = scoreL + scoreR;

					if (score > bestScore4) {
						bestScore4 = score;
						bestMid4 = mid;
					}
				}
				if (bestMid4 >= 0) {
					score2.set(min, max, bestScore4);
					Choice choiceL = chart2.get(min, bestMid4);
					Choice choiceR = chart4.get(bestMid4, max);
					chart2.set(min, max, new Choice(choiceL, choiceR));
				}
			}
		}

		Choice best = chart1.get(0, nNodes - 1);
		if (best != null) {
			best.updateGraph(model, input);
		}
		return input;
	}

	private static class DoubleChart {

		private final double[][] chart;

		public DoubleChart(int nNodes) {
			this.chart = new double[nNodes][];
			for (int i = 0; i < nNodes; i++) {
				chart[i] = new double[nNodes - i];
				for (int j = 0; j < chart[i].length; j++) {
					chart[i][j] = Double.NEGATIVE_INFINITY;
				}
			}
		}

		public double get(int min, int max) {
			return chart[min][max - min];
		}

		public void set(int min, int max, double value) {
			chart[min][max - min] = value;
		}
	}

	private static class Choice {

		private final int src;
		private final int tgt;
		private final int lab;
		private final Choice choiceL;
		private final Choice choiceR;

		public Choice(int src, int tgt, int lab, Choice choiceL, Choice choiceR) {
			this.src = src;
			this.tgt = tgt;
			this.lab = lab;
			this.choiceL = choiceL;
			this.choiceR = choiceR;
		}

		public Choice(Choice choiceL, Choice choiceR) {
			this.src = -1;
			this.tgt = -1;
			this.lab = -1;
			this.choiceL = choiceL;
			this.choiceR = choiceR;
		}

		public void updateGraph(Model model, CoNLLTree tree) {
			if (src >= 0) {
				tree.heads[tgt] = src;
				tree.deprels[tgt] = model.getDeprelForCode(lab);
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

		private final Choice[][] chart;

		public ChoiceChart(int nNodes) {
			this.chart = new Choice[nNodes][];
			for (int i = 0; i < nNodes; i++) {
				chart[i] = new Choice[nNodes - i];
			}
		}

		public Choice get(int min, int max) {
			return chart[min][max - min];
		}

		public void set(int min, int max, Choice choice) {
			chart[min][max - min] = choice;
		}
	}
}
