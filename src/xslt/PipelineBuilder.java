package xslt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;


public class PipelineBuilder implements ContentHandler {

	private Pipeline pipeline;
	private URL context;
	private FilterFactory filterFactory;
	
	public PipelineBuilder() {

		filterFactory = new SimpleFilterFactory(); //default
	}

	public URL getContext() {
		return context;
	}

	public void setContext(URL context) {
		this.context = context;
	}

	public Pipeline getPipeline(String urlString) {

		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			SAXTransformerFactory saxTransFact = (SAXTransformerFactory) factory;

			TransformerHandler identity;
			identity = saxTransFact.newTransformerHandler();
			
			URL url = new URL(urlString);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("GET");

	        InputStream in = connection.getInputStream();
		    Source source = new StreamSource(in);

		    this.setContext(url);
		    
			Result output = new SAXResult(this);
			identity.getTransformer().transform(source, output);
		}
		catch (TransformerException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (ProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}	

		return pipeline;
	}

	public Pipeline getPipeline(File pipelineFile) {
		
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			SAXTransformerFactory saxTransFact = (SAXTransformerFactory) factory;

			TransformerHandler identity;
			identity = saxTransFact.newTransformerHandler();
			
	        InputStream in = new FileInputStream(pipelineFile);
		    Source source = new StreamSource(in);
		    this.setContext(pipelineFile.toURI().toURL());
		    
			Result output = new SAXResult(this);
			identity.getTransformer().transform(source, output);
		}
		catch (TransformerException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}	

		return pipeline;
	}

	public Pipeline getPipeline() {
		
		return pipeline;
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
	}

	public void endPrefixMapping(String arg0) throws SAXException {
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
	}

	public void processingInstruction(String arg0, String arg1) throws SAXException {
	}

	public void setDocumentLocator(Locator arg0) {
	}

	public void skippedEntity(String arg0) throws SAXException {
	}

	public void startDocument() throws SAXException {
		
		pipeline = new Pipeline();
	}

	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
		
		if ( localName.equals("step") ) {
			
			String url    = attr.getValue("url");
			String filter = attr.getValue("filter");
			
			if ( url != null ) {
				
				try {
					addStylesheet(url);
				}
				catch (TransformerConfigurationException e) {
					e.printStackTrace();
				}
			}
			else {
				
				try {
					
					XMLFilter f = (XMLFilter) filterFactory.getFilter(filter);
					pipeline.addStep(f);
				}
				catch (IllegalAccessException e) {

					e.printStackTrace();
				}
				catch (InstantiationException e) {

					e.printStackTrace();
				}
				catch (ClassNotFoundException e) {

					e.printStackTrace();
				}
			}
		}
		else if ( localName.equals("pipeline") ) {
			
			String pipelineName = attr.getValue("name");
			pipeline.setName(pipelineName);
		}
	}

	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
	}
	
	
	private void addStylesheet(String urlString) throws TransformerConfigurationException {
		
		try {
			URL url = new URL(context, urlString);
			
			Source source = new StreamSource(url.openStream());
			source.setSystemId(url.toString());
			pipeline.addStep(source);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void setFilterFactory(FilterFactory factory) {

		this.filterFactory = factory;
	}
}
