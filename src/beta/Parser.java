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

		// Initialize the charts.

		ChoiceChart chart1 = new ChoiceChart(nNodes);
		ChoiceChart chart2 = new ChoiceChart(nNodes);
		ChoiceChart chart3 = new ChoiceChart(nNodes);
		ChoiceChart chart4 = new ChoiceChart(nNodes);

		DoubleChart score1 = new DoubleChart(nNodes);
		DoubleChart score2 = new DoubleChart(nNodes);
		DoubleChart score3 = new DoubleChart(nNodes);
		DoubleChart score4 = new DoubleChart(nNodes);

		EdgeScorer scorer = new EdgeScorer(model, input);

		// Fill the charts bottom-up, starting with spans of length 2. Note
		// that at this point, every span of length 1 has score 0.0.

		for (int max = 1; max < nNodes; max++) {
			for (int min = max - 1; min >= 0; min--) {
				score1.set(min, max, Double.NEGATIVE_INFINITY);
				score2.set(min, max, Double.NEGATIVE_INFINITY);
				score3.set(min, max, Double.NEGATIVE_INFINITY);
				score4.set(min, max, Double.NEGATIVE_INFINITY);

				double bestScoreMinMax = scorer.getBestScore(min, max);
				int bestLabelMinMax = scorer.getBestLabel(min, max);

				// Attach-Right
				// creates an edge min -> max
				for (int mid = min + 1; mid <= max; mid++) {
					double scoreL = score1.get(min, mid - 1);
					double scoreR = score2.get(mid, max);

					double score = scoreL + scoreR + bestScoreMinMax;

					if (score > score3.get(min, max)) {
						score3.set(min, max, score);
						Choice choiceL = chart1.get(min, mid - 1);
						Choice choiceR = chart2.get(mid, max);
						chart3.set(min, max, new Choice(min, max, bestLabelMinMax, choiceL, choiceR));
					}
				}

				double bestScoreMaxMin = scorer.getBestScore(max, min);
				int bestLabelMaxMin = scorer.getBestLabel(max, min);

				// Attach-Left
				// creates an edge max -> min
				for (int mid = min + 1; mid <= max; mid++) {
					double scoreL = score1.get(min, mid - 1);
					double scoreR = score2.get(mid, max);

					double score = scoreL + scoreR + bestScoreMaxMin;

					if (score > score4.get(min, max)) {
						score4.set(min, max, score);
						Choice choiceL = chart1.get(min, mid - 1);
						Choice choiceR = chart2.get(mid, max);
						chart4.set(min, max, new Choice(max, min, bestLabelMaxMin, choiceL, choiceR));
					}
				}

				// Complete-Right
				// creates no edge
				for (int mid = min + 1; mid <= max; mid++) {
					double scoreL = score3.get(min, mid);
					double scoreR = score1.get(mid, max);

					double score = scoreL + scoreR;

					if (score > score1.get(min, max)) {
						score1.set(min, max, score);
						Choice choiceL = chart3.get(min, mid);
						Choice choiceR = chart1.get(mid, max);
						chart1.set(min, max, new Choice(choiceL, choiceR));
					}
				}

				// Complete-Left
				// creates no edge
				for (int mid = min; mid < max; mid++) {
					double scoreL = score2.get(min, mid);
					double scoreR = score4.get(mid, max);

					double score = scoreL + scoreR;

					if (score > score2.get(min, max)) {
						score2.set(min, max, score);
						Choice choiceL = chart2.get(min, mid);
						Choice choiceR = chart4.get(mid, max);
						chart2.set(min, max, new Choice(choiceL, choiceR));
					}
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
