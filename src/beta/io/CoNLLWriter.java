/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class CoNLLWriter {

	/**
	 * The field separator.
	 */
	private static final String FIELD_SEPARATOR = "\t";
	/**
	 * The internal writer.
	 */
	private final Writer writer;

	/**
	 * Constructs a new writer with the specified output stream.
	 *
	 * @param columns the columns of the data format
	 * @param os an output stream
	 */
	public CoNLLWriter(OutputStream os) {
		this.writer = new BufferedWriter(new OutputStreamWriter(os));
	}

	public CoNLLWriter(File file) throws IOException {
		this.writer = new BufferedWriter(new FileWriter(file));
	}

	public CoNLLWriter(String fileName) throws IOException {
		this(new File(fileName));
	}

	/**
	 * Constructs a new writer for the standard output stream.
	 *
	 * @param columns
	 */
	public CoNLLWriter() {
		this(System.out);
	}

	/**
	 * Writes the next tree to the output stream.
	 *
	 * @param tree the tree to write to the output stream
	 */
	public void write(CoNLLTree tree) throws IOException {
		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < tree.getNNodes(); i++) {
			sb.append(i);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.forms[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.lemmas[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.cpostags[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.postags[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.feats[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.heads[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append(tree.deprels[i]);
			sb.append(FIELD_SEPARATOR);
			sb.append("_");
			sb.append(FIELD_SEPARATOR);
			sb.append("_");
			sb.append("\n");
		}

		sb.append("\n");

		writer.write(sb.toString());
	}

	public void close() throws IOException {
		writer.close();
	}

	public static void main(String[] args) throws Exception {
		CoNLLReader reader = new CoNLLReader(args[0]);
		CoNLLWriter writer;
		if (args.length == 1) {
			writer = new CoNLLWriter();
		} else {
			writer = new CoNLLWriter(new FileOutputStream(args[1]));
		}
		try {
			CoNLLTree tree;
			while ((tree = reader.read()) != null) {
				writer.write(tree);
			}
		} finally {
			reader.close();
			writer.close();
		}
	}
}
