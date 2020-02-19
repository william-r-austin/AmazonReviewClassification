package cs584.project1;

public class TfIdfResult {
	private Review review;
	private Rating predictedRating;
	private Double confidence;
	private String message;
	
	public Review getReview() {
		return review;
	}
	
	public void setReview(Review review) {
		this.review = review;
	}
	
	public Rating getPredictedRating() {
		return predictedRating;
	}
	
	public void setPredictedRating(Rating predictedRating) {
		this.predictedRating = predictedRating;
	}
	
	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
