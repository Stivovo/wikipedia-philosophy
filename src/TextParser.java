import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

	private static final Pattern DEFAULT = Pattern
			.compile("^(([^:\\[]|: )+?)([|#].+?)?$");
	private static final int DEFAULT_GROUP = 1;
	private static final Pattern FALSCHSCHR = Pattern
			.compile("\\{\\{Falschschreibung\\|(.+?)(\\|.*?)?\\}\\}");
	private static final int FALSCHSCHR_GROUP = 1;
	private static final Pattern HAUPTART = Pattern
			.compile("\\{\\{Hauptartikel\\|(.+?)(\\|.*?)?\\}\\}");
	private static final int HAUPTART_GROUP = 1;
	private static final Pattern TEMPLATECONT = Pattern
			.compile("\\{\\{Dieser Artikel(\\|)");
	private static final int TEMPLATECONT_GROUP = 1;

	private final StringBuffer buf;

	private String link;

	public TextParser() {
		this.buf = new StringBuffer();
		this.link = null;
	}

	public void addString(final String str) {
		this.buf.append(str);
	}

	public void parseThoroughly() {
		final String content = this.buf.toString();
		this.link = this.parseComplicated(content);
	}

	private String parseComplicated(final String content) {
		String modContent = content;
		String result = this.parseBraces(modContent);
		if (result != null)
			return result;

		// Remove comments if existent:
		modContent = modContent.replaceAll("<!--[\\S\\s]*?-->", "");
		result = this.parseBraces(modContent);
		if (result != null)
			return result;

		Matcher m;
		// Match for "Dieser Artikel|" and continue matchin in the rest
		m = TEMPLATECONT.matcher(modContent);
		if (m.find()) {
			final String newContent = modContent.substring(m
					.end(TEMPLATECONT_GROUP));
			result = this.parseComplicated(newContent);
		}
		if (result != null)
			return result;

		// Match for "Falschschreibung"
		m = FALSCHSCHR.matcher(modContent);
		if (m.find()) {
			result = this.cleanLink(m.group(FALSCHSCHR_GROUP));
		}
		if (result != null)
			return result;

		// Match for "Hauptartikel"
		m = HAUPTART.matcher(modContent);
		if (m.find()) {
			result = this.cleanLink(m.group(HAUPTART_GROUP));
		}
		if (result != null)
			return result;

		return result;
	}

	private String parseBraces(final String content) {
		int braces = 0;
		int curlyBraces = 0;
		int brackets = 0;
		boolean currentCurlyOpen = false;
		boolean currentCurlyClose = false;
		boolean currentBracketsOpen = false;
		boolean currentBracketsClose = false;
		int currentPos = -1;
		int linkStart = -1;

		// To take into account that braces are sometimes not properly closed.
		int curlyBraceBraceReset = 0;

		while (currentPos < content.length() - 1) {
			currentPos++;
			switch (content.charAt(currentPos)) {
			case '(':
				braces++;
				break;
			case ')':
				braces = Math.max(0, braces - 1);
				break;
			case '{':
				// if (currentCurlyOpen) {
				if (curlyBraces == 0) {
					curlyBraceBraceReset = braces;
				}
				curlyBraces++;
				// }
				currentCurlyOpen = !currentCurlyOpen;
				currentCurlyClose = false;
				currentBracketsOpen = false;
				currentBracketsClose = false;
				break;
			case '}':
				// if (currentCurlyClose) {
				curlyBraces = Math.max(0, curlyBraces - 1);
				if (curlyBraces == 0) {
					braces = curlyBraceBraceReset;
					curlyBraceBraceReset = 0;
				}
				// }
				currentCurlyOpen = false;
				currentCurlyClose = !currentCurlyClose;
				currentBracketsOpen = false;
				currentBracketsClose = false;
				break;
			case '[':
				if (currentBracketsOpen) {
					brackets++;
					if (braces == 0 && curlyBraces == 0 && brackets == 1) {
						linkStart = currentPos + 1;
					}
				}
				currentCurlyOpen = false;
				currentCurlyClose = false;
				currentBracketsOpen = !currentBracketsOpen;
				currentBracketsClose = false;
				break;
			case ']':
				if (currentBracketsClose) {
					brackets--;
					if (linkStart > -1) {
						final String link = this.cleanLink(content.substring(
								linkStart, currentPos - 1));
						if (link == null) {
							// Continue with search
							linkStart = -1;
						} else {
							// Return result
							return link;
						}
					}
				}
				currentCurlyOpen = false;
				currentCurlyClose = false;
				currentBracketsOpen = false;
				currentBracketsClose = !currentBracketsClose;
				break;
			default:
				break;
			}
		}
		return null;
	}

	private String cleanLink(final String link) {
		Matcher m = DEFAULT.matcher(link);
		if (m.find()) {
			return m.group(DEFAULT_GROUP).replace('_', ' ');
		} else {
			return null;
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
