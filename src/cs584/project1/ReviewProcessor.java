package cs584.project1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ReviewProcessor {
	
	private Set<String> performDimensionalityReduction(List<Review> trainingSet, Set<String> terms) {
		// Part 1 - Remove short words that would be stop words.
		System.out.println("Initial term count: " + terms.size());
		terms = filterByTermLength(terms);
		System.out.println("After removing short words: " + terms.size());
		populateTermFrequency(trainingSet, terms);

		// Part 2. Remove words that don't indicate positive or negative sentiment
		terms = filterByPredictiveValue(trainingSet, terms);
		System.out.println("After removing non-predictive words: " + terms.size());
		populateTermFrequency(trainingSet, terms);
		
		// Part 3. Filter out words that are only used once in the training set.
		terms = filterByFrequency(trainingSet, terms);
		System.out.println("After removing single use words: " + terms.size());
		populateTermFrequency(trainingSet, terms);
		
		return terms;
	}
	
	public void runSubmission() {
		// Read the training set
		ReviewParser reviewParser = new ReviewParser();
		reviewParser.parseReviewsForTrainingSet();
		List<Review> trainingSet = reviewParser.getReviewsForTrainingSet();

		// Preprocess to reduce the total number of terms
		Set<String> terms = reviewParser.getTermsForTrainingSet();
		terms = performDimensionalityReduction(trainingSet, terms);
		
		// Read the test set
		reviewParser.parseReviewsForTestSet();
		List<Review> testSet = reviewParser.getReviewsForTestSet();
		populateTermFrequency(testSet, terms);
		
		// Create the TF-IDF model
		FrequencyModel frequencyModel = new FrequencyModel();
		frequencyModel.construct(trainingSet);
		
		// Predict output class for the test file
		List<Integer> kValues = Arrays.asList(Constants.KNN_CONSTANT);
		Map<Integer, List<TfIdfResult>> resultsMap = createResultsMap(testSet.size(), kValues);
		predictSentiment(Constants.THREAD_COUNT, kValues, trainingSet, testSet, resultsMap, frequencyModel, Mode.SUBMISSION);
		
		// Create the output file
		List<TfIdfResult> testResults = resultsMap.get(Constants.KNN_CONSTANT);
		createOutputFile(testResults);
	}
	
	public void createOutputFile(List<TfIdfResult> results) {
		//long currentTime = System.currentTimeMillis();
		
		ZoneId zoneId = ZoneId.of("America/New_York");
		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), zoneId);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss");
		String timeOutput = zdt.format(formatter);
		
		
		File outputFile = new File("resources/output/prediction_waustin_" + timeOutput + ".dat");
		try {
			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			for(TfIdfResult result : results) {
				String output = result.getPredictedRating().getLabel();
				bufferedWriter.write(output + System.lineSeparator());
			}
			
			bufferedWriter.close();
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Set<String> filterByPredictiveValue(List<Review> trainingSet, Set<String> terms) {
		Set<String> termsCopy = new HashSet<>(terms);
		CombinedNaiveBayesModel bayesModel = new CombinedNaiveBayesModel();
		bayesModel.construct(trainingSet, Mode.SUBMISSION);
		
		Map<String, Double> predictiveValues = bayesModel.getTermsByPredictiveValue(terms);
		
		for(Map.Entry<String, Double> entry : predictiveValues.entrySet()) {
			if(entry.getValue() < 0.50001) {
				String term = entry.getKey();
				termsCopy.remove(term);
			}
		}
		
		return termsCopy;
	}
	
	private Set<String> filterByFrequency(List<Review> trainingSet, Set<String> terms) {
		FrequencyModel frequencyModel = new FrequencyModel();
		frequencyModel.construct(trainingSet);
		
		Set<String> termsCopy = new HashSet<>();
		for(String term : terms) {
			Integer count = frequencyModel.getGlobalTermFrequency(term);
			if(count > 1) {
				termsCopy.add(term);
			}
		}
		
		return termsCopy;
	}
	
	public void runTraining(Integer dataGroupSize) {
		ReviewParser reviewParser = new ReviewParser();
		reviewParser.parseReviewsForTrainingSet();
		List<Review> trainingSet = reviewParser.getReviewsForTrainingSet();
		
		// Partition the training set for K-fold cross validation
		partitionReviewDataSet(trainingSet, dataGroupSize);
				
		// Preprocess to reduce the total number of terms
		Set<String> terms = reviewParser.getTermsForTrainingSet();
		terms = performDimensionalityReduction(trainingSet, terms);
				
		// Create the TF-IDF model
		FrequencyModel frequencyModel = new FrequencyModel();
		frequencyModel.construct(trainingSet);
		
		List<Integer> kValues = Arrays.asList(1, 3, 5, 7, 11, 15, 23, 31, 47, 63, 95, 
				127, 181, 255, 383, 511, 767, 1023, 1535, 2047, 3071, 4095, 6143, 8191, 9253);
		
		List<Review> testSet = new ArrayList<>(trainingSet);
		
		Map<Integer, List<TfIdfResult>> resultsMap = createResultsMap(testSet.size(), kValues);
		predictSentiment(Constants.THREAD_COUNT, kValues, trainingSet, testSet, resultsMap, frequencyModel, Mode.CROSS_VALIDATION);
		
		for(Integer k : kValues) {
			List<TfIdfResult> results = resultsMap.get(k);
			
			int correctCount = 0;
			for(TfIdfResult result : results) {
				Review review = result.getReview();
				Rating givenRating = review.getGivenRating();
				Rating predictedRating = result.getPredictedRating();
				if(Objects.equals(givenRating, predictedRating)) {
					correctCount++;
				}
			}
					
			double accuracy = (100.0 * correctCount) / results.size();
			String accuracyStr = String.format("%.2f", accuracy);
			System.out.println("Finished computation for k = " + k + ". Total correct predictions = " + correctCount + " / " + results.size() + ", accuracy = " + accuracyStr + "%");
		}
	}
	
	private Set<String> filterByTermLength(Set<String> input) {
		return input.stream()
					.filter(x -> x.length() >= Constants.MIN_WORD_LENGTH)
					.collect(Collectors.toSet());
	}
	
	private void partitionReviewDataSet(List<Review> reviews, Integer dataGroupSize) {
		for(Review review : reviews) {
			Integer dataGroupId = (review.getInputLineNumber() % dataGroupSize);
			review.setDataGroupId(dataGroupId);
		}
	}
	
	
	private void populateTermFrequency(List<Review> reviews, Set<String> terms) {
		for(Review currentReview : reviews) {
			List<String> sampleTokens = currentReview.getTokenList();

			Map<String, Integer> termFrequency = new HashMap<>();
			
			for(String sampleToken : sampleTokens) {
				if(terms.contains(sampleToken)) {
					Utilities.incrementFrequencyMap(termFrequency, sampleToken);
				}
			}
			
			currentReview.setTermFrequency(termFrequency);
		}
	}
	
	private Map<Integer, List<TfIdfResult>> createResultsMap(int size, List<Integer> kValues) {
		Map<Integer, List<TfIdfResult>> results = new HashMap<>();
		
		for(Integer kValue : kValues) {
			List<TfIdfResult> resultsBacker = new ArrayList<>(Collections.nCopies(size, null));
			List<TfIdfResult> resultsList = Collections.synchronizedList(resultsBacker);
			results.put(kValue, resultsList);
		}
		
		return Collections.synchronizedMap(results);
	}
	
	private void predictSentiment(int threadCount, List<Integer> kValues, List<Review> trainingSet, List<Review> testSet, Map<Integer, List<TfIdfResult>> results, FrequencyModel frequencyModel, Mode mode) {
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		List<Future<String>> threadFutures = new ArrayList<>();
		
		Map<Integer, Map<Integer, Review>> threadTestSetMap = new LinkedHashMap<>();
		
		int i = 0;
		while(i < testSet.size()) {
			Review currentReview = testSet.get(i);
			Integer threadId = i % threadCount;
			
			Map<Integer, Review> threadMap = threadTestSetMap.get(threadId);
			 
			if(threadMap == null) {
				threadMap = new LinkedHashMap<>();
				threadTestSetMap.put(threadId, threadMap);
			}
			
			threadMap.put(i, currentReview);
			i++;
		}
		
		for(int j = 0; j < threadCount; j++) {
			Map<Integer, Review> threadTestSet = threadTestSetMap.get(j);
			PredictSentimentTask predictSentimentTask = new PredictSentimentTask(threadCount, j, trainingSet, threadTestSet, results, kValues, frequencyModel, mode);
			Future<String> x = executorService.submit(predictSentimentTask);
			threadFutures.add(x);
		}
		
		for(Future<String> x : threadFutures) {
			try {
				String result = x.get();
				System.out.println("Got thread result: " + result);
			} 
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		executorService.shutdown();
		System.out.println("Prediction Complete!");
	}
}
