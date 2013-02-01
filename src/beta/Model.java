/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import java.io.*;
import se.uu.nlp.util.Table;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Model {

	public static final String UNKNOWN_LABEL = "ROOT";
	private final Table<String> words;
	private final Table<String> tags;
	private final Table<String> labels;
	private final FeatureTrie features;
	private double[] weightVector;

	public Model() {
		this.words = new Table<String>();
		this.tags = new Table<String>();
		this.labels = new Table<String>();
		this.features = new FeatureTrie(33);
		this.weightVector = null;
	}

	public Model(Model model) {
		this.words = model.words;
		this.tags = model.tags;
		this.labels = model.labels;
		this.features = model.features;
		this.weightVector = new double[model.getNFeatures()];
	}

	private Model(Table<String> words, Table<String> tags, Table<String> labels, FeatureTrie features, double[] weightVector) {
		this.words = words;
		this.tags = tags;
		this.labels = labels;
		this.features = features;
		this.weightVector = weightVector;
	}

	public int addWord(String word) {
		return words.addEntry(word);
	}

	public int getNWords() {
		return words.getSize();
	}

	public int getCodeForWord(String word) {
		return words.getIndex(word);
	}

	public String getWordForCode(int code) {
		return words.getEntry(code);
	}

	public int addTag(String tag) {
		return tags.addEntry(tag);
	}

	public int getNTags() {
		return tags.getSize();
	}

	public int getCodeForTag(String tag) {
		return tags.getIndex(tag);
	}

	public String getTagForCode(int code) {
		return tags.getEntry(code);
	}

	public int addLabel(String label) {
		return labels.addEntry(label);
	}

	public int getNLabels() {
		return labels.getSize();
	}

	public int getCodeForLabel(String label) {
		return labels.getIndex(label);
	}

	public int getCodeForDefaultLabel() {
		return labels.getIndex(UNKNOWN_LABEL);
	}

	public String getLabelForCode(int code) {
		return labels.getEntry(code);
	}

	public String getDefaultLabel() {
		return UNKNOWN_LABEL;
	}

	public int addFeature(int[] feature) {
		return features.add(feature);
	}

	public int getNFeatures() {
		return features.getNEntries();
	}

	public int getCodeForFeature(int[] feature) {
		return features.get(feature);
	}

	public double[] getWeightVector() {
		return weightVector;
	}

	public void setWeightVector(double[] weightVector) {
		this.weightVector = weightVector;
	}

	public void clearWeightVector() {
		this.weightVector = new double[getNFeatures()];
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
		saveTable(words, bw);
		saveTable(tags, bw);
		saveTable(labels, bw);
		features.save(bw);
		saveWeightVector(bw);
	}

	private static void saveTable(Table<String> table, BufferedWriter bw) throws IOException {
		bw.write(Integer.toString(table.getSize()));
		bw.newLine();
		for (String entry : table.getEntries()) {
			bw.write(entry);
			bw.newLine();
		}
	}

	private void saveWeightVector(BufferedWriter bw) throws IOException {
		bw.write(Integer.toString(weightVector.length));
		bw.newLine();
		for (int i = 0; i < weightVector.length; i++) {
			bw.write(Integer.toString(i));
			bw.write(":");
			bw.write(Double.toString(weightVector[i]));
			bw.newLine();
		}
	}

	public static Model load(String fileName) throws IOException {
		return load(new File(fileName));
	}

	public static Model load(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		Model model = load(br);
		br.close();
		return model;
	}

	public static Model load(BufferedReader br) throws IOException {
		Table<String> words = loadTable(br);
		Table<String> tags = loadTable(br);
		Table<String> labels = loadTable(br);
		FeatureTrie features = FeatureTrie.load(br);
		double[] weightVector = loadWeightVector(br);
		return new Model(words, tags, labels, features, weightVector);
	}

	private static Table<String> loadTable(BufferedReader br) throws IOException {
		Table<String> table = new Table<String>();
		int nEntries = Integer.parseInt(br.readLine());
		for (int i = 0; i < nEntries; i++) {
			table.addEntry(br.readLine());
		}
		return table;
	}

	public static double[] loadWeightVector(BufferedReader br) throws IOException {
		int nEntries = Integer.parseInt(br.readLine());
		double[] weightVector = new double[nEntries];
		for (int i = 0; i < nEntries; i++) {
			String[] tokens = br.readLine().split(":");
			int index = Integer.parseInt(tokens[0]);
			double value = Double.parseDouble(tokens[1]);
			weightVector[index] = value;
		}
		return weightVector;
	}
}
