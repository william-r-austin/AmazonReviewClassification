package cs584.project1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utilities {
	
	public static void incrementFrequencyMap(Map<String, Integer> map, String term) {
		Integer result = map.get(term);
		if(result != null) {
			map.put(term, result + 1);
		}
		else {
			map.put(term, 1);
		}
	}
	
	public static void incrementFrequencyMap(Map<String, Integer> map, String term, Integer amount) {
		Integer result = map.get(term);
		if(result != null) {
			map.put(term, result + amount);
		}
		else {
			map.put(term, amount);
		}
	}
	
	public static Integer getTermFrequency(Map<String, Integer> map, String term) {
		Integer result = map.get(term);
		if(result == null) {
			result = 0;
		}
		
		return result;
	}
	
	public static Double cosineSimilarity(SparseVector v1, SparseVector v2) {
		SparseVector smaller = null;
		SparseVector larger = null;
		
		if(v1.getSize() <= v2.getSize()) {
			smaller = v1;
			larger = v2;
		}
		else {
			smaller = v2;
			larger = v1;
		}
		
		Double dotProduct = 0.0;
		
		for(Map.Entry<Integer, Double> entry : smaller.getEntrySet()) {
			Integer index = entry.getKey();
			Double valueOther = larger.getValue(index);
			
			if(valueOther != null) {
				Double currentFactor = entry.getValue() * valueOther;
				dotProduct += currentFactor;
			}
		}
		
		return dotProduct; // / magnitudeFactor;
	}
	
	public static List<Set<Integer>> sortSets(Set<Integer> s1, Set<Integer> s2) {
		List<Set<Integer>> sorted = new ArrayList<>();
		sorted.add(s1);
		sorted.add(s2);
		
		sorted.sort(new Comparator<Set<Integer>>() {

			@Override
			public int compare(Set<Integer> arg0, Set<Integer> arg1) {
				Integer size1 = arg0.size();
				Integer size2 = arg1.size();
				
				return size1.compareTo(size2);
			}
		});
		
		return sorted;
	}
	
	public static Integer intersectionSize(Set<Integer> s1, Set<Integer> s2) {
		List<Set<Integer>> sortedSets = sortSets(s1, s2);
		
		Set<Integer> smaller = sortedSets.get(0);
		Set<Integer> larger = sortedSets.get(1);
		
		Set<Integer> common = new HashSet<>(smaller);
		common.retainAll(larger);
		
		return common.size();
	}
	
	public static Integer unionSize(Set<Integer> s1, Set<Integer> s2) {
		Set<Integer> common = new HashSet<>(s1);
		common.addAll(s2);
		
		return common.size();
	}
	
	public static Double dicesCoefficient(Set<Integer> s1, Set<Integer> s2) {
		Double numerator = 2.0 * intersectionSize(s1, s2);
		Double denominator = 1.0 * (s1.size() + s2.size());
		
		return numerator / denominator;
	}
	
	public static Double jaccardsCoefficient(Set<Integer> s1, Set<Integer> s2) {
		Double numerator = 1.0 * intersectionSize(s1, s2);
		Double denominator = 1.0 * unionSize(s1, s2);
		
		return numerator / denominator;
	}
	
	public static Double cosineCoefficient(Set<Integer> s1, Set<Integer> s2) {
		Double numerator = 1.0 * intersectionSize(s1, s2);
		Double denominator = Math.sqrt(1.0 * s1.size()) * Math.sqrt(1.0 * s2.size());
		
		return numerator / denominator;
	}
	
	public static Double overlapCoefficient(Set<Integer> s1, Set<Integer> s2) {
		Double numerator = 1.0 * intersectionSize(s1, s2);
		Double denominator = 1.0 * Math.min(s1.size(), s2.size());
		
		return numerator / denominator;
	}
}
