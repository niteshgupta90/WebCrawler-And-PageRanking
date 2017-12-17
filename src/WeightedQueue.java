
import java.util.Comparator;
import java.util.PriorityQueue;

public class WeightedQueue {
	private PriorityQueue<Element> priorityQueue;
	private boolean weighted;

	class WeightComparator implements Comparator<Element> {
		@Override
		public int compare(Element e1, Element e2) {
			return (e2.weight >= e1.weight) ? 1 : -1;
		}
	}

	public WeightedQueue(int capacity, boolean weighted) {
		priorityQueue = new PriorityQueue<>(capacity, new WeightComparator());
		this.weighted = weighted;
	}

	public void add(Element e) {
		if (!weighted)
			e.weight = 0;
		priorityQueue.offer(e);
	}

	public Element extract() {
		return priorityQueue.poll();
	}

	public boolean isEmpty() {
		return priorityQueue.isEmpty();
	}
}
