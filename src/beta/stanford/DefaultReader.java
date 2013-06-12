/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.stanford;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class DefaultReader {

	/**
	 * The internal reader.
	 */
	protected final BufferedReader reader;

	/**
	 * Creates a reader.
	 *
	 * @param reader the low-level reader
	 */
	public DefaultReader(Reader reader) {
		this.reader = new BufferedReader(reader);
	}

	/**
	 * Creates a reader that reads from the specified file.
	 *
	 * @param file the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public DefaultReader(File file) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(file));
	}

	/**
	 * Creates a reader that reads from the file with the specified name.
	 *
	 * @param fileName the name of the file to read from
	 * @throws FileNotFoundException if the specified file does not exist, is a
	 * directory rather than a regular file, or for some other reason cannot be
	 * opened for reading
	 */
	public DefaultReader(String fileName) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(fileName));
	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

	public List<String> readLines() throws IOException {
		String line;
		if ((line = reader.readLine()) == null || line.isEmpty()) {
			return null;
		} else {
			List<String> lines = new LinkedList<String>();
			do {
				lines.add(line);
			} while (!((line = reader.readLine()) == null || line.isEmpty()));
			return lines;
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
}
