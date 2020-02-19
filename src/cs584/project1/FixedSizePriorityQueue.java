package cs584.project1;

import java.util.Comparator;
import java.util.TreeSet;

public class FixedSizePriorityQueue<T extends Comparable<T>> {
	private final int capacity;
	private final TreeSet<T> mySet; // = new TreeSet<>();
	private final PriorityQueueType priorityQueueType;
	
	public FixedSizePriorityQueue(final PriorityQueueType type, final int capacityArg) {
		this.priorityQueueType = type;
		Comparator<T> comparator = (PriorityQueueType.MIN.equals(type) ? Comparator.naturalOrder() : Comparator.reverseOrder());
		mySet = new TreeSet<>(comparator);
		capacity = capacityArg;
		if(capacityArg < 1) {
			throw new RuntimeException("Capacity must be > 0."); 
		}
	}
	
	public void add(final T t) {
		if(mySet.size() < capacity) {
			mySet.add(t);
		}
		else if(mySet.size() == capacity) {
			if(PriorityQueueType.MIN.equals(priorityQueueType)) {
				// Min Priority Queue
				if(mySet.last().compareTo(t) > 0) {
					mySet.pollLast();
					mySet.add(t);
				}
			}
			else {
				// Max Priority Queue
				if(mySet.last().compareTo(t) < 0) {
					mySet.pollLast();
					mySet.add(t);
				}
			}
		
		}
		else {
			throw new RuntimeException("Invalid priority queue size!! Size = " + mySet.size() + ", but capacity = " + capacity);
		}
	}
	
	public void printSet() {
		System.out.println("Set = " + mySet);
	}
	
	public TreeSet<T> getBackingTreeSet() {
		return mySet;
	}

}
