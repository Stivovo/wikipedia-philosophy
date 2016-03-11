import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class Parser {

	private enum Tag {
		TITLE, NS, TEXT, OTHER
	}

	private final XMLEventReader r;

	private boolean isIrrelevant = false;

	private Tag currentTag = Tag.OTHER;

	private String title = null;
	private TextParser text = null;
	
	private int counter = 0;
	private int failCounter = 0;

	public Parser(final String filename) throws FileNotFoundException,
			XMLStreamException {
		final XMLInputFactory f = XMLInputFactory.newInstance();
		this.r = f.createXMLEventReader(new FileInputStream(filename));
	}

	public void parse() throws XMLStreamException {
		while (r.hasNext()) {
			XMLEvent eve = r.nextEvent();
			if (this.isIrrelevant
					&& !(eve.getEventType() == XMLStreamConstants.END_ELEMENT && eve
							.asEndElement().getName().getLocalPart()
							.equalsIgnoreCase("page"))) {
				continue;
			}
			switch (eve.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				switch (eve.asStartElement().getName().getLocalPart()) {
				case "ns":
					this.currentTag = Tag.NS;
					break;
				case "title":
					eve = r.nextEvent();
					this.title = eve.asCharacters().getData();
					break;
				case "text":
					this.currentTag = Tag.TEXT;
					this.text = new TextParser();
					break;
				default:
					break;
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				switch (eve.asEndElement().getName().getLocalPart()) {
				case "page":
					if (!this.isIrrelevant) {
						this.text.parseThoroughly();
						if (this.text.getLink() == null) {
//							System.out.println("Something's wrong.");
							this.failCounter++;
						}
//						System.out.println(this.title);
//						System.out.println(this.text.getLink());
					}
					this.isIrrelevant = false;
					this.title = null;
					this.text = null;
					this.counter++;
					if (this.counter % 10000 == 0) {
						System.out.println(this.counter + " " + this.failCounter);
					}
					break;
				case "text":
					this.currentTag = Tag.OTHER;
					break;
				case "ns":
					this.currentTag = Tag.OTHER;
					break;
				default:
					break;
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				switch (this.currentTag) {
				case TEXT:
					this.text.addString(eve.asCharacters().getData());
					if (this.text.isDone()) {
						this.currentTag = Tag.OTHER;
					}
					break;
				case NS:
					this.isIrrelevant |= eve.asCharacters().getData().charAt(0) != '0';
					this.currentTag = Tag.OTHER;
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			if (this.counter > 10000000) {
				break;
			}
		}
	}
	
	public String toString() {
		return this.text.toString();
	}
}
