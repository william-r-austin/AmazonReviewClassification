package cs584.project1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CombinedNaiveBayesModel {
	private List<NaiveBayesModel> allModels = new ArrayList<>();
	
	public CombinedNaiveBayesModel() {
		NaiveBayesModel nbm = new NaiveBayesModel(0);
		allModels.add(nbm);
	}
	
	public CombinedNaiveBayesModel(int k) {
		for(int x = 0; x < k; x++) {
			NaiveBayesModel nbm = new NaiveBayesModel(x);
			allModels.add(nbm);
		}
	}
	
	public void construct(List<Review> trainingSet, Mode mode) {
		for(Review review : trainingSet) {
			Integer dataGroupId = 0;
			Set<String> words = review.getTerms();
			Rating lineRating = review.getGivenRating();
			
			if(Mode.CROSS_VALIDATION.equals(mode)) {
				dataGroupId = review.getDataGroupId();
			}
			
			processLine(words, dataGroupId, lineRating);
		}
	}
	
	public Map<String, Double> getTermsByPredictiveValue(Set<String> terms) {
		Map<String, Double> predictiveValueHashMap = new HashMap<>();
		for(String term : terms) {
			Integer positiveTermFrequencyCount = 0; //Utilities.getTermFrequency(positiveTermFrequency, term);
			Integer negativeTermFrequencyCount = 0; //Utilities.getTermFrequency(negativeTermFrequency, term);
			for(NaiveBayesModel model : allModels) {
				positiveTermFrequencyCount += model.getPositiveFrequencyCount(term);
				negativeTermFrequencyCount += model.getNegativeFrequencyCount(term);
			}
			
			Double positivePredictiveValue = (1.0 * positiveTermFrequencyCount) / (positiveTermFrequencyCount + negativeTermFrequencyCount); 
			Double predictiveValue = Math.max(positivePredictiveValue, 1.0 - positivePredictiveValue);
			predictiveValueHashMap.put(term, predictiveValue);
		}
		
		
		Map<String, Double> sortedMap = sortByValue(predictiveValueHashMap);
		return sortedMap;
	}
	
	private Map<String, Double> sortByValue(Map<String, Double> input) {
		List<Entry<String, Double>> list = new ArrayList<>(input.entrySet());
        list.sort(Entry.comparingByValue());

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Entry<String, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
	}
	
	public void processLine(Set<String> words, Integer dataGroupId, Rating lineRating) {
		NaiveBayesModel model = allModels.get(dataGroupId);
		
		if(Rating.POSITIVE.equals(lineRating)) {
			for(String p : words) {
				model.incrementPositiveFrequencyCount(p);
			}
			
			model.incrementTotalPositiveLines();
		}
		else if(Rating.NEGATIVE.equals(lineRating)) {
			for(String n : words) {
				model.incrementNegativeFrequencyCount(n);
			}
			
			model.incrementTotalNegativeLines();
		}
	}
	
	public List<TfIdfResult> predict(List<Review> predictSet, Mode mode, Double alpha) {

		List<TfIdfResult> results = new ArrayList<>();
		
		for(Review reviewToPredict : predictSet) {
			TfIdfResult result = getPrediction(reviewToPredict, mode, alpha);
			results.add(result);
			/*
			//System.out.println("Prediction for line #" + reviewToPredict.getInputLineNumber());
			if(prediction.equals(reviewToPredict.getGivenRating())) {
				//System.out.println("Result is: CORRECT\n");
				correctCount++;
			}
			else {
				//System.out.println("Result is: NOT CORRECT\n");
			}*/
		}
		
		return results;
	}
	
	/*
	
	public Map<String, Double> g() {
		Set<String> allModelTerms = new HashSet<>();
		for(NaiveBayesModel m : allModels) {
			
		}
	}*/
	
	public TfIdfResult getPrediction(Review review, Mode mode, double alpha) {
		Set<String> terms = review.getTerms();
				
		Rating prediction = Rating.NONE;
		Integer totalLines = 0;
		Integer totalPositiveLines = 0;
		Integer totalNegativeLines = 0;
		
		Map<String, Integer> positiveTermFrequency = new HashMap<>();
		Map<String, Integer> negativeTermFrequency = new HashMap<>();
		
		for(NaiveBayesModel nbm : allModels) {
			boolean validModel = true;
			
			if(Mode.CROSS_VALIDATION.equals(mode)) {
				Integer dataGroupId = review.getDataGroupId();
				if(nbm.getModelId().equals(dataGroupId)) {
					validModel = false;
				}
			}
			
			if(validModel) {
				totalLines += nbm.getTotalLines();
				totalPositiveLines += nbm.getTotalPositiveLines();
				totalNegativeLines += nbm.getTotalNegativeLines();
				
				for(String term : terms) {
					Integer positiveFrequency = nbm.getPositiveFrequencyCount(term);
					Integer negativeFrequency = nbm.getNegativeFrequencyCount(term);
					
					Utilities.incrementFrequencyMap(positiveTermFrequency, term, positiveFrequency);
					Utilities.incrementFrequencyMap(negativeTermFrequency, term, negativeFrequency);
				}
			}
		}
		
		double positiveFactor = ((alpha + totalPositiveLines) / (totalLines + (2 * alpha))); 
		double negativeFactor = ((alpha + totalNegativeLines) / (totalLines + (2 * alpha)));
		
		for(String term : terms) {
			Integer positiveTermFrequencyCount = Utilities.getTermFrequency(positiveTermFrequency, term);
			Integer negativeTermFrequencyCount = Utilities.getTermFrequency(negativeTermFrequency, term);
			
			double currentPositiveFactor = (alpha + positiveTermFrequencyCount) / (totalPositiveLines + (2 * alpha));
			double currentNegativeFactor = (alpha + negativeTermFrequencyCount) / (totalNegativeLines + (2 * alpha));
			
			positiveFactor *= currentPositiveFactor;
			negativeFactor *= currentNegativeFactor;
		}
		
		double confidence = 0.5;
		
		if(positiveFactor > negativeFactor) {
			prediction = Rating.POSITIVE;
			confidence = positiveFactor / (positiveFactor + negativeFactor);
		}
		else {
			prediction = Rating.NEGATIVE;
			confidence = negativeFactor / (positiveFactor + negativeFactor);
		}
		
		System.out.println("Making Bayes Prediction for review on line #" + review.getInputLineNumber());
		System.out.println("Prediction = " + prediction.name() + ", Confidence = " + confidence);
		
		if(Mode.CROSS_VALIDATION.equals(mode)) {
			boolean correct = prediction.equals(review.getGivenRating());
			System.out.println("Result = " + (correct ? "CORRECT" : "INCORRECT") + "!!\n");
		}
		else {
			System.out.println();
		}
		
		TfIdfResult result = new TfIdfResult();
		result.setReview(review);
		result.setConfidence(confidence);
		result.setPredictedRating(prediction);
		
		return result;
	}

}
