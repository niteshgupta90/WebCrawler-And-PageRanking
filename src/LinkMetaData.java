
public class LinkMetaData {
	String link;
	int posLink;
	String anchorText;

	public LinkMetaData(String link, String anchor, int pos) {
		this.posLink = pos;
		this.link = link;
		this.anchorText = anchor;
	}

	@Override
	public String toString() {
		return "LinkMetaData [link=" + link + ", posLink=" + posLink + ", anchorText=" + anchorText + "]";
	}

}
