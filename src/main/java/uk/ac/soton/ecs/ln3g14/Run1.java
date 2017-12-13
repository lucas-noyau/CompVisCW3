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
public class Run1 extends Classifier {
	
	final int kValue = 15;
	final int resolution = 16;

	List<String> classes;
	List<double[]> featureVectors;

	private DoubleNearestNeighboursExact knn;

	Run1() {
		super();
	}
	
	Run1(String trainingDataPath, String testingDataPath) {
		super(trainingDataPath, testingDataPath);
	}
	
	@Override
	void go() {
		System.out.println("	Generate Feature Vectors");
		// Generate feature vectors
		double[][] featureVectors = extractFeatureVector(trainingData);
		// Save feature vectors
		knn = new DoubleNearestNeighboursExact(featureVectors);
		System.out.println("	Testing Against Data");
		// Test feature vectors
		ArrayList<String> results = classify(testingData);
		printResults(results);
	}
	
	/*
	 * Extracts the feature vectors from a dataset
	 */
	double[][] extractFeatureVector(GroupedDataset<String, ListDataset<FImage>, FImage> data) {
		classes = new ArrayList<String>();
		featureVectors = new ArrayList<double[]>();
		// For image in the dataset
		for(String group : data.getGroups()){
			System.out.println(group);
			for(FImage image : data.get(group)){
				// extract feature vector
				double[] vector = extractFeatureVector(image);
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
	double[] extractFeatureVector(FImage image) {
		// Get shortest side of image
		int side = Math.min(image.height, image.width);
		// Get cropped image
		FImage square = image.extractCenter(side, side);
		// Resize image
		FImage tiny = square.process(new ResizeProcessor(resolution, resolution));
		// Turn 2D array into 1D vector
		DoubleFV vector = new DoubleFV(ArrayUtils.reshape(ArrayUtils.convertToDouble(tiny.pixels))).normaliseFV();
		// Return vector
		return vector.values;
	}
	
	/*
	 * Run against testing data
	 */
	ArrayList<String> classify(GroupedDataset<String,ListDataset<FImage>,FImage> data) {
		ArrayList<String> results = new ArrayList<String>();
		for(String group : data.getGroups()){
			for(int i = 0; i < data.get(group).size(); i++) {
				FImage image = data.get(group).get(i);
				String guessedClass = classify(image);
				results.add(guessedClass);
			}
		}
		return results;
	}
	
	/*
	 * Run against single image
	 */
	String classify(FImage image) {
		// Create Feature Vector for image
		double[] vector = extractFeatureVector(image);
		// Find k nearest neighbour
		List<IntDoublePair> neighbours = knn.searchKNN(vector, kValue);
		// Count neighbours for each class
		Map<String,Integer> classCount = new HashMap<String,Integer>();
		// For all neighbours
		for (IntDoublePair n: neighbours) {
			// Get class
			String nClass = classes.get(n.first);
			// Add 1 to the classCount for the class
			if (classCount.containsKey(nClass)) {
				classCount.put(nClass, classCount.get(nClass)+1);
			} else {
				classCount.put(nClass, 1);
			}
		}
		// Sorting list by number of neighbours of that class
		List<Map.Entry<String, Integer>> guessList = new ArrayList<Map.Entry<String, Integer>>(classCount.entrySet());
		Collections.sort(guessList, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2){
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		String guessedClass = guessList.get(0).getKey();
		return guessedClass;
	}
}