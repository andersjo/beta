/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta.stanford;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class StanfordEvaluator {

	private int nEdges1;
	private int nEdges2;
	private int nCommonEdges;

	public StanfordEvaluator() {
		this.nEdges1 = 0;
		this.nEdges2 = 0;
		this.nCommonEdges = 0;
	}

	public void add(Reader reader1, Reader reader2) throws IOException {
		DefaultReader r1 = new DefaultReader(reader1);
		DefaultReader r2 = new DefaultReader(reader2);
		add(r1, r2);
		r1.close();
		r2.close();
	}

	public void add(File file1, File file2) throws IOException {
		DefaultReader r1 = new DefaultReader(file1);
		DefaultReader r2 = new DefaultReader(file2);
		add(r1, r2);
		r1.close();
		r2.close();
	}

	public void add(String fileName1, String fileName2) throws IOException {
		DefaultReader r1 = new DefaultReader(fileName1);
		DefaultReader r2 = new DefaultReader(fileName2);
		add(r1, r2);
		r1.close();
		r2.close();
	}

	private void add(DefaultReader reader1, DefaultReader reader2) throws IOException {
		Set<Edge> edges1;
		Set<Edge> edges2;
		while ((edges1 = readEdges(reader1)) != null && (edges2 = readEdges(reader2)) != null) {
			nEdges1 += edges1.size();
			nEdges2 += edges2.size();

			// Compute the intersection of the two sets.
			edges1.retainAll(edges2);

			nCommonEdges += edges1.size();
		}
	}

	public double getPrecision() {
		return (double) nCommonEdges / (double) nEdges1;
	}

	public double getRecall() {
		return (double) nCommonEdges / (double) nEdges2;
	}

	public double getF1() {
		double p = getPrecision();
		double r = getRecall();
		return 2.0 * p * r / (p + r);
	}

	private static Set<Edge> readEdges(DefaultReader reader) throws IOException {
		List<String> lines = reader.readLines();
		if (lines == null) {
			return null;
		} else {
			Set<Edge> result = new HashSet<Edge>();
			for (String line : lines) {
				String[] tokens = tokenize(line);
				if (tokens == null) {
					throw new IOException();
				} else {
					result.add(new Edge(tokens));
				}
			}
			return result;
		}
	}

	private static String[] tokenize(String line) {
		String[] tmp = tokenize(line, new String[]{"(", ", ", ")"});
		if (tmp == null) {
			return null;
		}
		String[] tokens = new String[5];
		int i = tmp[1].lastIndexOf("-");
		if (i < 0) {
			return null;
		}
		int j = tmp[2].lastIndexOf("-");
		if (j < 0) {
			return null;
		}
		tokens[0] = tmp[0];
		tokens[1] = tmp[1].substring(0, i);
		tokens[2] = tmp[1].substring(i + 1);
		tokens[3] = tmp[2].substring(0, j);
		tokens[4] = tmp[2].substring(j + 1);
		return tokens;
	}

	private static String[] tokenize(String line, String[] stop) {
		String[] tokens = new String[stop.length + 1];
		for (int i = 0; i < stop.length; i++) {
			int j = line.indexOf(stop[i]);
			if (j < 0) {
				return null;
			} else {
				tokens[i] = line.substring(0, j);
				line = line.substring(j + stop[i].length());
			}
		}
		tokens[stop.length] = line;
		return tokens;
	}

	private static class Edge {

		private final String deprel;
		private final int src;
		private final int tgt;

		public Edge(String[] tokens) {
			// tokens[0] = dependency relation
			// tokens[1] = word form of the source word
			// tokens[2] = index of the source word
			// tokens[3] = word form of the target word
			// tokens[4] = index of the target word

			this.deprel = tokens[0];
			this.src = Integer.parseInt(tokens[2]);
			this.tgt = Integer.parseInt(tokens[4]);
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 79 * hash + (this.deprel != null ? this.deprel.hashCode() : 0);
			hash = 79 * hash + this.src;
			hash = 79 * hash + this.tgt;
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Edge other = (Edge) obj;
			if ((this.deprel == null) ? (other.deprel != null) : !this.deprel.equals(other.deprel)) {
				return false;
			}
			if (this.src != other.src) {
				return false;
			}
			if (this.tgt != other.tgt) {
				return false;
			}
			return true;
		}
	}

	public static void main(String[] args) throws IOException{
		StanfordEvaluator evaluator = new StanfordEvaluator();
		evaluator.add(args[0],args[1]);
		
		NumberFormat percentFormatter = NumberFormat.getPercentInstance(Locale.US);
		percentFormatter.setMinimumFractionDigits(2);
		percentFormatter.setMaximumFractionDigits(2);
		double precision = evaluator.getPrecision();
		double recall = evaluator.getRecall();
		double f1 = evaluator.getF1();
		
		System.err.format("# gold edges: %d%n", evaluator.nEdges1);
		System.err.format("# candidate edges: %d%n", evaluator.nEdges2);
		System.err.format("Precision: %s%n", percentFormatter.format(precision));
		System.err.format("Recall: %s%n", percentFormatter.format(recall));
		System.err.format("F1: %s%n", percentFormatter.format(f1));
	}
}
