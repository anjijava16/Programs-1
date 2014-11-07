package org.whu.hjp.ctr.spm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Generate features for the dataset
 */
public class FeatureFactory {

	protected LinkedHashMap<String, Integer> wrodcount;
	protected ArrayList<String> namelist;

	public FeatureFactory() {
		namelist = isName("namelist");
	}

	/**
	 * Words is a list of the words in the entire corpus, previousLabel is the
	 * label for position-1 (or O if it's the start of a new sentence), and
	 * position is the word you are adding features for. PreviousLabel must be
	 * the only label that is visible to this method.
	 */
	private List<String> computeFeatures(List<String> words,
			String previousLabel, int position) {
		// define the
		String prepreviousLable = "O";
		List<String> features = new ArrayList<String>();

		// get the tokens in the window size
		int prevposition = position - 1;
		int prevprevposition = position - 2;
		if (position < 2) {
			prevprevposition = 0;
			prevposition = 0;
		}

		int nextposition = position + 1;
		int nextnextposition = position + 2;
		if (position > (words.size() - 3)) {
			nextposition = words.size() - 1;
			nextnextposition = words.size() - 1;
		}
		String prevprevWord = words.get(prevprevposition);
		String previousWord = words.get(prevposition);
		String currentWord = words.get(position);
		String nextWord = words.get(nextposition);
		String nextnextWord = words.get(nextnextposition);
		String firstelement = FirstElement(currentWord);

		Stemmer s = new Stemmer();
		for (int i = 0; i < currentWord.length(); i++) {
			s.add(currentWord.charAt(i));
		}
		s.stem();
		String stemmedWord = StemmerWord(currentWord);
		String previousstemmedWord = StemmerWord(previousWord);
		String nextstemmedWord = StemmerWord(nextWord);
		String prepreviousstemmedWord = StemmerWord(prevprevWord);
		// String nextnextstemmedWord = StemmerWord(nextnextWord);

		// x_{i} word type
		boolean allcapitalized = isAllCaptilized(currentWord);
		boolean alphanumeric = isAlphaNumeric(currentWord);
		boolean iscaptilized = isCaptilized(currentWord);
		boolean islowercased = isAllLowercasedd(currentWord);
		boolean captilizedMixedCase = isCaptilizedMixedCase(currentWord);
		boolean isname = false;
		boolean ispuncase = isCaptilizedPuncCase(currentWord);
		boolean isdigit = isendwithdigit(currentWord);
		boolean ismixedcase = isMixedCase(currentWord);
		for (int i = 0; i < namelist.size() - 1; i++) {
			if (currentWord.equals(namelist.get(i)))
				;
			isname = true;
		}

		String convertedWord = Shape(currentWord);
		String preconvertedWord = Shape(previousWord);
		String nextconvertedWord = Shape(nextWord);
		String prepreconvertedWord = Shape(prevprevWord);
		String nextnextconvertedWord = Shape(nextnextWord);

		// capitalization pattern in the window
		boolean prevpreviscaptilized = isCaptilized(prevprevWord);
		boolean previscaptilized = isCaptilized(previousWord);
		boolean nextiscaptilized = isCaptilized(nextWord);
		boolean nextnextiscaptilized = isCaptilized(nextnextWord);
		boolean preaphanumeric = isAlphaNumeric(previousWord);
		boolean nextalphanumeric = isAlphaNumeric(nextWord);
		boolean prelowercase = isAllLowercasedd(previousWord);
		boolean nextlowercase = isAllLowercasedd(nextWord);
		// add features into the feature set

		features.add("word=" + currentWord);
		features.add("stemmedWord=" + stemmedWord);
		features.add("previousstemmedWord=" + previousstemmedWord);
		features.add("nextstemmedWord=" + nextstemmedWord);
		features.add("pervWord=" + previousWord);
		features.add("prevprevWord=" + prevprevWord);
		features.add("nextnextWord=" + nextnextWord);
		features.add("preconvertedWord=" + preconvertedWord);
		features.add("prepreconvertedWord=" + prepreconvertedWord);
		features.add("nextconvertedWord=" + nextconvertedWord);
		features.add("nextnextconvertedWord=" + nextnextconvertedWord);
		features.add("nextWord=" + nextWord);
		features.add("convertedWord=" + convertedWord);
		features.add("iscaptilized=" + iscaptilized);
		// features.add("previscaptilized=" + previscaptilized +
		// ", iscaptilized=" + iscaptilized + ", nextiscaptilized=" +
		// nextiscaptilized);
		features.add("pervWord=" + previousWord + ", prevLabel="
				+ previousLabel);
		features.add("word=" + currentWord + ", prevLabel=" + previousLabel);

		prepreviousLable = previousLabel;

		return features;
	}

