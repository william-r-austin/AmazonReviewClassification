package cs584.project1;

public class ReviewComparisonResult implements Comparable<ReviewComparisonResult> {

	private final Double cosineSimilarity;
	private final Review review;
	
	public ReviewComparisonResult(Review review, Double cosineSimilarity) {
		this.review = review;
		this.cosineSimilarity = cosineSimilarity;
	}
	
	@Override
	public int compareTo(ReviewComparisonResult other) {
		if(review.getInputLineNumber().equals(other.getReview().getInputLineNumber())) {
			return 0;
		}
		
		return cosineSimilarity.compareTo(other.getCosineSimilarity());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((review == null) ? 0 : review.getInputLineNumber().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReviewComparisonResult other = (ReviewComparisonResult) obj;
		if (review == null) {
			if (other.review != null) {
				return false;
			}
		} 
		else if (!review.getInputLineNumber().equals(other.review.getInputLineNumber())) {
			return false;
		}
		return true;
	}

	public Double getCosineSimilarity() {
		return cosineSimilarity;
	}

	public Review getReview() {
		return review;
	}
}
