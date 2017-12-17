
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PageRank {
	private String file = "";
	private Map<String, List<String>> adjList = new HashMap<String, List<String>>();
	private Map<String, Integer> vertexNameToIdMap = new HashMap<>();
	private Map<Integer, String> idTovertexNameIdMap = new HashMap<>();
	private int totalVertices = 0;
	private static final double DAMPING_FACTOR = 0.85;
	private double error;
	private double[] pageRank;
	private int numOfIterationsToConverge;
	private int[] in;

	public int getNumOfIterationsToConverge() {
		return numOfIterationsToConverge;
	}

	public PageRank(String file, double error) throws NumberFormatException, IOException {
		this.file = file;
		this.error = error;
		readGraph(file);
		numOfIterationsToConverge = 0;
		pageRank = computePageRank();
	}

	private void readGraph(String fileName) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Set<String> set = new HashSet<>();
		String line = "";
		int vNum = 0;
		if ((line = br.readLine()) != null) {
			totalVertices = Integer.parseInt(line);
		}
		in = new int[totalVertices];
		while ((line = br.readLine()) != null) {
			String[] arr = line.split("\\s+");
			List<String> list;
			if (adjList.containsKey(arr[0])) {
				list = adjList.get(arr[0]);
			} else {
				list = new ArrayList<String>();
			}
			list.add(arr[1]);
			adjList.put(arr[0], list);
			vNum = addIfNotPresent(arr[0], set, vNum);
			vNum = addIfNotPresent(arr[1], set, vNum);
			in[vertexNameToIdMap.get(arr[1])]++;
		}
		br.close();
	}

	private int addIfNotPresent(String vertex, Set<String> set, int v) {
		if (!set.contains(vertex)) {
			set.add(vertex);
			idTovertexNameIdMap.put(v, vertex);
			vertexNameToIdMap.put(vertex, v++);
		}

		return v;
	}

	int outDegreeOf(String vertex) {
		return adjList.get(vertex).size();
	}

	int inDegreeOf(String vertex) {
		return in[vertexNameToIdMap.get(vertex)];
	}

	int numEdges() {
		int numOfEdges = 0;
		for (String key : adjList.keySet()) {
			numOfEdges += adjList.get(key).size();
		}
		return numOfEdges;
	}

	private double[] computePageRank() {
		double[] nextRank = new double[totalVertices];

		double initPar = (double) 1 / totalVertices;
		for (int i = 0; i < totalVertices; i++) {
			nextRank[i] = initPar;
		}

		double[] rank;
		do {
			numOfIterationsToConverge++;
			rank = nextRank;
			nextRank = nextRank(rank);
		} while (!converged(rank, nextRank));

		return nextRank;
	}

	private boolean converged(double[] rank, double[] nextRank) {
		double diff = 0;
		for (int i = 0; i < totalVertices; i++) {
			diff += Math.abs(rank[i] - nextRank[i]);
		}

		return diff <= error;
	}

	private double[] nextRank(double[] currRank) {
		double[] rank = new double[totalVertices];

		double sinkAddition = (1 - DAMPING_FACTOR) / totalVertices;
		for (String u : adjList.keySet()) {
			int out = outDegreeOf(u);
			if (out > 0) {
				List<String> neighbours = adjList.get(u);
				double x = (DAMPING_FACTOR * currRank[vertexNameToIdMap.get(u)]) / out;
				for (String v : neighbours) {
					rank[vertexNameToIdMap.get(v)] += x;
				}
			} else {
				sinkAddition += (DAMPING_FACTOR / totalVertices);
			}
		}

		for (int i = 0; i < totalVertices; i++) {
			rank[i] += sinkAddition;
		}

		return rank;
	}

	public double pageRank(String vertex) {
		return pageRank[vertexNameToIdMap.get(vertex)];
	}

	public String[] topKPageRank(int k) {
		String[] ranks = new String[k];
		Set<String> taken = new HashSet<>();
		for (int i = 0; i < k; i++) {
			int max = 0;
			while (taken.contains(idTovertexNameIdMap.get(max)))
				max++;

			for (int j = max + 1; j < totalVertices; j++) {
				if (!taken.contains(idTovertexNameIdMap.get(j)) && pageRank[max] < pageRank[j]) {
					max = j;
				}
			}
			taken.add(idTovertexNameIdMap.get(max));
			ranks[i] = idTovertexNameIdMap.get(max);
		}
		return ranks;
	}

	public String[] topKInDegree(int k) {
		@SuppressWarnings("unchecked")
		List<String>[] inDegreeArrayList = new ArrayList[totalVertices];
		String[] inDegreeArray = new String[k];
		for (String key : adjList.keySet()) {
			if(inDegreeArrayList[inDegreeOf(key)]==null)
				inDegreeArrayList[inDegreeOf(key)] = new ArrayList<String>();
			inDegreeArrayList[inDegreeOf(key)].add(key);
		}
		
		int i = inDegreeArrayList.length - 1, l = 0;
		while (i >= 0 && k > 0) {
			if (inDegreeArrayList[i] != null) {
				int numOfElements = inDegreeArrayList[i].size();
				int j = 0;			
				while (j<numOfElements) {
					inDegreeArray[l++] = inDegreeArrayList[i].get(j++);
					k--;
					if (k == 0) return inDegreeArray;
				}
			}
			i--;
		}
		
		return inDegreeArray;
	}

	public String[] topKOutDegree(int k) {
		@SuppressWarnings("unchecked")
		List<String>[] outDegreeArrayList = new ArrayList[totalVertices];
		String[] outDegreeArray = new String[k];
		for (String key : adjList.keySet()) {
			if(outDegreeArrayList[outDegreeOf(key)]==null)
				outDegreeArrayList[outDegreeOf(key)] = new ArrayList<String>();
			outDegreeArrayList[outDegreeOf(key)].add(key);
		}
		
		int i = outDegreeArrayList.length - 1, l = 0;
		while (i >= 0 && k > 0) {
			if (outDegreeArrayList[i] != null) {
				int numOfElements = outDegreeArrayList[i].size();
				int j = 0;			
				while (j<numOfElements) {
					outDegreeArray[l++] = outDegreeArrayList[i].get(j++);
					k--;
					if (k == 0) return outDegreeArray;
				}
			}
			i--;
		}
		
		return outDegreeArray;
	}

}