	public static String FirstElement(String currentWord) {
		String[] ary = currentWord.split("");
		String firstElement = ary[1];
		boolean iscaptilized = isCaptilized(currentWord);

		if (iscaptilized) {
			return firstElement;
		} else {
			return "";
		}
	}

	public static String Shape(String currentWord) {
		String[] ary = currentWord.split("");

		for (int i = 1; i < ary.length; i++) {
			// "[a-zA-Z0-9]*"
			String ithelement = ary[i];
			if (ithelement.matches("[a-z]")) {
				ary[i] = "x";
			}
			if (ithelement.matches("[A-Z]")) {
				ary[i] = "X";
			}
			if (ithelement.matches("[0-9]")) {
				ary[i] = "#";
			}
		}
		String convertedWord = "";

		for (String sa : ary) {
			convertedWord += sa;
		}
		return convertedWord;
	}

	public static String StemmerWord(String currentWord) {
		Stemmer s = new Stemmer();
		for (int i = 0; i < currentWord.length(); i++) {
			s.add(currentWord.charAt(i));
		}
		s.stem();
		String stemmedWord = s.toString();
		return stemmedWord;
	}

	// determine whether the currentWord is a name
	private ArrayList<String> isName(String filename) {
		namelist = new ArrayList<String>();

		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (Exception e) {
			System.err.println("Can't open '" + filename + "' for input:");
			e.printStackTrace();
			System.exit(1);
		}

		for (String line = readLine1(in, filename); line != null; line = readLine1(
				in, filename)) {
			String personname = line.trim();
			namelist.add(personname);
		}
		return namelist;
	}

	private static boolean isendwithdigit(String currentWord) {
		String[] ary = currentWord.split("");
		if (ary[currentWord.length()].matches("[0-9]")) {
			return true;
		}
		return false;
	}

	private static boolean isCaptilizedMixedCase(String currentWord) {
		if (currentWord.length() == 2 && isCaptilized(currentWord)) {
			char secondword = currentWord.charAt(1);
			if (secondword == '.') {
				return true;
			}
			return false;
		}

		return false;
	}

