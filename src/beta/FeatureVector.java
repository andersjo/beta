/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class FeatureVector {

	private final static double ONE = 1.0;
	private final TIntList pos;
	private final TIntList neg;

	public FeatureVector() {
		this.pos = new TIntLinkedList();
		this.neg = new TIntLinkedList();
	}

	public void increment(int index) {
		pos.add(index);
	}

	public void update(double alpha, double[] parameters) {
		TIntIterator itPos = pos.iterator();
		while (itPos.hasNext()) {
			parameters[itPos.next()] += alpha;
		}

		TIntIterator itNeg = neg.iterator();
		while (itNeg.hasNext()) {
			parameters[itNeg.next()] -= alpha;
		}
	}

	public double getScore(double[] parameters) {
		double score = 0.0;

		TIntIterator itPos = pos.iterator();
		while (itPos.hasNext()) {
			score += parameters[itPos.next()];
		}

		TIntIterator itNeg = neg.iterator();
		while (itNeg.hasNext()) {
			score -= parameters[itNeg.next()];
		}

		return score;
	}

	// ||x|| = \sqrt{xx}, so ||x||^2 = xx
	public double getSquaredEuclidean() {
		TIntDoubleMap map = getMap();
		
		double squaredEuclidean = 0.0;
		
		for (Double value : map.values()) {
			squaredEuclidean += value * value;
		}
		
		return squaredEuclidean;
	}

	public double getDotProduct(FeatureVector otherVector) {
		TIntDoubleMap map1 = this.getMap();
		TIntDoubleMap map2 = otherVector.getMap();

		double dotProduct = 0.0;

		TIntDoubleIterator it = map1.iterator();
		while (it.hasNext()) {
			it.advance();
			int key = it.key();
			if (map2.containsKey(key)) {
				dotProduct += it.value() * map2.get(key);
			}
		}

		return dotProduct;
	}

	private TIntDoubleMap getMap() {
		TIntDoubleMap map = new TIntDoubleHashMap();

		TIntIterator itPos = pos.iterator();
		while (itPos.hasNext()) {
			map.adjustOrPutValue(itPos.next(), ONE, ONE);
		}

		TIntIterator itNeg = neg.iterator();
		while (itNeg.hasNext()) {
			map.adjustOrPutValue(itNeg.next(), -ONE, -ONE);
		}

		return map;
	}

	public static FeatureVector getDelta(FeatureVector vector1, FeatureVector vector2) {
		FeatureVector delta = new FeatureVector();
		delta.pos.addAll(vector1.pos);
		delta.neg.addAll(vector1.neg);
		delta.neg.addAll(vector2.pos);
		delta.pos.addAll(vector2.neg);
		return delta;
	}
}
