package uk.ac.soton.ecs.ln3g14;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureImpl;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.image.FImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;
import uk.ac.soton.ecs.ln3g14.Run2.Extractor;
import uk.ac.soton.ecs.ln3g14.ln3g14ch12.PHOWExtractor;


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

	LiblinearAnnotator<Record<FImage>, String> annotator;

	Run3() {
		super();
	}

	Run3(String trainingDataPath, String testingDataPath) {
		super(trainingDataPath, testingDataPath);
	}

	@Override
	void train(GroupedDataset<String,ListDataset<FImage>,FImage> data) {
		System.out.println("	creating SIFT extractor");
		DenseSIFT dsift = new DenseSIFT(5, 7);
		PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
		System.out.println("	creating assigner");
		HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);
		System.out.println("	creating PHOW extractor");
		FeatureExtractor<DoubleFV, Record<FImage>> extractor = new PHOWExtractor(pdsift, assigner);
		System.out.println("	training classifier");
		annotator = new LiblinearAnnotator<Record<FImage>, String>(extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
		annotator.train(data);
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
