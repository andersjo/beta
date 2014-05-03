/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class FeatureVector {

	private final static double ONE = 1.0;
	private final TIntList features;

	public FeatureVector() {
		this.features = new TIntLinkedList();
	}

	public void increment(int index) {
		features.add(index);
	}

	public void addTo(double[] parameters) {
		addTo(ONE, parameters);
	}

	public void addTo(double alpha, double[] parameters) {
		TIntIterator it = features.iterator();
		while (it.hasNext()) {
			parameters[it.next()] += alpha;
		}
	}

	public void subtractFrom(double[] parameters) {
		subtractFrom(ONE, parameters);
	}

	public void subtractFrom(double alpha, double[] parameters) {
		TIntIterator it = features.iterator();
		while (it.hasNext()) {
			parameters[it.next()] -= alpha;
		}
	}

	public double getScore(double[] parameters) {
		double score = 0.0;
		TIntIterator it = features.iterator();
		while (it.hasNext()) {
			score += parameters[it.next()];
		}
		return score;
	}
}
