package xslt;

import org.xml.sax.XMLFilter;

public class SimpleFilterFactory implements FilterFactory {

	@Override
	public XMLFilter getFilter(String classname) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

		return (XMLFilter) Class.forName(classname).newInstance();
	}

}
