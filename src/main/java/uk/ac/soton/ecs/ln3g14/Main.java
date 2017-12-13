package uk.ac.soton.ecs.ln3g14;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.FImage;

public class Main {
	
	String trainingDataPath = "/Users/lucas/Desktop/openimaj-coursework3/data/training/";
	String testingDataPath = "zip:/Users/lucas/Desktop/openimaj-coursework3/data/testing.zip";	

	public static void main(String[] args) {
		Main m = new Main();
		m.testClassifierGetData();
	}
	
	GroupedDataset<String, ListDataset<FImage>, FImage> generateSubsets(VFSGroupDataset<FImage> input, int numGroups) {
		System.out.println("	Generating subsets of training and testing data");
		return GroupSampler.sample(input, numGroups, false);
	}
	
	void testClassifierGetData() {
		Run1 c = new Run1(trainingDataPath, testingDataPath);
		System.out.println("Training Data:\t" + c.trainingData);
		System.out.println("Testing Data:\t" + c.testingData);
		System.out.println("	Starting classifier up");
		c.go();
		System.out.println("	Completed");
	}
}