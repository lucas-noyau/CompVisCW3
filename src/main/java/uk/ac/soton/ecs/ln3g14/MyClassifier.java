package uk.ac.soton.ecs.ln3g14;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;


public abstract class MyClassifier {
	
	GroupedDataset<String,ListDataset<FImage>,FImage> trainingData, testingData;

	MyClassifier() {
	}
	
	MyClassifier(String trainingDataPath, String testingDataPath) {
		this.trainingData = getData(trainingDataPath);
		this.testingData = getData(testingDataPath);
	}
	
	static GroupedDataset<String, ListDataset<FImage>, FImage> getData(String path) {
		System.out.println("	Getting dataset from path string");
		try {
			VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>(path, ImageUtilities.FIMAGE_READER);
			return GroupSampler.sample(dataset, dataset.size(), false);
		} catch (FileSystemException e) {
			System.err.println("Could not read data from path string");
		}
		return null;
	}
	
	void go() {
		this.train(trainingData);
		System.out.println("	Testing Against Data");
		ArrayList<String> results = classify(testingData);
		printResults(results);
	}
	
	void printResults(ArrayList<String> results) {
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("output.txt"));
			for (int i = 0; i < results.size(); i++) {
			String output = i + ".jpg " + results.get(i);
				out.println(output);
				System.out.println(output);
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Could not write to file");
			e.printStackTrace();
		}
	}
	
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
	
	abstract void train(GroupedDataset<String,ListDataset<FImage>,FImage> data);
	abstract String classify(FImage image);
}
