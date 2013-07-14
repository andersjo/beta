/*
 * See the file "LICENSE" for the full license governing this code.
 */
package beta;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Model implements Serializable {

	public static final String UNKNOWN_LABEL = "ROOT";
	private final Table<String> forms;
	private final Table<String> lemmas;
	private final Table<String> cpostags;
	private final Table<String> postags;
	private final Table<String> deprels;
	private final FeatureTrie features;
	private double[] weightVector;

	public Model() {
		this.forms = new Table<String>();
		this.lemmas = new Table<String>();
		this.cpostags = new Table<String>();
		this.postags = new Table<String>();
		this.deprels = new Table<String>();
		this.features = new FeatureTrie(85);
		this.weightVector = null;
	}

	public Model(Model model) {
		this.forms = model.forms;
		this.lemmas = model.lemmas;
		this.cpostags = model.cpostags;
		this.postags = model.postags;
		this.deprels = model.deprels;
		this.features = model.features;
		this.weightVector = new double[model.getNFeatures()];
	}

	private Model(Table<String> forms, Table<String> lemmas, Table<String> cpostags, Table<String> postags, Table<String> deprels, FeatureTrie features, double[] weightVector) {
		this.forms = forms;
		this.lemmas = lemmas;
		this.cpostags = cpostags;
		this.postags = postags;
		this.deprels = deprels;
		this.features = features;
		this.weightVector = weightVector;
	}

	public int addForm(String word) {
		return forms.addEntry(word);
	}

	public int getNForms() {
		return forms.getSize();
	}

	public int getCodeForForm(String word) {
		return forms.getIndex(word);
	}

	public String getFormForCode(int code) {
		return forms.getEntry(code);
	}

	public int addLemma(String lemma) {
		return lemmas.addEntry(lemma);
	}

	public int getNLemmas() {
		return lemmas.getSize();
	}

	public int getCodeForLemma(String lemma) {
		return lemmas.getIndex(lemma);
	}

	public String getLemmaForCode(int code) {
		return lemmas.getEntry(code);
	}

	public int addCPOSTag(String tag) {
		return cpostags.addEntry(tag);
	}

	public int getNCPOSTags() {
		return cpostags.getSize();
	}

	public int getCodeForCPOSTag(String cpostag) {
		return cpostags.getIndex(cpostag);
	}

	public String getCPOSTagForCode(int code) {
		return cpostags.getEntry(code);
	}

	public int addPOSTag(String tag) {
		return postags.addEntry(tag);
	}

	public int getNPOSTags() {
		return postags.getSize();
	}

	public int getCodeForPOSTag(String postag) {
		return postags.getIndex(postag);
	}

	public String getPOSTagForCode(int code) {
		return postags.getEntry(code);
	}

	public int addDeprel(String deprel) {
		return deprels.addEntry(deprel);
	}

	public int getNDeprels() {
		return deprels.getSize();
	}

	public int getCodeForDeprel(String label) {
		return deprels.getIndex(label);
	}

	public int getCodeForDefaultDeprel() {
		return deprels.getIndex(UNKNOWN_LABEL);
	}

	public String getDeprelForCode(int code) {
		return deprels.getEntry(code);
	}

	public String getDefaultDeprel() {
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
		OutputStream os = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(this);
		os.close();
	}

	public static Model load(String fileName) throws IOException {
		InputStream is = new BufferedInputStream(new GZIPInputStream(new FileInputStream(fileName)));
		ObjectInputStream ois = new ObjectInputStream(is);
		Model model = null;
		try {
			model = (Model) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		is.close();
		return model;
	}
}
