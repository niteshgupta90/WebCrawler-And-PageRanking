
import java.util.List;

public class PageContext {
	private List<LinkMetaData> meta;
	private List<String> tokens;

	public PageContext(List<LinkMetaData> meta, List<String> tokens) {
		this.meta = meta;
		this.tokens = tokens;
	}

	public List<LinkMetaData> getMeta() {
		return meta;
	}

	public List<String> getTokens() {
		return tokens;
	}

}
