
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WikiTennisRanker {

	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		if (args.length < 3) {
			System.out.println("Incorrect number of input argumnets. Please try again");
		}
		double approximationFactor = Double.parseDouble(args[1]);
		String file = args[0];
		int k = Integer.parseInt(args[2]);
		try {
			buildGraph();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		PageRank pageRank = new PageRank(file, approximationFactor);
		String[] outDegreeArray = new String[k];
		String[] inDegreeArray = new String[k];
		String[] pageRankArray = new String[k];

		outDegreeArray = pageRank.topKOutDegree(k);
		inDegreeArray = pageRank.topKInDegree(k);
		pageRankArray = pageRank.topKPageRank(k);

		writeGraphToFile(outDegreeArray, "OutDegree");
		writeGraphToFile(inDegreeArray, "InDegree");
		writeGraphToFile(pageRankArray, "PageRank");

		System.out.println("Top K Out Degree Pages are: ");
		for (int i = 0; i < outDegreeArray.length; i++) {
			System.out.println(outDegreeArray[i]);
		}

		System.out.println("_____________________________________");

		System.out.println("Top K In Degree Pages are: ");
		for (int i = 0; i < inDegreeArray.length; i++) {
			System.out.println(inDegreeArray[i]);
		}

		System.out.println("_____________________________________");

		System.out.println("Top K Page Rank Pages are: ");
		for (int i = 0; i < pageRankArray.length; i++) {
			System.out.println(pageRankArray[i]);
		}
		
		emitJaccardSimilarity(outDegreeArray, inDegreeArray, pageRankArray);
		System.out.println("Number of iterations to for convergence : " + pageRank.getNumOfIterationsToConverge());
		System.out.println("Number of edges in the graph : " + pageRank.numEdges());
	}

	private static void writeGraphToFile(String[] arr, String outFile) throws IOException {
		FileWriter writer = new FileWriter(outFile);
		for (int i = 0; i < arr.length; i++) {
			writer.write(arr[i] + "\n");
		}
		writer.close();
	}

	private static void emitJaccardSimilarity(String[] outDegreeRank, String[] inDegreeRank, String[] pageRank) {
		int k = outDegreeRank.length;
		System.out.println("_____________________________________");

		System.out.println(
				"Jaccard Similarity Between Out & In for K :  " + k + " :" + getJaccard(outDegreeRank, inDegreeRank));
		System.out.println(
				"Jaccard Similarity Between Out & Page for K : " + k + " :" + getJaccard(outDegreeRank, pageRank));
		System.out.println(
				"Jaccard Similarity Between In & Page for K : " + k + " :" + getJaccard(inDegreeRank, pageRank));
	}

	private static double getJaccard(String[] one, String[] two) {
		Set<String> s = new HashSet<>(Arrays.asList(one));
		int common = 0;
		for (String a : two) {
			if (s.contains(a))
				common++;
		}
		return (double) (common) / (one.length * 2 - common);
	}
	
	private static void buildGraph() throws InterruptedException, IOException {
		String[] keyWords = { "cricket", "world cup" };
		WikiCrawler crawler = new WikiCrawler("/wiki/cricket", keyWords, 100, "graph.txt", Boolean.TRUE);
		crawler.crawl();
	}
}
