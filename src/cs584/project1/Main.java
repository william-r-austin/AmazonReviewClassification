package cs584.project1;

public class Main {

	public static void main(String[] args) {
		
		testSimilarityMetrics();
		testStemmers();
	}
	
	private static void testStemmers() {
		for(StemmingType stemmingType : StemmingType.values()) {
			Constants.STEMMING_TYPE = stemmingType;
			System.out.println("Beginnning processing with stemming type: " + stemmingType.name());
			long startTime = System.currentTimeMillis();
			ReviewProcessor processor = new ReviewProcessor();
			processor.runTraining(Constants.CROSS_VALIDATION_FOLDS);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Done with metric. Total time was: " + totalTime);
		}
	}
	
	private static void testSimilarityMetrics() {
		for(SimilarityMetric metric : SimilarityMetric.values()) {
			Constants.SIMILARITY_METRIC = metric;
			System.out.println("Beginnning processing with metric: " + metric.name());
			long startTime = System.currentTimeMillis();
			ReviewProcessor processor = new ReviewProcessor();
			processor.runTraining(Constants.CROSS_VALIDATION_FOLDS);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Done with metric. Total time was: " + totalTime);
		}
	}
}
