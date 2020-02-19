package cs584.project1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrequencyModel {
	private Map<String, Integer> tokenDocumentFrequency = new HashMap<>();
	private Map<String, Integer> termToIndexMap = new HashMap<>();
	private List<String> termList = new ArrayList<>();
	private Integer totalDocumentCount;
	private Integer totalPositiveDocuments;
	private Integer totalNegativeDocuments;
	
	public void construct(List<Review> trainingSet) {
		populateDocumentCounts(trainingSet);
		populateDocumentFrequency(trainingSet);
		saveTermIdMapping();
		computeVectors(trainingSet);
		computeAllTermIds(trainingSet);
	}
	
	private void populateDocumentCounts(List<Review> trainingSet) {
		totalDocumentCount = trainingSet.size();
		totalPositiveDocuments = 0;
		totalNegativeDocuments = 0;
		
		for(Review review : trainingSet) {
			if(Rating.POSITIVE.equals(review.getGivenRating())) {
				totalPositiveDocuments++;
			}
			else if(Rating.NEGATIVE.equals(review.getGivenRating())) {
				totalNegativeDocuments++;
			}
		}
	}
		
	private void populateDocumentFrequency(List<Review> trainingSet) {
		for(Review currentReview : trainingSet) {
			Map<String, Integer> termFrequency = currentReview.getTermFrequency();
			
			for(String uniqueToken : termFrequency.keySet()) {
				Utilities.incrementFrequencyMap(tokenDocumentFrequency, uniqueToken);
			}
		}
	}
	
	private void computeAllTermIds(List<Review> reviews) {
		for(Review review : reviews) {
			Set<Integer> termIds = computeTermIds(review);
			review.setTermIds(termIds);
		}
	}
	
	private Set<Integer> computeTermIds(Review review) {
		Set<Integer> termIdSet = new HashSet<>();
		
		for(String term : review.getTerms()) {
			Integer termIndex = termToIndexMap.get(term);
			if(termIndex != null) {
				termIdSet.add(termIndex);
			}
		}
		return termIdSet;
	}
	
	private void saveTermIdMapping() {
		termList = new ArrayList<>(tokenDocumentFrequency.keySet());
		
		int index = 0;
		for(String term : termList) {
			termToIndexMap.put(term, index);
			index++;
		}
	}
	
	private void computeVectors(List<Review> reviews) {
		for(Review review : reviews) {
			SparseVector weightVector = computeWeightVector(review);
			review.setWeightVector(weightVector);
		}
	}
	
	public Double getPositiveWeight() {
		Double weight = (totalDocumentCount / 2.0) / totalPositiveDocuments;
		return weight;
	}
	
	public Double getNegativeWeight() {
		Double weight = (totalDocumentCount / 2.0) / totalNegativeDocuments;
		return weight;
	}
	
	public Integer getGlobalTermFrequency(String term) {
		return Utilities.getTermFrequency(tokenDocumentFrequency, term);
	}
	
	public SparseVector computeWeightVector(Review review) {
		SparseVector weightVector = new SparseVector(termList.size());
		Map<String, Integer> termFrequency = review.getTermFrequency();
		
		for(Map.Entry<String, Integer> termData : termFrequency.entrySet()) {
			String term = termData.getKey();
			Integer count = termData.getValue();
			Integer termIndex = termToIndexMap.get(term);
			
			if(termIndex != null) {
				Integer documentsContainingTerm = Utilities.getTermFrequency(tokenDocumentFrequency, term);
				
				double tfValue = count.doubleValue();
				double idfValue = Math.log(totalDocumentCount.doubleValue() / documentsContainingTerm.doubleValue());
				
				weightVector.setValue(termIndex, tfValue * idfValue);
			}
		}
		
		weightVector.normalize();
		return weightVector;
	}
}
