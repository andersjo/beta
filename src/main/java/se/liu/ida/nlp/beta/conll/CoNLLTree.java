/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta.conll;

/**
 * A dependency tree.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class CoNLLTree {

	/**
	 * Field #2: Word form or punctuation symbol.
	 */
	public final String[] forms;
	/**
	 * Field #3: Lemma or stem of word forms, or an underscore if not available.
	 */
	public final String[] lemmas;
	/**
	 * Field #4: Coarse-grained part-of-speech tag.
	 */
	public final String[] cpostags;
	/**
	 * Field #5: Fine-grained part-of-speech tag, or identical to the
	 * coarse-grained part-of-speech tag if not available.
	 */
	public final String[] postags;
	/**
	 * Field #6: Unordered set of syntactic and/or morphological features,
	 * separated by a vertical bar (|), or an underscore if not available.
	 */
	public final String[] feats;
	/**
	 * Field #7: Head of the current token, which is either a token identifier
	 * or zero (0) in case the current token does not have a head.
	 */
	public final int[] heads;
	/**
	 * Field #8: Dependency relation to the head, or ROOT in case the current
	 * token does not have a head.
	 */
	public final String[] deprels;

	/**
	 * Creates an empty tree.
	 *
	 * @param nNodes the number of nodes of the new tree
	 */
	public CoNLLTree(int nNodes) {
		this.forms = new String[nNodes];
		this.lemmas = new String[nNodes];
		this.cpostags = new String[nNodes];
		this.postags = new String[nNodes];
		this.feats = new String[nNodes];
		this.heads = new int[nNodes];
		this.deprels = new String[nNodes];
	}

	/**
	 * Creates a new tree by copying an old tree.
	 *
	 * @param tree the old tree
	 */
	public CoNLLTree(CoNLLTree tree) {
		this(tree.forms.length);
		System.arraycopy(tree.forms, 0, forms, 0, forms.length);
		System.arraycopy(tree.lemmas, 0, lemmas, 0, lemmas.length);
		System.arraycopy(tree.cpostags, 0, cpostags, 0, cpostags.length);
		System.arraycopy(tree.postags, 0, postags, 0, postags.length);
		System.arraycopy(tree.feats, 0, feats, 0, feats.length);
		System.arraycopy(tree.heads, 0, heads, 0, heads.length);
		System.arraycopy(tree.deprels, 0, deprels, 0, deprels.length);
	}

	/**
	 * Returns the number of nodes of this tree.
	 *
	 * @return the number of nodes of this tree
	 */
	public int getNNodes() {
		return forms.length;
	}
}