	private static boolean isMixedCase(String currentWord) {
		if (!isCaptilized(currentWord)) {
			for (int i = 1; i < currentWord.length(); i++) {
				if (!Character.isLowerCase(currentWord.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isCaptilizedPuncCase(String currentWord) {
		if (currentWord.length() > 2 && isCaptilized(currentWord)) {
			char secondword = currentWord.charAt(currentWord.length() - 1);
			if (secondword == '.') {
				return true;
			}
			return false;
		}

		return false;
	}

	// determine whether the current word is capitalized
	private static boolean isCaptilized(String currentWord) {

		if (Character.isUpperCase(currentWord.charAt(0))) {
			return true;
		} else {
			return false;
		}
	}

	// determine whether the current word is all capitalized
	private static boolean isAllCaptilized(String currentWord) {
		for (int i = 0; i < currentWord.length(); i++) {
			if (!Character.isUpperCase(currentWord.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// determine whether the current word is all lowercased
	private static boolean isAllLowercasedd(String currentWord) {
		for (int i = 0; i < currentWord.length(); i++) {
			if (!Character.isLowerCase(currentWord.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	// determine whether the current word is alphanumeric
	private static boolean isAlphaNumeric(String currentWord) {
		if (currentWord.length() < 2) {
			return false;
		}

		if (!currentWord.matches("[a-zA-Z0-9]*")) {
			return false;
		}
		return true;
	}

	/**
	 * generate features for training data
	 */
	public List<Datum> setFeaturesTrain(List<Datum> data) {
		// this is so that the feature factory code doesn't accidentally use the
		// true label info
		List<Datum> newData = new ArrayList<Datum>();
		List<String> words = new ArrayList<String>();

		for (Datum datum : data) {
			words.add(datum.word);
		}

		String previousLabel = "O";
		for (int i = 0; i < data.size(); i++) {
			Datum datum = data.get(i);
			Datum newDatum = new Datum(datum.word, datum.label);
			newDatum.features = computeFeatures(words, previousLabel, i);
			newDatum.previousLabel = previousLabel;
			newData.add(newDatum);
			previousLabel = datum.label;
		}
		return newData;
	}

	/**
	 * generate features for testing data
	 */
	public List<Datum> setFeaturesTest(List<Datum> data) {
		List<Datum> newData = new ArrayList<Datum>();
		List<String> words = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		Map<String, Integer> labelIndex = new HashMap<String, Integer>();

		for (Datum datum : data) {
			words.add(datum.word);
			if (labelIndex.containsKey(datum.label) == false) {
				labelIndex.put(datum.label, labels.size());
				labels.add(datum.label);
			}
		}

		// compute features for all possible previous labels in advance for
		// Viterbi algorithm
		for (int i = 0; i < data.size(); i++) {
			Datum datum = data.get(i);

			if (i == 0) {
				String previousLabel = "O";
				datum.features = computeFeatures(words, previousLabel, i);
				Datum newDatum = new Datum(datum.word, datum.label);
				newDatum.features = computeFeatures(words, previousLabel, i);
				newDatum.previousLabel = previousLabel;
				newData.add(newDatum);
			} else {
				for (String previousLabel : labels) {
					datum.features = computeFeatures(words, previousLabel, i);
					Datum newDatum = new Datum(datum.word, datum.label);
					newDatum.features = computeFeatures(words, previousLabel, i);
					newDatum.previousLabel = previousLabel;
					newData.add(newDatum);
				}
			}
		}
		return newData;
	}

	/**
	 * read data from the file and store them into Datum
	 */
	public List<Datum> readData(String filename) throws IOException {
		List<Datum> data = new ArrayList<Datum>();
		BufferedReader in = new BufferedReader(new FileReader(filename));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (line.trim().length() == 0)
				continue;
			String[] bits = line.split("\\s+");
			String word = bits[0];
			String label = bits[1];
			Datum datum = new Datum(word, label);
			data.add(datum);
		}
		return data;
	}

	// read every line from namelist
	private static String readLine1(BufferedReader in, String filename) {
		String line = null;
		try {
			line = in.readLine();
		} catch (Exception e) {
			System.err.println("Can't open '" + filename + "' for input:'");
			e.printStackTrace();
			System.exit(1);
		}
		return line;
	}

	/** write feature into a file */
	public void writeData(List<Datum> data, String filename) throws IOException {
		FileWriter file = new FileWriter(filename + ".json", false);
		for (int i = 0; i < data.size(); i++) {
			try {
				JSONObject obj = new JSONObject();
				Datum datum = data.get(i);
				obj.put("_label", datum.label);
				obj.put("_word", base64encode(datum.word));
				obj.put("_prevLabel", datum.previousLabel);

				JSONObject featureObj = new JSONObject();
				List<String> features = datum.features;
				for (int j = 0; j < features.size(); j++) {
					String feature = features.get(j).toString();
					featureObj.put("_" + feature, feature);
				}
				obj.put("_features", featureObj);
				obj.write(file);
				file.append("\n");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		file.close();
	}

	private String base64encode(String str) {
		Base64 base = new Base64();
		byte[] strBytes = str.getBytes();
		byte[] encBytes = base.encode(strBytes);
		String encoded = new String(encBytes);
		return encoded;
	}
}
