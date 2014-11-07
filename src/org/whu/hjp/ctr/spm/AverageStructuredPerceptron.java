package org.whu.hjp.ctr.spm;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rits.cloning.Cloner;

/**
 * Implementation of Average Structured Perceptron Algorithm, you can find the
 * detailed information about strctured perceptron with averaging through this
 * link {@link http
 * ://www.cs.columbia.edu/~mcollins/courses/6998-2012/lectures/lec5.1.pdf}
 * <p>
 *
 * <b>NOTE: </b> we use the average weight to make prediction w = w_{a} / NT ,
 * in our case, set n = 1, because there is just one sequence
 */
public class AverageStructuredPerceptron {

	/**
	 * The difference between the structured perceptron and the structured
	 * perceptron with averaging: (1) structured perceptron: the stopping
	 * condition is whether the predicted structure is equal to the gold
	 * structure (2) structured perceptron: the stopping condition is the
	 * iteration number.
	 */
	public static void main(String[] args) throws IOException {
		String trainFile = "trainWithFeatures.json";
		String testFile = "testWithFeatuers.json";
		List<Datum> trainData = readData(trainFile);
		List<Datum> testDataWithMultiplePrevLabels = readData(testFile);
		double[][] weight;
		weight = learnPerceptron(trainData, testDataWithMultiplePrevLabels);
		if (weight == null)
			return;
		System.out.println("Done.....");
	}

	/**
	 * train structured perceptron with the averaging
	 */
	public static double[][] learnPerceptron(List<Datum> trainData,
			List<Datum> testDataWithMultiplePrevLabels) {

		FeatureRepresentation obj = new FeatureRepresentation(trainData);
		double[][] weight = new double[obj.labelIndex.size()][obj.featureIndex
				.size()];
		double[][] averageWeight = new double[obj.labelIndex.size()][obj.featureIndex
				.size()];

		// update the iteration number in order to get a better result
		int iteration = 300;
		List<Datum> testData = new ArrayList<Datum>();
		testData.add(testDataWithMultiplePrevLabels.get(0));
		for (int i = 1; i < testDataWithMultiplePrevLabels.size(); i += obj.labelIndex
				.size()) {
			testData.add(testDataWithMultiplePrevLabels.get(i));
		}
		// execute the training for the number of iterations
		for (int i = 0; i < iteration; i++) {
			System.out.println("the " + (i + 1) + " iteration");
			Viterbi viterbi = new Viterbi(obj.labelIndex, obj.featureIndex,
					weight);
			Cloner cloner = new Cloner();
			List<Datum> clonerTestData = cloner.deepClone(testData);
			viterbi.decode(clonerTestData, testDataWithMultiplePrevLabels);
			// Scorer.score(clonerTestData);
			List<Datum> predictData = new ArrayList<Datum>();
			for (int j = 0; j < clonerTestData.size(); j++) {
				String word = clonerTestData.get(j).word;
				String guessLabel = clonerTestData.get(j).guessLabel;
				Datum newdata = new Datum(word, guessLabel);
				newdata.features = clonerTestData.get(j).features;
				if (j > 0) {
					newdata.previousLabel = clonerTestData.get(j - 1).guessLabel;
				} else {
					newdata.previousLabel = "O";
				}
				predictData.add(newdata);
			}
			double[][] goldFeature = getStructureFeature(testData,
					obj.featureIndex, obj.labelIndex);
			double[][] predictFeature = getStructureFeature(predictData,
					obj.featureIndex, obj.labelIndex);

			if (!matrixEqual(goldFeature, predictFeature)) {
				weight = updateWeight(weight, goldFeature, predictFeature);
				averageWeight = addWeight(weight, averageWeight);
			}
		}
		// decoding
		averageWeight = divide(averageWeight, iteration);
		Viterbi viterbi = new Viterbi(obj.labelIndex, obj.featureIndex,
				averageWeight);
		viterbi.decode(testData, testDataWithMultiplePrevLabels);
		Scorer.score(testData);

		return weight;
	}

	public static boolean matrixEqual(double[][] goldFeature,
			double[][] predictFeature) {
		boolean equal = true;
		boolean jump = false;
		for (int i = 0; i < goldFeature.length; i++) {
			for (int j = 0; j < goldFeature[i].length; j++) {
				if (goldFeature[i][j] != predictFeature[i][j]) {
					equal = false;
					jump = true;
					break;
				}
			}
			if (jump == true)
				break;
		}
		return equal;
	}

	public static double[][] updateWeight(double[][] weight,
			double[][] goldFeature, double[][] predictFeature) {
		for (int i = 0; i < weight.length; i++) {
			for (int j = 0; j < weight[i].length; j++) {
				weight[i][j] = weight[i][j] + goldFeature[i][j]
						- predictFeature[i][j];
			}
		}
		return weight;
	}

	// increment the averageWeight by weight
	//
	public static double[][] addWeight(double[][] weight,
			double[][] averageWeight) {
		for (int i = 0; i < averageWeight.length; i++) {
			for (int j = 0; j < averageWeight[i].length; j++) {
				averageWeight[i][j] = averageWeight[i][j] + weight[i][j];
			}
		}
		return averageWeight;
	}

	// divide the weight by the number of iteration
	public static double[][] divide(double[][] weight, double iteration) {
		for (int i = 0; i < weight.length; i++) {
			for (int j = 0; j < weight[i].length; j++) {
				weight[i][j] = weight[i][j] / iteration;
			}
		}
		return weight;
	}

	public static double[][] getStructureFeature(List<Datum> datas,
			Index featureIndex, Index labelIndex) {
		double[][] structureFeature = new double[labelIndex.size()][featureIndex
				.size()];

		for (int i = 0; i < datas.size(); i++) {
			Datum data = datas.get(i);
			List<String> features = data.features;
			String label = data.label;
			int position = labelIndex.indexOf(label);
			for (Object feature : features) {
				int f = featureIndex.indexOf(feature);
				if (f >= 0) {
					structureFeature[position][f] += 1;
				}
			}
		}

		return structureFeature;
	}

	private static List<Datum> readData(String filename) throws IOException {
		List<Datum> data = new ArrayList<Datum>();
		FileInputStream fstream = new FileInputStream(filename);
		JSONTokener tokener = null;

		try {
			tokener = new JSONTokener(fstream);
			while (tokener.more()) {
				JSONObject object = (JSONObject) tokener.nextValue();
				if (object == null)
					break;

				String word = object.getString("_word");
				String label = object.getString("_label");
				String previousLabel = object.getString("_prevLabel");
				JSONObject featureObject = (JSONObject) object.get("_features");
				List<String> features = new ArrayList<String>();
				for (String name : JSONObject.getNames(featureObject)) {
					features.add(featureObject.getString(name));
				}
				Datum datum = new Datum(word, label);
				datum.features = features;
				datum.previousLabel = previousLabel;
				data.add(datum);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}
}
