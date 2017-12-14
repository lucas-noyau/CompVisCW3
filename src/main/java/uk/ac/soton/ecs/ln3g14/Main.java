package uk.ac.soton.ecs.ln3g14;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.FImage;

public class Main {
	
	String trainingDataPath = "/Users/lucas/Desktop/openimaj-coursework3/data/training_small/";
	String testingDataPath = "zip:/Users/lucas/Desktop/openimaj-coursework3/data/testing.zip";	

	public static void main(String[] args) {
		Main m = new Main();
//		m.run1();
		m.run2();
		
	}
	
	GroupedDataset<String, ListDataset<FImage>, FImage> generateSubsets(VFSGroupDataset<FImage> input, int numGroups) {
		System.out.println("	Generating subsets of training and testing data");
		return GroupSampler.sample(input, numGroups, false);
	}
	
	void run1() {
		System.out.println("	Initialising");
		Classifier c = new Run1(trainingDataPath, testingDataPath);
		System.out.println("	Starting classifier up");
		c.go();
		System.out.println("	Completed");
	}
	
	void run2() {
		System.out.println("	Initialising");
		Classifier c = new Run2(trainingDataPath, testingDataPath);
		System.out.println("	Starting classifier up");
		c.go();
		System.out.println("	Completed");
	}
}