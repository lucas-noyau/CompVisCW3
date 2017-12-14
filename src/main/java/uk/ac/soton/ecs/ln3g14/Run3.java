package uk.ac.soton.ecs.ln3g14;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntDoublePair;


/*
 * K-nearest-neighbour classifier using the "tiny image" feature.
 * Crop to square
 * Simplify to fixed resolution
 * Pack pixels into vector
 * Use k-nearest-neighbour with best possible k-value
 */
public class Run3 extends MyClassifier {
	
	final int kValue = 15;
	final int resolution = 16;

	List<String> classes;
	List<double[]> featureVectors;

	private DoubleNearestNeighboursExact knn;

	Run3() {
		super();
	}
	
	Run3(String trainingDataPath, String testingDataPath) {
		super(trainingDataPath, testingDataPath);
	}
	
	@Override
	void train(GroupedDataset<String,ListDataset<FImage>,FImage> data) {
		System.out.println("	Generate Feature Vectors");
		// Generate feature vectors
		double[][] featureVectors = extractFeature(trainingData);
		// Save feature vectors
		knn = new DoubleNearestNeighboursExact(featureVectors);
	}
	
	/*
	 * Extracts the feature vectors from a dataset
	 */
	double[][] extractFeature(GroupedDataset<String, ListDataset<FImage>, FImage> data) {
		classes = new ArrayList<String>();
		featureVectors = new ArrayList<double[]>();
		// For image in the dataset
		for(String group : data.getGroups()){
			System.out.println(group);
			for(FImage image : data.get(group)){
				// extract feature vector
				double[] vector = extractFeature(image);
				// Add data to lists
				featureVectors.add(vector);
				classes.add(group);
			}
		}
		// Return list of all feature vectors
		return featureVectors.toArray(new double[][]{});
	}

	/*
	 * Extracts the feature vector from an image
	 */
	double[] extractFeature(FImage image) {
		return null;
	}
	
	/*
	 * Run against single image
	 */
	@Override
	String classify(FImage image) {
		return null;
	}
}