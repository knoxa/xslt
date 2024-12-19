package test.xslt;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import xslt.BaseFilter;

public class TestFilter extends BaseFilter {


	@Override
	public void endElement(String uri, String localName, String qname) throws SAXException {

		if ( qname.equals("data") ) {

			super.startElement(uri, "ADDED", "ADDED", new AttributesImpl());
			super.endElement(uri, "ADDED", "ADDED");
		}

		super.endElement(uri, localName, qname);
	}
}
