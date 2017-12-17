
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class WikiCrawler {
	private static final String BASE_URL = "https://en.wikipedia.org";
	private static int SLEEP_TIME_MS = 1000;
	private String seedUrl;
	private String[] keyWords;
	private int maxNodes;
	private String outFile;
	private boolean weighted;
	Set<String> disallowedLinks;

	public WikiCrawler(String seedUrl, String[] keyWords, int max, String outFile, boolean isWeighted) {
		this.seedUrl = seedUrl;
		this.maxNodes = max;
		this.keyWords = keyWords;
		this.outFile = outFile;
		this.weighted = isWeighted;
		this.disallowedLinks = getDisallowedLinks("robots.txt");
	}

	/**
	 * 1. fetch the html page. Extract all the the
	 * <p>
	 * tags for each
	 * <p>
	 * tag : fetch the links and the words compute distance for each link and
	 * add it to Weighted Queue. Run BFS
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void crawl() throws InterruptedException, IOException {
		WeightedQueue weightedQueue = new WeightedQueue(maxNodes * 10, weighted);
		weightedQueue.add(new Element(seedUrl, 1));
		int processed = 0;
		Set<String> visited = new HashSet<>();
		Map<String, List<String>> adjList = new HashMap<>();
		while (!weightedQueue.isEmpty() && processed < maxNodes) {
			Element e = weightedQueue.extract();
			if (!visited.contains(e.key) && !disallowedLinks.contains(e.key)) {
				adjList.put(e.key, new ArrayList<>());
				Map<String, Integer> linkMap = extractLinksInOnePage(e.key);

				// Remove self loop
				Set<String> keys = linkMap.keySet();
				keys.remove(e.key);

				for (String link : keys) {
					if (!visited.contains(link)) {
						adjList.get(e.key).add(link);
						double weight = computeWeight(linkMap.get(link));
						weightedQueue.add(new Element(link, weight));
					}
				}
				visited.add(e.key);
				processed++;
				if (processed % 10 == 0) {
					Thread.sleep(SLEEP_TIME_MS);
				}
			}
		}

		writeGraphToFile(adjList, visited);
	}

	private boolean isPresent(Map<String, List<String>> adjList, String link, String key) {
		return adjList.containsKey(link) && adjList.get(link).contains(key);
	}

	private void writeGraphToFile(Map<String, List<String>> graph, Set<String> vertices) throws IOException {
		FileWriter writer = new FileWriter(outFile);
		writer.write(vertices.size() + "\n");
		for (String oneVertex : graph.keySet()) {
			List<String> verticesList = graph.get(oneVertex);
			for (String vert : verticesList) {
				if (vertices.contains(vert))
					writer.write(oneVertex + "\t" + vert + "\n");
			}
		}
		writer.close();
	}

	private double computeWeight(Integer d) {
		double weight = 0;
		if (d == 0)
			weight = 1;
		else if (d > 20)
			weight = 0;
		else {
			weight = (double) 1 / (d + 2);
		}

		return weight;
	}

	private Map<String, Integer> extractLinksInOnePage(String url) {
		String content = extractContent(readContents(url));
		PageContext context = PreprocessUtils.extractPageContext(content);
		Map<String, List<Integer>> keyWordPositions = PreprocessUtils.extractKeywordPositions(context, keyWords);
		return PreprocessUtils.computeLinkDistances(context, keyWordPositions);
	}

	private String extractContent(String content) {
		int idx = content.indexOf("<p>");

		if (idx >= 0) {
			content = content.substring(idx + 3);
			content = content.replace("<p>", "");
		}

		return content.replace("</p>", "");
	}

	private String readContents(String oneUrl) {
		StringBuilder sb = new StringBuilder();
		URL url;
		InputStream is = null;
		BufferedReader br;
		String line;

		try {
			url = new URL(BASE_URL + oneUrl);
			is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (Exception ex) {
			// DO Nothing
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}

		return sb.toString();
	}

	private String readFile(String file) {
		StringBuilder sb = new StringBuilder();
		InputStream is = null;
		BufferedReader br;
		String line;

		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (Exception ex) {
			// DO Nothing
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}

		return sb.toString();
	}

	private Set<String> getDisallowedLinks(String file) {
		Set<String> disallowedLinks = new HashSet<String>();
		String text = readFile(file);
		StringTokenizer st = new StringTokenizer(text);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			try {
				if (token.equalsIgnoreCase("Disallow:")) {
					disallowedLinks.add(st.nextToken().toLowerCase());
				}
			} catch (Exception e) {
			}
		}
		return disallowedLinks;
	}

}
