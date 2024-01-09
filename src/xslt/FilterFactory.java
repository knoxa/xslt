package xslt;

import org.xml.sax.XMLFilter;

public interface FilterFactory {

	XMLFilter getFilter(String classname) throws IllegalAccessException, InstantiationException, ClassNotFoundException;
}
