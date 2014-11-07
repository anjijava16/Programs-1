package org.whu.hjp.ctr.spm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/*Update the weight*/
public class Perceptron {

	/* read plain text and store them in arrayList */
	public ArrayList<ArrayList<String>> readPlainTextArray(String fileName) {
		ArrayList<ArrayList<String>> relationship = new ArrayList<ArrayList<String>>();
		try {
			BufferedReader relationshipBufferedReader = new BufferedReader(
					new FileReader(fileName));
			for (String line = relationshipBufferedReader.readLine(); line != null; line = relationshipBufferedReader
					.readLine()) {
				String[] items = line.split(",");
				ArrayList<String> arguments = new ArrayList<String>();
				for (int i = 0; i < items.length; i++) {
					arguments.add(items[i]);
				}
				relationship.add(arguments);
			}
			relationshipBufferedReader.close();
			return relationship;

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	// Multiply two vector
	public Double multiply(ArrayList<Double> weight, List<Double> features) {
		double value = 0.0;
		for (int i = 0; i < weight.size(); i++) {
			value = value + weight.get(i) * features.get(i);
		}

		return value;
	}

	/** calculate the distance of delta */
	public double calculatedistance(ArrayList<Double> delta) {
		double distance = 0.0;
		for (int i = 0; i < delta.size(); i++) {
			distance = distance + delta.get(i) * delta.get(i);
		}
		distance = Math.sqrt(distance);

		return distance;
	}

	/** Implementation of batch Perceptron */
	public ArrayList<Double> learn_perceptron(
			ArrayList<ArrayList<Double>> dataset,
			ArrayList<Double> initial_weight, double alpha) {
		int column = dataset.get(0).size() - 1;
		int row = dataset.size();
		double eplison = 0.000001;
		ArrayList<Double> delta;

		int k = 0;
		do {
			k = k + 1;
			System.out.println("The " + Integer.toString(k) + " Iteration");
			delta = new ArrayList<Double>();
			for (int i = 0; i < column; i++) {
				delta.add(0.0);
			}

			for (int j = 0; j < row; j++) {
				ArrayList<Double> training_example = dataset.get(j);
				Double label = training_example.get(0); // get label
				List<Double> features = training_example.subList(1,
						training_example.size());
				Double value = multiply(initial_weight, features);
				if (value * label <= 0) {
					for (int i = 0; i < delta.size(); i++) {
						Double updelta = delta.get(i) - label * features.get(i);
						delta.set(i, updelta);
					}
				}
			}

			for (int i = 0; i < delta.size(); i++) {

				delta.set(i, delta.get(i) / row);
			}

			for (int i = 0; i < initial_weight.size(); i++) {

				initial_weight.set(i,
						initial_weight.get(i) - alpha * delta.get(i));
			}

			System.out.println("The updated weight:" + initial_weight);

		} while (calculatedistance(delta) > eplison);

		return initial_weight;
	}

	/**
	 * Execute the perceptron algorithm
	 *
	 */
	public static void main(String[] args) {
		/* Import the two Gaussian File */
		Perceptron perceptron = new Perceptron();
		ArrayList<ArrayList<String>> dataset = perceptron
				.readPlainTextArray("twogaussian.csv");
		ArrayList<ArrayList<Double>> twogaussian = new ArrayList<ArrayList<Double>>();

		/*
		 * Covert ArrayList<ArrayList<String>> to ArrayList<ArrayList<Double>>
		 * in order to run the perceptron algorithm
		 */
		for (int i = 0; i < dataset.size(); i++) {

			ArrayList<String> dataset_row = dataset.get(i);
			ArrayList<Double> twogaussian_row = new ArrayList<Double>();
			/* class label */
			twogaussian_row.add(Double.parseDouble(dataset_row.get(0)));
			/* w_{0}, its feature all 1 */
			twogaussian_row.add(1.0);

			/* other features */
			for (int j = 1; j < dataset_row.size(); j++) {
				String element = dataset_row.get(j);
				twogaussian_row.add(Double.parseDouble(element));
			}
			twogaussian.add(twogaussian_row);
		}

		/*
		 * how many features, the reason for deducting 1 is that there is a
		 * label
		 */
		int column = twogaussian.get(0).size() - 1;
		int row = twogaussian.size();
		/* initialize the weight vector as zeros(1, colum) */
		ArrayList<Double> initial_weight = new ArrayList<Double>();
		for (int i = 0; i < column; i++) {
			initial_weight.add(0.0);
		}

		/* the learning rate */
		double alpha = 1.0;
		ArrayList<Double> final_weight = perceptron.learn_perceptron(
				twogaussian, initial_weight, alpha);
		System.out.println("The final weight " + final_weight);
	}
}
