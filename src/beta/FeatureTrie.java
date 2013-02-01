/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.*;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class FeatureTrie {

	private static final TIntObjectHashMap<FeatureNode> NULL_MAP = new TIntObjectHashMap<FeatureNode>();
	private static final int NO_ELEMENT = -1;
	private final FeatureNode root;
	private final FeatureNode sentinel;
	private int nEntries;
	private FeatureNode last;

	public FeatureTrie() {
		this.root = new FeatureNode(null, 0);
		this.sentinel = new FeatureNode(null, 0);
		this.last = sentinel;
	}

	public int getNEntries() {
		return nEntries;
	}

	public int add(int[] key) {
		FeatureNode node = root;
		for (int i = 0; i < key.length; i++) {
			FeatureNode child = node.getChild(key[i]);
			if (child == null) {
				node = node.addChild(key[i]);
			} else {
				node = child;
			}
		}
		if (node.index == NO_ELEMENT) {
			node.index = nEntries;
			last.next = node;
			last = node;
			nEntries++;
		}
		return node.index;
	}

	public int get(int[] key) {
		FeatureNode node = root;
		for (int i = 0; i < key.length; i++) {
			FeatureNode child = node.getChild(key[i]);
			if (child == null) {
				return NO_ELEMENT;
			} else {
				node = child;
			}
		}
		return node.index;
	}

	private static class FeatureNode {

		private final FeatureNode parent;
		private final int childIndex;
		private TIntObjectHashMap<FeatureNode> children;
		public int index;
		public FeatureNode next;

		public FeatureNode(FeatureNode parent, int childIndex) {
			this.parent = parent;
			this.childIndex = childIndex;
			this.children = NULL_MAP;
			this.index = NO_ELEMENT;
		}

		public FeatureNode addChild(int childIndex) {
			if (children == NULL_MAP) {
				children = new TIntObjectHashMap<FeatureNode>();
			}
			FeatureNode child = new FeatureNode(this, childIndex);
			children.put(childIndex, child);
			return child;
		}

		public FeatureNode getChild(int key) {
			return children.get(key);
		}

		@Override
		public String toString() {
			if (parent == null) {
				return "";
			} else {
				if (parent.parent == null) {
					return Integer.toString(childIndex);
				} else {
					return parent.toString() + " " + Integer.toString(childIndex);
				}
			}
		}

		public int[] getKey() {
			FeatureNode node = this;
			int depth = 0;
			while (node.parent != null) {
				node = node.parent;
				depth++;
			}

			int[] key = new int[depth];
			node = this;
			while (node.parent != null) {
				depth--;
				key[depth] = node.childIndex;
				node = node.parent;
			}

			return key;
		}
	}

	public void save(String fileName) throws IOException {
		save(new File(fileName));
	}

	public void save(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		save(bw);
		bw.close();
	}

	public void save(BufferedWriter bw) throws IOException {
		bw.write(Integer.toString(nEntries));
		bw.newLine();
		FeatureNode current = sentinel.next;
		while (current != null) {
			bw.write(current.toString());
			bw.newLine();
			current = current.next;
		}
	}

	public static FeatureTrie load(String fileName) throws IOException {
		return load(new File(fileName));
	}

	public static FeatureTrie load(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		FeatureTrie featureTrie = load(br);
		br.close();
		return featureTrie;
	}

	public static FeatureTrie load(BufferedReader br) throws IOException {
		FeatureTrie featureTrie = new FeatureTrie();
		int nEntries = Integer.parseInt(br.readLine());
		for (int i = 0; i < nEntries; i++) {
			String tokens[] = br.readLine().split(" ");
			int[] key = new int[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				key[j] = Integer.parseInt(tokens[j]);
			}
			featureTrie.add(key);
		}
		return featureTrie;
	}
}
