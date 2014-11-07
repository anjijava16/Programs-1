package org.whu.hjp.ctr.spm;

import java.util.*;
import java.io.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.rits.cloning.*;
import com.rits.perspectives.*;

/**
 * Implementation of Structured Perceptron algorithm, you can find the detailed
 * information about the algorithm here
 * (http://lxmls.it.pt/2011/slides/day3_slides.tar.gz)
 */

public class StructuredPerceptron {

	/**
	 * There are several steps for this algorithm. 1. read data and feature the
	 * 6301 iteration tp: 1832 guessEntities: 1832 trueEntitites: 1832 precision
	 * = 1.0 recall = 1.0 F1 = 1.0 tp: 1832 guessEntities: 1832 trueEntitites:
	 * 1832 precision = 1.0 recall = 1.0 F1 = 1.0
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
	 * train the structured preceptron There are no bias term in the structured
	 * perceprton
	 */
	public static double[][] learnPerceptron(List<Datum> trainData,
			List<Datum> testDataWithMultiplePrevLabels) {

		FeatureRepresentation obj = new FeatureRepresentation(trainData);
		double[][] weight = new double[obj.labelIndex.size()][obj.featureIndex
				.size()];
		// for (int i= 0; i< weight.length; i++) {
		// weight[i] = 0.0;
		// }

		boolean run = true;
		int iteration = 100;
		List<Datum> testData = new ArrayList<Datum>();
		testData.add(testDataWithMultiplePrevLabels.get(0));
		for (int i = 1; i < testDataWithMultiplePrevLabels.size(); i += obj.labelIndex
				.size()) {
			testData.add(testDataWithMultiplePrevLabels.get(i));
		}
		int q = 0;
		// for (int i = 0; i < iteration; i++)
		while (run) {
			// for each training example(x, y)
			System.out.println("the " + (q + 1) + " iteration");
			Viterbi viterbi = new Viterbi(obj.labelIndex, obj.featureIndex,
					weight);
			Cloner cloner = new Cloner();
			List<Datum> clonerTestData = cloner.deepClone(testData);
			viterbi.decode(clonerTestData, testDataWithMultiplePrevLabels);
			Scorer.score(clonerTestData);
			List<Datum> predictData = new ArrayList<Datum>();
			for (int j = 0; j < clonerTestData.size(); j++) {
				// System.out.println(j);
				String word = clonerTestData.get(j).word;
				String guessLabel = clonerTestData.get(j).guessLabel;
				// System.out.println("true label: " + testData.get(j).label +
				// "   guess label:" + guessLabel);
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
			// for (int j = 0; j < goldFeature.length; j++) {
			// System.out.println(goldFeature[j] + "===" + predictFeature[j]);
			// }
			if (!matrixEqual(goldFeature, predictFeature)) {
				weight = updateWeight(weight, goldFeature, predictFeature);
			} else {
				run = false;
			}
			q = q + 1;
		}
		// for (int j = 0; j < weight.length; j++) {
		// System.out.println(weight[j]);
		// }
		Viterbi viterbi = new Viterbi(obj.labelIndex, obj.featureIndex, weight);
		viterbi.decode(testData, testDataWithMultiplePrevLabels);
		// for (Datum data : testData) {
		// System.out.println(data.label + "====" + data.guessLabel);
		// }
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

	/** update features */
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

	/** Compute the feature for the data */
	public static double[][] getStructureFeature(List<Datum> datas,
			Index featureIndex, Index labelIndex) {
		double[][] structureFeature = new double[labelIndex.size()][featureIndex
				.size()];

		for (int i = 0; i < datas.size(); i++) {
			Datum data = datas.get(i);
			List<String> features = data.features;
			String label = data.label;
			int position = labelIndex.indexOf(label);
			// String previousLabel = data.previousLabel;
			for (Object feature : features) {
				int f = featureIndex.indexOf(feature);
				if (f >= 0) {
					structureFeature[position][f] += 1;
				}
			}

			// int j = featureIndex.indexOf(previousLabel + " " + label);
			// if ( j >= 0 ) {
			// structureFeature[j] += 1;
			// }
		}

		return structureFeature;
	}

	// Read words, labels, and features
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
