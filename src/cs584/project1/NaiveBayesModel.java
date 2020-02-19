package cs584.project1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NaiveBayesModel {
	private Integer totalPositiveLines;
	private Integer totalNegativeLines;
	private Map<String, Integer> positiveFrequencyCount;
	private Map<String, Integer> negativeFrequencyCount;
	private Integer modelId;
	
	public NaiveBayesModel(Integer modelIdInput) {
		modelId = modelIdInput;
		totalPositiveLines = 0;
		totalNegativeLines = 0;
		positiveFrequencyCount = new HashMap<>();
		negativeFrequencyCount = new HashMap<>();
	}

	public Integer getTotalPositiveLines() {
		return totalPositiveLines;
	}

	public void incrementTotalPositiveLines() {
		totalPositiveLines++;
	}

	public Integer getTotalNegativeLines() {
		return totalNegativeLines;
	}

	public void incrementTotalNegativeLines() {
		totalNegativeLines++;
	}
	
	public Integer getTotalLines() {
		return totalPositiveLines + totalNegativeLines;
	}
	
	public Set<String> getAllTerms() {
		Set<String> allTerms = new HashSet<>();
		allTerms.addAll(positiveFrequencyCount.keySet());
		allTerms.addAll(negativeFrequencyCount.keySet());
		return allTerms;
	}

	public Integer getPositiveFrequencyCount(String term) {
		return Utilities.getTermFrequency(positiveFrequencyCount, term);
	}

	public void incrementPositiveFrequencyCount(String term) {
		Utilities.incrementFrequencyMap(positiveFrequencyCount, term);
	}

	public Integer getNegativeFrequencyCount(String term) {
		return Utilities.getTermFrequency(negativeFrequencyCount, term);
	}

	public void incrementNegativeFrequencyCount(String term) {
		Utilities.incrementFrequencyMap(negativeFrequencyCount, term);
	}

	public Integer getModelId() {
		return modelId;
	}
}
