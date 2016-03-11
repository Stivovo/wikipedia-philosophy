import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

public class Main {

	public static final String FILENAME = "/home/sammy/tmp/wikistuff/download/shorted/parts";

	public static void main(String... args) throws FileNotFoundException,
			XMLStreamException {
		final Parser p = new Parser(FILENAME);
		p.parse();
	}
}
