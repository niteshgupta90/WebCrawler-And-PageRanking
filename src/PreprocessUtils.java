
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class PreprocessUtils {
	private static final String LINK = "LINK";

	public static PageContext extractPageContext(String para) {
		para = para.replaceAll(">", "> ");
		para = para.replaceAll("<", " <");
		para = para.replaceAll("=", " = ");
		para = para.replaceAll("\"", "");

		StringTokenizer st = new StringTokenizer(para);
		List<LinkMetaData> meta = new ArrayList<>();

		List<String> listOfTokens = new ArrayList<String>();
		int pos = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			try {
				if (token.equalsIgnoreCase("<A")) {
					String hyperlink;
					st.nextToken();
					st.nextToken();

					hyperlink = st.nextToken();
					token = st.nextToken();
					while (!token.endsWith(">")) {
						token = st.nextToken();
					}

					String hypertext = "";
					do {
						token = st.nextToken();
						if (!token.equalsIgnoreCase("</A>"))
							hypertext += " " + token;
					} while (!token.equalsIgnoreCase("</A>"));
					if (hyperlink.indexOf("#") == -1 && hyperlink.indexOf(":") == -1) {
						meta.add(new LinkMetaData(hyperlink, hypertext.trim().toLowerCase(), pos));
					}
					listOfTokens.add(LINK);
				} else {
					listOfTokens.add(token);
				}
				pos++;

			} catch (Exception e) {
			}
		}

		return new PageContext(meta, listOfTokens);
	}

	public static Map<String, List<Integer>> extractKeywordPositions(PageContext context, String[] keys) {
		Map<String, List<Integer>> m = new HashMap<>();

		List<String> tokens = context.getTokens();
		for (int i = 0; i < tokens.size(); i++) {
			for (String key : keys) {
				String[] parts = key.split("\\s+");
				int k = i;
				boolean matched = true;
				for (String oneKey : parts) {
					if (k < tokens.size() && oneKey.equalsIgnoreCase(tokens.get(k))) {
						k++;
					} else {
						matched = false;
						break;
					}
				}
				if (matched) {
					List<Integer> positions = m.getOrDefault(key, new ArrayList<>());
					positions.add(i);
					m.put(key, positions);
				}
			}
		}
		return m;
	}

	/**
	 * 
	 * @param context
	 * @param keyWordPositions
	 * @return
	 */
	public static Map<String, Integer> computeLinkDistances(PageContext context,
			Map<String, List<Integer>> keyWordPositions) {
		Map<String, Integer> m = new HashMap<>();
		for (LinkMetaData linkInfo : context.getMeta()) {
			String url = linkInfo.link;
			String anchorText = linkInfo.anchorText;
			int dist = Integer.MAX_VALUE;
			for (String keyWord : keyWordPositions.keySet()) {
				if (anchorText.indexOf(keyWord) != -1 || containsInLink(url, keyWord)) {
					dist = 0;
					break;
				}
			}

			if (dist == Integer.MAX_VALUE) {
				int linkPos = linkInfo.posLink;
				for (String keyWord : keyWordPositions.keySet()) {
					List<Integer> positionMap = keyWordPositions.get(keyWord);
					for (Integer onePos : positionMap) {
						dist = Math.min(dist, Math.abs(onePos - linkPos));
					}
				}
			}

			m.put(url, dist);
		}

		return m;
	}

	private static boolean containsInLink(String link, String keyword) {
		keyword = keyword.replaceAll(" ", "_");
		return link.indexOf(keyword) != -1;
	}
}
