package cs584.project1;

import java.util.Set;
import java.util.function.BiFunction;

public class Constants {
	public static final String TRAINING_FILE = "resources/1580449515_4035058_train_file.dat";
	public static final String TEST_FILE = "resources/1580449515_4313154_test.dat";
	
	public static final Mode CURRENT_MODE = Mode.CROSS_VALIDATION;
	
	public static final int THREAD_COUNT = 4;
	
	public static final int CROSS_VALIDATION_FOLDS = 7;
	
	public static final int KNN_CONSTANT = 3071;
	
	public static final double SMOOTHING_CONSTANT = 0.6;
	
	public static final BiFunction<Set<Integer>, Set<Integer>, Double> COEFFICIENT_FUNCTION = Utilities::jaccardsCoefficient;
	
	public static StemmingType STEMMING_TYPE = StemmingType.NONE;
	
	public static final int MIN_WORD_LENGTH = 4;
	
	public static SimilarityMetric SIMILARITY_METRIC = SimilarityMetric.COSINE_SIMILARITY;
	
	public static final Boolean VERBOSE = false;
	
}
