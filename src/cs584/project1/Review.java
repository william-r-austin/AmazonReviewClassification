package cs584.project1;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Review {
	private Rating givenRating;
	private Rating computedRating;
	private Integer inputLineNumber;
	private String originalText;
	private List<String> tokenList;
	private Map<String, Integer> termFrequency;
	private Set<Integer> termIds;
	private Integer dataGroupId;
	private SparseVector weightVector;
	
	public Review() {
		
	}

	public Rating getGivenRating() {
		return givenRating;
	}

	public void setGivenRating(Rating givenRating) {
		this.givenRating = givenRating;
	}

	public Rating getComputedRating() {
		return computedRating;
	}

	public void setComputedRating(Rating computedRating) {
		this.computedRating = computedRating;
	}

	public Integer getInputLineNumber() {
		return inputLineNumber;
	}

	public void setInputLineNumber(Integer inputLineNumber) {
		this.inputLineNumber = inputLineNumber;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public List<String> getTokenList() {
		return tokenList;
	}

	public void setTokenList(List<String> tokenList) {
		this.tokenList = tokenList;
	}

	public Map<String, Integer> getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Map<String, Integer> termFrequency) {
		this.termFrequency = termFrequency;
	}

	public Set<Integer> getTermIds() {
		return termIds;
	}

	public void setTermIds(Set<Integer> termIds) {
		this.termIds = termIds;
	}

	public Set<String> getTerms() {
		return termFrequency.keySet();
	}

	public Integer getDataGroupId() {
		return dataGroupId;
	}

	public void setDataGroupId(Integer dataGroupId) {
		this.dataGroupId = dataGroupId;
	}

	public SparseVector getWeightVector() {
		return weightVector;
	}

	public void setWeightVector(SparseVector weightVector) {
		this.weightVector = weightVector;
	}
}
