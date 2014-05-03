/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 * Read dependency trees in a CoNLL-like format from a character-input stream.
 *
 * <p>For the sake of efficiency, the input stream is read from using a
 * BufferedReader.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class CoNLLReader {

	/**
	 * The field separator.
	 */
	private static final String FIELD_SEPARATOR = "\t";
	/**
	 * The data columns of the root node.
	 */
	private static final String ROOT_FORM = "<ROOT>";
	private static final String ROOT_LEMMA = "<ROOT>";
	private static final String ROOT_CPOSTAG = "<ROOT>";
	private static final String ROOT_POSTAG = "<ROOT>";
	private static final String ROOT_FEATS = "";
	private static final int ROOT_HEAD = 0;
	private static final String ROOT_DEPREL = "ROOT";
	/**
	 * The internal reader.
	 */
	private final BufferedReader reader;

	/**
	 * Creates a tree reader.
	 *
	 * @param reader the low-level reader
	 */
	public CoNLLReader(Reader reader) {
		this.reader = new BufferedReader(reader);
	}

	/**
	 * Creates a tree reader that reads from the specified file.
	 *
	 * @param file the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public CoNLLReader(File file) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(file));
	}

	/**
	 * Creates a tree reader that reads from the file with the specified name.
	 *
	 * @param fileName the name of the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public CoNLLReader(String fileName) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(fileName));
	}

	/**
	 * Reads a dependency tree. A dependency tree is considered to be terminated
	 * by a blank line.
	 *
	 * <p>The fields are added as feature values to the nodes in the tree. The
	 * fields that specify the position of a token and the position of its head
	 * are removed; the latter is instead reflected in the tree structure.
	 *
	 * @return the dependency tree, or null if no tree could be read
	 * @throws IOException if an I/O error occurs
	 */
	public CoNLLTree read() throws IOException {
		String line = reader.readLine();
		if (line == null || line.isEmpty()) {
			return null;
		} else {
			List<String> lines = new LinkedList<String>();
			do {
				lines.add(line);
			} while ((line = reader.readLine()) != null && !line.isEmpty());

			CoNLLTree tree = new CoNLLTree(lines.size() + 1);

			tree.forms[0] = ROOT_FORM;
			tree.lemmas[0] = ROOT_LEMMA;
			tree.cpostags[0] = ROOT_CPOSTAG;
			tree.postags[0] = ROOT_POSTAG;
			tree.feats[0] = ROOT_FEATS;
			tree.heads[0] = ROOT_HEAD;
			tree.deprels[0] = ROOT_DEPREL;

			int id = 1;
			for (String currentLine : lines) {
				String[] fields = currentLine.split(FIELD_SEPARATOR);

				tree.forms[id] = fields[1];
				tree.lemmas[id] = fields[2];
				tree.cpostags[id] = fields[3];
				tree.postags[id] = fields[4];
				tree.feats[id] = fields[5];
				tree.heads[id] = Integer.parseInt(fields[6]);
				tree.deprels[id] = fields[7];

				id++;
			}

			return tree;
		}
	}

	/**
	 * Closes the stream.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void close() throws IOException {
		reader.close();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		CoNLLReader reader = new CoNLLReader(args[0]);
		try {
			int nTrees = 0;
			CoNLLTree tree;
			while ((tree = reader.read()) != null) {
				nTrees++;
			}
			System.out.println(nTrees);
		} finally {
			reader.close();
		}
	}
}
