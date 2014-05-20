/*
 * See the file "LICENSE" for the full license governing this code.
 */
package se.liu.ida.nlp.beta;

import gnu.trove.impl.Constants;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Marco Kuhlmann <marco.kuhlmann@lingfil.uu.se>
 */
public class Model implements Serializable {

	public static final String BEG_TOKEN = "<BEG>";
	public static final String END_TOKEN = "<END>";
	public static final String MID_TOKEN = "<MID>";
	private static final int NO_ENTRY = -1;
	public static final String UNKNOWN_LABEL = "ROOT";
	private final Table<String> forms;
	private final Table<String> lemmas;
	private final Table<String> cpostags;
	private final Table<String> postags;
	private final Table<String> deprels;
	private final TLongIntMap features;
	private double[] weightVector;

	public Model() {
		this.forms = new Table<>();
		this.lemmas = new Table<>();
		this.cpostags = new Table<>();
		cpostags.addEntry(BEG_TOKEN);
		cpostags.addEntry(END_TOKEN);
		cpostags.addEntry(MID_TOKEN);
		this.postags = new Table<>();
		postags.addEntry(BEG_TOKEN);
		postags.addEntry(END_TOKEN);
		postags.addEntry(MID_TOKEN);
		this.deprels = new Table<>();
		this.features = new TLongIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0, NO_ENTRY);
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

	private Model(Table<String> forms, Table<String> lemmas, Table<String> cpostags, Table<String> postags, Table<String> deprels, TLongIntMap features, double[] weightVector) {
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

	public int addFeature(long feature) {
		int code = features.get(feature);
		if (code == NO_ENTRY) {
			code = features.size();
			features.put(feature, code);
		}
		return code;
	}

	public int getNFeatures() {
		return features.size();
	}

	public int getCodeForFeature(long feature) {
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
		try (OutputStream os = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)))) {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(this);
		}
	}

	public static Model load(String fileName) throws IOException {
		Model model;
		try (InputStream is = new BufferedInputStream(new GZIPInputStream(new FileInputStream(fileName)))) {
			ObjectInputStream ois = new ObjectInputStream(is);
			model = null;
			try {
				model = (Model) ois.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}
		return model;
	}
}
