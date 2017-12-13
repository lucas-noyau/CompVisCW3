package uk.ac.soton.ecs.ln3g14;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntFloatPair;


/*
 * K-nearest-neighbour classifier using the "tiny image" feature.
 * Crop to square
 * Simplify to fixed resolution
 * Pack pixels into vector
 * Use k-nearest-neighbour with best possible k-value
 */
public class Run2 extends Classifier {

	Run2() {
		super();
	}
	
	Run2(String trainingDataPath, String testingDataPath) {
		super(trainingDataPath, testingDataPath);
	}
	
	@Override
	void go() {
		System.out.println("Generate Feature Vectors");
		// Generate feature vectors
		// Save feature vectors
		System.out.println("Testing Against Data");
		// Test feature vectors
		classify(testingData);
	}
	
	/*
	 * 
	 */
	
	
	
	
	
	/*
	 * 
	 */
	static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift) {
		return null;
	}
	
	static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {
		@Override
		public DoubleFV extractFeature(Record<FImage> arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/*
	 * Run against testing data
	 */
	void classify(GroupedDataset<String,ListDataset<FImage>,FImage> data) {

	}
	
	/*
	 * Run against single image
	 */
	void classify(FImage image) {

	}
}