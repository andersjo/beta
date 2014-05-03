/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@liu.se>
 */
public class FeatureTrie implements Serializable {

	private static final int NO_ELEMENT = -1;
	private transient FeatureNode[] roots;
	private transient int nEntries;

	public FeatureTrie(int size) {
		this.roots = makeRoots(size);
	}

	private static FeatureNode[] makeRoots(int size) {
		FeatureNode[] tmp = new FeatureNode[size];
		for (int i = 0; i < size; i++) {
			tmp[i] = new FeatureNode();
		}
		return tmp;
	}

	public int getNEntries() {
		return nEntries;
	}

	public int add(int... key) {
		assert key.length > 0;
		assert 0 <= key[0] && key[0] < roots.length;
		FeatureNode node = roots[key[0]];
		for (int i = 1; i < key.length; i++) {
			FeatureNode child = node.getChild(key[i]);
			if (child == null) {
				node = node.addChild(key[i]);
			} else {
				node = child;
			}
		}
		if (node.index == NO_ELEMENT) {
			node.index = nEntries;
			nEntries++;
		}
		return node.index;
	}

	public int get(int... key) {
		assert key.length > 0;
		assert 0 <= key[0] && key[0] < roots.length;
		FeatureNode node = roots[key[0]];
		for (int i = 1; i < key.length; i++) {
			FeatureNode child = node.getChild(key[i]);
			if (child == null) {
				return NO_ELEMENT;
			} else {
				node = child;
			}
		}
		return node.index;
	}

	/**
	 * Returns the entries of this trie in the order in which they were added.
	 *
	 * @return the entries of this trie in the order in which they were added
	 */
	private int[][] getEntries() {
		int[][] codes = new int[nEntries][];

		Deque<TIntObjectIterator<FeatureNode>> agenda = new LinkedList<TIntObjectIterator<FeatureNode>>();

		TIntObjectMap<FeatureNode> rootMap = new TIntObjectHashMap<FeatureNode>();
		for (int i = 0; i < roots.length; i++) {
			rootMap.put(i, roots[i]);
		}
		agenda.addLast(rootMap.iterator());

		while (!agenda.isEmpty()) {
			TIntObjectIterator<FeatureNode> iterator = agenda.getLast();
			if (!iterator.hasNext()) {
				agenda.removeLast();
			} else {
				iterator.advance();
				FeatureNode node = iterator.value();
				if (node.index != NO_ELEMENT) {
					int[] code = new int[agenda.size()];
					int i = 0;
					for (TIntObjectIterator<FeatureNode> it : agenda) {
						code[i] = it.key();
						i++;
					}
					codes[node.index] = code;
				}
				agenda.addLast(node.children.iterator());
			}
		}

		return codes;
	}

	private static class FeatureNode {

		private TIntObjectHashMap<FeatureNode> children;
		public int index;

		public FeatureNode() {
			this.children = new TIntObjectHashMap<FeatureNode>();
			this.index = NO_ELEMENT;
		}

		public FeatureNode addChild(int childIndex) {
			FeatureNode child = new FeatureNode();
			children.put(childIndex, child);
			return child;
		}

		public FeatureNode getChild(int key) {
			return children.get(key);
		}
	}

	private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeInt(roots.length);
		oos.writeInt(nEntries);
		for (int[] feature : getEntries()) {
			oos.writeObject(feature);
		}
	}

	private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		this.roots = makeRoots(ois.readInt());
		int nEntriesToRead = ois.readInt();
		for (int i = 0; i < nEntriesToRead; i++) {
			add((int[]) ois.readObject());
		}
	}
}
