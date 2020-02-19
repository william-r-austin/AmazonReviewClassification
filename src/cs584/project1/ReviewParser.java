package cs584.project1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class ReviewParser {
	private List<Review> testSetReviews;
	private List<Review> trainingSetReviews;
	private Set<String> trainingSetTerms;
		
	public void parseReviewsForTestSet() {
		testSetReviews = readReviewFile(Constants.TEST_FILE, false, -1);
	}
	
	public List<Review> getReviewsForTestSet() {
		return testSetReviews;
	}
	
	public void parseReviewsForTrainingSet() {
		trainingSetTerms = new HashSet<>();
		trainingSetReviews = readReviewFile(Constants.TRAINING_FILE, true, -2);
		
		// Compute the distribution and term set
		Map<Rating, Integer> trainingSetDist = new EnumMap<>(Rating.class);
		for(Review review : trainingSetReviews) {
			trainingSetTerms.addAll(review.getTokenList());
			
			Rating rating = review.getGivenRating();
			Integer count = trainingSetDist.get(rating);
			
			if(count == null) {
				count = 1;
			}
			else {
				count++;
			}
			
			trainingSetDist.put(rating, count);
		}
	}
	
	public Set<String> getTermsForTrainingSet() {
		return trainingSetTerms;
	}
	
	public List<Review> getReviewsForTrainingSet() {
		return trainingSetReviews;
	}
	
	private List<Review> readReviewFile(String filename, boolean isTagged, int defaultDataGroupId) {
		List<Review> reviews = new ArrayList<>();
		
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			
			String inputLine = br.readLine();
			int lineNumber = 1;
			
			while(inputLine != null) {
				
				Rating givenRating = Rating.NONE;
				
				if(isTagged) {
					String stringRating = inputLine.substring(0, 2);
					if("+1".equals(stringRating)) {
						givenRating = Rating.POSITIVE;
					}
					else if("-1".equals(stringRating)) {
						givenRating = Rating.NEGATIVE;
					}
					
					inputLine = inputLine.substring(3);
				}
				
				Review currentReview = new Review();
				currentReview.setInputLineNumber(lineNumber);
				currentReview.setGivenRating(givenRating);
				currentReview.setOriginalText(inputLine);
				currentReview.setDataGroupId(defaultDataGroupId);
				
				List<String> tokens = tokenize(inputLine);
				currentReview.setTokenList(tokens);
				
				reviews.add(currentReview);
				
				inputLine = br.readLine();
				lineNumber++;
			}
			
			br.close();
			fr.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return reviews;
	}
	
	
	private List<String> tokenize(String input) {
		List<String> words = new ArrayList<>();
		
		String[] initialTokens = input.replaceAll("[^a-zA-Z' ]", " ").toLowerCase().trim().split("\\s+");
		
		for(String token : initialTokens) {
			StemmingType stemmingType = Constants.STEMMING_TYPE;
			
			if(StemmingType.PORTER.equals(stemmingType)) {
				PorterStemmer porterStemmer = new PorterStemmer();
				String stemmedWord = porterStemmer.stem(token);
				words.add(stemmedWord);
			}
			else if(StemmingType.SNOWBALL.equals(stemmingType)) {
				SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
				String stemmedWord = snowballStemmer.stem(token).toString();
				words.add(stemmedWord);
			}
			else if(StemmingType.NONE.equals(stemmingType)) {
				words.add(token); // No stemming
			}
		}
		
		return words;
	}
	

}
