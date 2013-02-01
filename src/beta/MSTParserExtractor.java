/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import se.uu.nlp.dlib.conll.CoNLLReader;
import se.uu.nlp.dlib.conll.CoNLLTree;

/**
 * Extract MSTParser feature strings.
 *
 * <p>This class makes it possible to verify that the features extracted by beta
 * are exactly the same as the features extracted by MSTParser.
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class MSTParserExtractor {

	private final Model model;
	private final Emitter emitter;

	public MSTParserExtractor(Model model, File file) throws IOException {
		this.model = model;
		this.emitter = new Emitter(model, file);
	}

	public void next(CoNLLTree graph) {
		CoNLLTree input = new CoNLLTree(graph);
		for (int i = 0; i < graph.getNNodes(); i++) {
			input.forms[i] = CoNLLReader.normalize(graph.forms[i]);
			input.lemmas[i] = CoNLLReader.normalize(graph.lemmas[i]);
		}

		EdgeFeaturizer featurizer = new EdgeFeaturizer(model, input);
		for (int i = 1; i < input.getNNodes(); i++) {
			int label = model.getCodeForLabel(input.deprels[i]);
			featurizer.featurize(input.heads[i], i, label, emitter);
		}
	}

	public void exit() throws IOException {
		emitter.close();
	}

	public static class Emitter implements FeatureHandler {

		private final Model model;
		private final BufferedWriter writer;

		public Emitter(Model model, File file) throws IOException {
			this.model = model;
			this.writer = new BufferedWriter(new FileWriter(file));
		}

		public void write(String s) throws IOException {
			writer.write(s);
		}

		public void close() throws IOException {
			writer.close();
		}

		@Override
		public void handle(int[] feature) {
			StringBuilder sb = new StringBuilder();
			sb.append("DEB ");
			switch (feature[0]) {
				case 100:
					sb.append("POSPC=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getAttDist(feature, 4));
					break;
				case 101:
					if (feature.length == 5) {
						sb.append("POSPT=");
						sb.append(getTag(feature[1]));
						sb.append(" ");
						sb.append(getTag(feature[2]));
						sb.append(" ");
						sb.append(getTag(feature[3]));
						sb.append("*");
						sb.append(getAttDist(feature, 4));
					} else {
						sb.append("POSPT1=");
						sb.append(getTag(feature[1]));
						sb.append(" ");
						sb.append(getTag(feature[2]));
						sb.append(" ");
						sb.append(getTag(feature[3]));
					}
					break;
				case 102:
					sb.append("POSPT1=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(" ");
					sb.append(getTag(feature[4]));
					sb.append(getStarredAttDist(feature, 5));
					break;
				case 103:
					sb.append("POSPT2=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 104:
					sb.append("POSPT3=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 105:
					sb.append("POSPT4=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 106:
					sb.append("APOSPT1=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 107:
					sb.append("APOSPT1=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(" ");
					sb.append(getTag(feature[4]));
					sb.append(getStarredAttDist(feature, 5));
					break;
				case 108:
					sb.append("APOSPT2=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 109:
					sb.append("APOSPT3=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 110:
					sb.append("APOSPT4=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 111:
					sb.append("BAPOSPT1=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(" ");
					sb.append(getTag(feature[4]));
					sb.append(getStarredAttDist(feature, 5));
					break;
				case 112:
					sb.append("BAPOSPT2=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(" ");
					sb.append(getTag(feature[4]));
					sb.append(getStarredAttDist(feature, 5));
					break;
				case 113:
					sb.append("HC2FF1=");
					sb.append(getWord(feature[1]));
					sb.append(getStarredAttDist(feature, 2));
					break;
				case 114:
					sb.append("HC2FF1=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 115:
					sb.append("HC2FF1=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 116:
					sb.append("HC2FF1=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(" ");
					sb.append(getWord(feature[4]));
					sb.append(getStarredAttDist(feature, 5));
					break;
				case 117:
					sb.append("HC2FF2=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getWord(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 118:
					sb.append("HC2FF3=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 119:
					sb.append("HC2FF4=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getWord(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 120:
					sb.append("HC2FF4=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getWord(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append(getStarredAttDist(feature, 4));
					break;
				case 121:
					sb.append("HC2FF5=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 122:
					sb.append("HC2FF6=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(getStarredAttDist(feature, 3));
					break;
				case 123:
					sb.append("HC2FF7=");
					sb.append(getTag(feature[1]));
					sb.append(getStarredAttDist(feature, 2));
					break;
				case 124:
					sb.append("HC2FF8=");
					sb.append(getWord(feature[1]));
					sb.append(getStarredAttDist(feature, 2));
					break;
				case 125:
					sb.append("HC2FF9=");
					sb.append(getTag(feature[1]));
					sb.append(getStarredAttDist(feature, 2));
					break;
				case 200:
					if (feature.length == 3) {
						sb.append("NTS1=");
						sb.append(getLabel(feature[1]));
						sb.append(getSuffix(feature, 2));
					} else {
						sb.append("ANTS1=");
						sb.append(getLabel(feature[1]));
					}
					break;
				case 201:
					sb.append("NTH=");
					sb.append(getWord(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append("&");
					sb.append(getLabel(feature[3]));
					sb.append(getSuffix(feature, 4));
					break;
				case 202:
					sb.append("NTI=");
					sb.append(getTag(feature[1]));
					sb.append("&");
					sb.append(getLabel(feature[2]));
					sb.append(getSuffix(feature, 3));
					break;
				case 203:
					sb.append("NTIA=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append("&");
					sb.append(getLabel(feature[3]));
					sb.append(getSuffix(feature, 4));
					break;
				case 204:
					sb.append("NTIB=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append("&");
					sb.append(getLabel(feature[3]));
					sb.append(getSuffix(feature, 4));
					break;
				case 205:
					sb.append("NTIC=");
					sb.append(getTag(feature[1]));
					sb.append(" ");
					sb.append(getTag(feature[2]));
					sb.append(" ");
					sb.append(getTag(feature[3]));
					sb.append("&");
					sb.append(getLabel(feature[4]));
					sb.append(getSuffix(feature, 5));
					break;
				case 206:
					sb.append("NTJ=");
					sb.append(getWord(feature[1]));
					sb.append("&");
					sb.append(getLabel(feature[2]));
					sb.append(getSuffix(feature, 3));
					break;
				default:
					sb.append("UNKNOWN");
					break;
			}
			try {
				writer.write(sb.toString());
				writer.newLine();
			} catch (IOException e) {
				System.err.println("I/O exception.");
				System.exit(1);
			}
		}

		private String getWord(int code) {
			return model.getWordForCode(code);
		}

		private String getTag(int code) {
			if (code == -1) {
				return "STR";
			}
			if (code == -2) {
				return "END";
			}
			if (code == -3) {
				return "MID";
			}
			return model.getTagForCode(code);
		}

		private String getAttDist(int[] feature, int k) {
			StringBuilder sb = new StringBuilder();
			if (k < feature.length) {
				sb.append("&");
				int code = feature[k];
				if (code < 10) {
					sb.append("RA");
				} else {
					sb.append("LA");
					code -= 10;
				}
				sb.append("&");
				switch (code) {
					case 6:
						sb.append("10");
						break;
					case 5:
						sb.append("5");
						break;
					default:
						sb.append(Integer.toString(code));
						break;
				}
			}
			return sb.toString();
		}

		private String getStarredAttDist(int[] feature, int k) {
			if (k < feature.length) {
				return "*" + getAttDist(feature, k);
			} else {
				return "";
			}
		}

		private String getLabel(int code) {
			return model.getLabelForCode(code);
		}

		private String getSuffix(int[] feature, int k) {
			StringBuilder sb = new StringBuilder();
			if (k < feature.length) {
				sb.append("&");
				int code = feature[k];
				if (code < 10) {
					sb.append("RA");
				} else {
					sb.append("LA");
					code -= 10;
				}
				sb.append("&");
				switch (code) {
					case 0:
						sb.append("true");
						break;
					case 1:
						sb.append("false");
						break;
				}
			}
			return sb.toString();
		}
	}
}