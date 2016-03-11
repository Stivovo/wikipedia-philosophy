public class CopyOfTextParser {

	private final StringBuffer buf;

	private int currentPos;
	private int openedBraces;
	private int openedCurlyBraces;
	private int startOfLink;
	private boolean isTemplateLink = false;

	private boolean seenColon = false;

	// private boolean isComment = false;

	private String link;

	public CopyOfTextParser() {
		this.buf = new StringBuffer();
		this.currentPos = 0;
		this.openedBraces = 0;
		this.openedCurlyBraces = 0;
		this.startOfLink = -1;
		this.link = null;
	}

	public void addString(final String str) {
		this.buf.append(str);
		while (this.currentPos < this.buf.length() && this.link == null) {
			switch (this.buf.charAt(this.currentPos)) {
			case '(':
				this.openedBraces++;
				break;
			case '{':
				this.openedCurlyBraces++;
				break;
			case ')':
				this.openedBraces = Math.max(0, this.openedBraces - 1);
				break;
			case '}':
				this.openedCurlyBraces = Math
						.max(0, this.openedCurlyBraces - 1);
				break;
			case '[':
				if (this.openedBraces == 0 && this.openedCurlyBraces == 0) {
					this.startOfLink = this.currentPos + 1;
				}
				break;
			case ':':
				// If we find : in a link, then we don't want the link.
				// If we're not in a link, then it doesn't matter anyway.
				this.seenColon = true;
				break;
			case '|':
			case ']':
			case '#':
				if (this.startOfLink > -1) {
					// Terminate link. We're done.
					this.link = this.buf.substring(this.startOfLink,
							this.currentPos);
					if (this.seenColon && this.link.matches("\\S:\\S")) {
						this.link = null;
					}
					this.startOfLink = -1;
					this.isTemplateLink = false;
					this.seenColon = false;
				}
				break;
			default:
				break;
			}
			this.currentPos++;
		}
	}

	public void parseThoroughly() {
		final String content = this.buf.toString();
		if (content.contains("{{Falschschreibung")) {
			this.link = content.replaceAll(
					"^\\{\\{Falschschreibung\\|([^|\\}]+).*$", "\\1");
		}
	}

	public String getLink() {
		return this.link;
	}

	public boolean isDone() {
		return this.link != null;
	}

	public String toString() {
		return "[" + (this.link == null ? "null" : this.link) + "] " + this.buf;
	}
}
