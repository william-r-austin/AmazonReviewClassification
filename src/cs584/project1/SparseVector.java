package cs584.project1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SparseVector {
	private Integer length;
	private Map<Integer, Double> valuesMap = new HashMap<Integer, Double>();
	
	public SparseVector(Integer length) {
		this.length = length;
	}
	
	public Double getValue(Integer index) {
		checkBounds(index);
		
		Double value = valuesMap.get(index);
		if(value == null) {
			value = 0.0;
		}
		
		return value;
	}
	
	public void setValue(Integer index, Double newValue) {
		checkBounds(index);
		valuesMap.put(index, newValue);
	}
	
	private void checkBounds(Integer index) {
		if(index < 0 || index >= length) {
			throw new RuntimeException("Index out of bounds for SparseVector. Requested index was: " + index + ", but length is: " + length);
		}
	}
	
	public void normalize() {
		Double magnitude = getMagnitude();
		
		for(Map.Entry<Integer, Double> entry : valuesMap.entrySet()) {
			Double currentValue = entry.getValue();
			Double newValue = currentValue / magnitude;
			entry.setValue(newValue);
		}
	}
	
	public Set<Map.Entry<Integer, Double>> getEntrySet() {
		return valuesMap.entrySet();
	}
	
	public int getSize() {
		return valuesMap.size();
	}
	
	public Set<Integer> getNonZeroIndices() {
		return valuesMap.keySet();
	}
	
	public Double getMagnitude() {
		Double squaredLength = 0.0;
		
		for(Double value : valuesMap.values()) {
			squaredLength += (value * value);
		}
		
		return Math.sqrt(squaredLength);
		
	}
}
