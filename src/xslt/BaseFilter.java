package xslt;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class BaseFilter extends XMLFilterImpl {
	
	private StringBuffer text;
	private Stack<String> elementNameStack;

	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	public BaseFilter() {
		
		super();
		init();
	}

	public BaseFilter(XMLReader parent) {

		super(parent);
	}

	private void init() {
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qname) throws SAXException {
		
		elementNameStack.pop();
		super.endElement(uri, localName, qname);
	}

	@Override
	public void startDocument() throws SAXException {

		text = new StringBuffer();
		elementNameStack = new Stack<String>();

		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qname, Attributes attr) throws SAXException {

		text.setLength(0);
		elementNameStack.push(qname);
		super.startElement(uri, localName, qname, attr);
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		text.append(ch, start, length);
		super.characters(ch, start, length);
	}
	
	
	public String parentElementName() {
		
		String parent = elementNameStack.isEmpty() ? "<root>" : elementNameStack.peek();
		return parent;
	}
	
	
	public String getText() {
		
		return text.toString();
	}
}
