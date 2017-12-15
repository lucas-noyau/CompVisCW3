package uk.ac.soton.ecs.ln3g14;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureImpl;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;


/*
 * Set of linear classifiers using bag-of-words.
 *
 * Get vocabImages from each class
 * Sample dense patches of set size for each step
 * create a feature vector for each patch
 * use k-means on the vectors to obtain a set number of clusters
 * Generate a HardAssigner and a feature extractor
 *
 */
public class Run2 extends MyClassifier {

	// Clustering params
	static int clusters = 500;

	// Patch params
	static float step = 4;
	static float patchSize = 8;

	LiblinearAnnotator<FImage,String> annotator;

	Run2() {
		super();
	}

	Run2(String trainingDataPath, String testingDataPath) {
		super(trainingDataPath, testingDataPath);
	}

	@Override
	void train(GroupedDataset<String,ListDataset<FImage>,FImage> data) {
		System.out.println("	Generating Vocab");
		HardAssigner<float[],float[],IntFloatPair> assigner = trainQuantiser(data);
		System.out.println("	Generating FeatureExtractor");
		FeatureExtractor<DoubleFV,FImage> featureExtractor = new Extractor(assigner);
		System.out.println("	Generating Linear Classifier");
		annotator = new LiblinearAnnotator<FImage,String>(featureExtractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
		System.out.println("	Training Linear Classifier");
		annotator.train(data);
		System.out.println("	Training Complete");
	}

	/*
	 *
	 */
	static HardAssigner<float[],float[],IntFloatPair> trainQuantiser(Dataset<FImage> data) {
		List<float[]> allkeys = new ArrayList<float[]>();
		for (FImage image : data) {
			for (LocalFeature<SpatialLocation, FloatFV> feature : extractFeature(image)) {
				allkeys.add(feature.getFeatureVector().values);
			}
		}
		FloatKMeans km = FloatKMeans.createKDTreeEnsemble(clusters);
		float[][] datasource = allkeys.toArray(new float[][]{});
		FloatCentroidsResult result = km.cluster(datasource);
		return result.defaultHardAssigner();
	}

	/*
	 *
	 */
	static List<LocalFeature<SpatialLocation,FloatFV>> extractFeature(FImage image) {
		List<LocalFeature<SpatialLocation,FloatFV>> featureList = new ArrayList<LocalFeature<SpatialLocation,FloatFV>>();
		// Position for patches
		RectangleSampler rectangles = new RectangleSampler(image, step, step, patchSize, patchSize);
		// extract feature from each position
		for (Rectangle rectangle : rectangles) {
			FImage area = image.extractROI(rectangle);

			float[] vector = ArrayUtils.reshape(area.pixels);
			float average = centeredAverage(vector);

			FloatFV featureV = new FloatFV(vector);
			SpatialLocation location = new SpatialLocation(rectangle.x, rectangle.y);
			LocalFeature<SpatialLocation, FloatFV> feature = new LocalFeatureImpl<SpatialLocation, FloatFV>(location, featureV); //should just be (location, value)?
			featureList.add(feature);
		}
		return featureList;
	}
	
	/*
	 * Find Centered Average of Double Array
	 */
	public static float centeredAverage(float[] vals) {
		float sum = 0;
		float min = vals[0];
		float max = vals[0];

		for(int i = 0; i < vals.length; i++) {
				sum += vals[i];
				min = Math.min(min, vals[i]);
				max = Math.max(max, vals[i]);
		}

	return (sum - min - max) / (vals.length - 2);
	}

	/*
	 * Our extractor class
	 */
	static class Extractor implements FeatureExtractor<DoubleFV,FImage> {
		HardAssigner<float[],float[],IntFloatPair> assigner;
		Extractor(HardAssigner<float[],float[],IntFloatPair> assigner) {this.assigner = assigner;}
		@Override
		public DoubleFV extractFeature(FImage image) {
			BagOfVisualWords<float[]> bagOfWords = new BagOfVisualWords<float[]>(assigner);
			BlockSpatialAggregator<float[],SparseIntFV> aggregator = new BlockSpatialAggregator<float[],SparseIntFV>(bagOfWords, 2, 2);
			return aggregator.aggregate(Run2.extractFeature(image), image.getBounds()).normaliseFV();
		}
	}

	/*
	 * Run against single image
	 */
	@Override
	String classify(FImage image) {
		return annotator.classify(image).getPredictedClasses().iterator().next();
	}
}
