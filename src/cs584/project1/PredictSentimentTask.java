package cs584.project1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class PredictSentimentTask implements Callable<String> {
	private final int threadCount;
	private final int threadId;
	private final List<Review> trainingSet;
	private final Map<Integer, Review> testSet;
	private final Map<Integer, List<TfIdfResult>> resultsMap;
	private final List<Integer> kValues;
	private final FrequencyModel frequencyModel;
	private final Mode mode;
	
	public PredictSentimentTask(int threadCount, int threadId, List<Review> trainingSet, Map<Integer, Review> testSet, Map<Integer, List<TfIdfResult>> resultsMap, List<Integer> kValues, FrequencyModel frequencyModel, Mode mode) {
		this.threadCount = threadCount;
		this.threadId = threadId;
		this.trainingSet = trainingSet;
		this.testSet = testSet;
		this.resultsMap = resultsMap;
		this.kValues = kValues;
		this.frequencyModel = frequencyModel;
		this.mode = mode;
		
		Collections.sort(this.kValues);
	}
	
	@Override
	public String call() throws Exception {
		
		Integer maxKValue = kValues.stream().max(Integer::compare).get();
		
		for(Map.Entry<Integer, Review> predictReviewEntry : testSet.entrySet()) {
			
			Integer testSetIndex = predictReviewEntry.getKey();
			Review predictReview = predictReviewEntry.getValue();

			if(Mode.SUBMISSION.equals(mode)) {
				SparseVector weightVector = frequencyModel.computeWeightVector(predictReview);
				predictReview.setWeightVector(weightVector);
			}
			
			SparseVector predictWeightVector = predictReview.getWeightVector();
			
			FixedSizePriorityQueue<ReviewComparisonResult> kMeansQueueMax = new FixedSizePriorityQueue<>(PriorityQueueType.MAX, maxKValue);
			FixedSizePriorityQueue<ReviewComparisonResult> kMeansQueueMin = new FixedSizePriorityQueue<>(PriorityQueueType.MIN, maxKValue);
			
			for(Review compareReview : trainingSet) {
				boolean validComparison = true;
				
				if(Mode.CROSS_VALIDATION.equals(mode)) {
					Integer predictDataGroupId = predictReview.getDataGroupId();
					if(predictDataGroupId.equals(compareReview.getDataGroupId())) {
						validComparison = false;
					}
				}
				
				if(validComparison) {
					SimilarityMetric metric = Constants.SIMILARITY_METRIC;
					Double similarity = 0.0;
					
					if(SimilarityMetric.COSINE_SIMILARITY.equals(metric)) {
						similarity = Utilities.cosineSimilarity(predictWeightVector, compareReview.getWeightVector());
					}
					else {
						Set<Integer> predictVectorInt = predictWeightVector.getNonZeroIndices();
						Set<Integer> compareVectorInt = compareReview.getWeightVector().getNonZeroIndices();
						
						if(SimilarityMetric.DICES_COEFFICIENT.equals(metric)) {
							similarity = Utilities.dicesCoefficient(predictVectorInt, compareVectorInt);
						}
						else if(SimilarityMetric.JACCARDS_COEFFICIENT.equals(metric)) {
							similarity = Utilities.jaccardsCoefficient(predictVectorInt, compareVectorInt);
						}
						else if(SimilarityMetric.COSINE_COEFFICIENT.equals(metric)) {
							similarity = Utilities.cosineCoefficient(predictVectorInt, compareVectorInt);
						}
						else if(SimilarityMetric.OVERLAP_COEFFICIENT.equals(metric)) {
							similarity = Utilities.overlapCoefficient(predictVectorInt, compareVectorInt);
						}
					}
										
					ReviewComparisonResult rcr = new ReviewComparisonResult(compareReview, similarity);
					kMeansQueueMax.add(rcr);
					kMeansQueueMin.add(rcr);
				}
			}
			
			for(Integer kValue : kValues) {
				// Get Results for MAX queue.
				Map<Rating, Double> maxWeights = getTotalWeights(kMeansQueueMax, kValue);
				
				Rating predictedRating = Rating.NONE;
				double confidence = 0.5;
				
				Double positivePredictions = maxWeights.get(Rating.POSITIVE);
				Double negativePredictions = maxWeights.get(Rating.NEGATIVE);
				
				
				if(positivePredictions >= negativePredictions) {
					predictedRating = Rating.POSITIVE;
					confidence = (1.0 * positivePredictions) / (positivePredictions + negativePredictions);
				}
				else {
					predictedRating = Rating.NEGATIVE;
					confidence = (1.0 * negativePredictions) / (positivePredictions + negativePredictions);
				}
				
				TfIdfResult result = new TfIdfResult();
				result.setReview(predictReview);
				result.setPredictedRating(predictedRating);
				result.setConfidence(confidence);
				
				if(Constants.VERBOSE) {
					System.out.println("Predicting sentiment for review on line #" + predictReview.getInputLineNumber() + " with K = " + kValue + " by Thread ID " + threadId); // + ". Closest matches were from lines " + sourceLines);
					System.out.println("Prediction is: " + predictedRating.name());
				}
				
				if(Mode.CROSS_VALIDATION.equals(mode)) {
					boolean correct = predictedRating.equals(predictReview.getGivenRating());
					if(Constants.VERBOSE) {
						System.out.println("Result was: " + (correct ? "CORRECT" : "INCORRECT") + " and the confidence was: " + confidence + ".\n");
					}
				}
				else {
					if(Constants.VERBOSE) {
						System.out.println();
					}
				}
				
				List<TfIdfResult> results = resultsMap.get(kValue);

				results.set(testSetIndex, result);

			}
		}
		
		return "Finished processing for thread #" + threadId;
	}
	
	private Map<Rating, Double> getTotalWeights(FixedSizePriorityQueue<ReviewComparisonResult> kMeansQueueMin, Integer kValue) {
		Map<Rating, Double> weights = new HashMap<>();
		Double positivePredictionsMinTotal = 0.0;
		Double negativePredictionsMinTotal = 0.0;
		TreeSet<ReviewComparisonResult> minMatches = kMeansQueueMin.getBackingTreeSet();
		//List<Integer> sourceLines = new ArrayList<>();
		
		Iterator<ReviewComparisonResult> minIterator = minMatches.iterator();
		
		Integer currentMinQueueIndex = 0;
	
		while(minIterator.hasNext() && currentMinQueueIndex < kValue) {
			ReviewComparisonResult result = minIterator.next();
			
			Rating resultRating = result.getReview().getGivenRating();
			if(Rating.POSITIVE.equals(resultRating)) {
				positivePredictionsMinTotal += frequencyModel.getPositiveWeight();
			}
			else if(Rating.NEGATIVE.equals(resultRating)) {
				negativePredictionsMinTotal += frequencyModel.getNegativeWeight();
			}
			
			currentMinQueueIndex++;
		}
		
		weights.put(Rating.POSITIVE, positivePredictionsMinTotal);
		weights.put(Rating.NEGATIVE, negativePredictionsMinTotal);
		
		return weights;
	}
	
	public int getNextReviewId(int i) {
		return (i * threadCount) + threadId;
	}

}
