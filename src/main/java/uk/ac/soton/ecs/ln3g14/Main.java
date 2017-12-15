package uk.ac.soton.ecs.ln3g14;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.FImage;

public class Main {
	
	static String trainingDataPath = "/Users/lucas/Desktop/openimaj-coursework3/data/training/";
	static String trainingDataPathSmall = "/Users/lucas/Desktop/openimaj-coursework3/data/training_small/";
	static String testingDataPath = "zip:/Users/lucas/Desktop/openimaj-coursework3/data/testing.zip";	
	static String testingDataPathSmall = "zip:/Users/lucas/Desktop/openimaj-coursework3/data/testing_small.zip";	

	public static void main(String[] args) {
//		new Run1(trainingDataPath, testingDataPath).go();
		new Run2(trainingDataPathSmall, testingDataPathSmall).go();
	}
	
	GroupedDataset<String, ListDataset<FImage>, FImage> generateSubsets(VFSGroupDataset<FImage> input, int numGroups) {
		System.out.println("	Generating subsets of training and testing data");
		return GroupSampler.sample(input, numGroups, false);
	}
}