
import java.io.IOException;

public class Test {
	public static void main(String[] args) throws InterruptedException, IOException {
		String[] keyWords = { "tennis", "grand slam"};
		WikiCrawler crawler = new WikiCrawler("/wiki/Tennis", keyWords, 10, "graph.txt", Boolean.TRUE);
		crawler.crawl();
	}
}
