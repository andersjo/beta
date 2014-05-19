/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class FeatureIndex {

	private final TLongIntMap map;
	private int size;

	public FeatureIndex() {
		this.map = new TLongIntHashMap();
	}

	public int size() {
		return map.size() + 1;
	}

	public int put(long feature) {
		return map.put(feature, map.size() + 1);
	}

	public int get(long feature) {
		return map.get(feature);
	}

	public boolean containsKey(long feature) {
		return map.containsKey(feature);
	}
}
