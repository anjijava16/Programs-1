package org.whu.hjp.ctr.spm;

import java.util.*;
import java.io.*;

/**
 * we build a structured Perceptron method for identifying person and location
 * names in newswire text. Hence, this task is called Named Entity Recognition.
 * Named-entity recognition (NER) (also known as entity identification and
 * entity extraction) is a subtask of information extraction that seeks to
 * locate and classify atomic elements in text into predefined categories such
 * as the names of persons, organizations, locations, expressions of times ,
 * quantities, monetary values, percentages, etc.
 * (http://en.wikipedia.org/wiki/Named-entity_recognition)
 */
public class NER {

	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.out
					.println("USAGE: java -cp classes NER ../data/train ../data/dev 1|2 ");
			System.out
					.println("1: structured perceptron;     2: structured perceptron with average ");
			return;
		}

		FeatureFactory ff = new FeatureFactory();
		// read the train and test data
		List<Datum> trainData = ff.readData(args[0]);
		List<Datum> testData = ff.readData(args[1]);
		String choice = args[2];

		// add the features
		List<Datum> trainDataWithFeatures = ff.setFeaturesTrain(trainData);
		List<Datum> testDataWithFeatures = ff.setFeaturesTest(testData);

		// write the data with the features into JSON files
		// ff.writeData(trainDataWithFeatures, "trainWithFeatures");
		// ff.writeData(testDataWithFeatures, "testWithFeatuers");

		double[][] weight;

		if (choice.equals("1")) {
			weight = StructuredPerceptron.learnPerceptron(
					trainDataWithFeatures, testDataWithFeatures);
		} else {
			weight = AverageStructuredPerceptron.learnPerceptron(
					trainDataWithFeatures, testDataWithFeatures);
		}
		if (weight == null)
			return;

		// run Structured Perceptron
		System.out.println("Done......");
	}

}
