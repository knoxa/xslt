package xslt;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLFilter;

public class Pipeline {

	private Vector<Object> pipeline;
	private Vector<Object> list;
	private TransformerFactory factory;
	private SAXTransformerFactory saxTransFact;
	private String outputMethod = "xml";
	private String pipelineName;
	private Result result;
	
	public Pipeline() {

		factory = TransformerFactory.newInstance();
		saxTransFact = (SAXTransformerFactory) factory;
 
		list = new Vector<Object>();
	}


	public void addStep(Source xslt) throws TransformerConfigurationException {
		
		this.addStep(saxTransFact.newTemplates(xslt));
	}
	
	public void addStep(Templates template) {
		
		Properties p = template.getOutputProperties();
		outputMethod = p.getProperty(OutputKeys.METHOD);
		
		list.add(template);
		pipeline = null;
	}

	
	private void connectStep(Templates template) throws TransformerConfigurationException {
		
		TransformerHandler currentStep = saxTransFact.newTransformerHandler(template);
		
		if ( pipeline.size() > 0 ) {
			
			Object previousStep = pipeline.lastElement();
			
			if ( previousStep instanceof TransformerHandler ) {
				
				SAXResult result = new SAXResult(currentStep);
				((TransformerHandler) previousStep).setResult(result);
			}
			else if ( previousStep instanceof XMLFilter ) {
				
				((XMLFilter) previousStep).setContentHandler(currentStep);
			}
			
		}
				
		pipeline.add(currentStep);		
	}

	
	public void addStep(XMLFilter filter) {
		
		list.add(filter);
		pipeline = null;
	}
	
	
	private void connectStep(XMLFilter filter) {
		
		if ( pipeline.size() > 0 ) {

			Object previousStep = pipeline.lastElement();
			
			if ( previousStep instanceof TransformerHandler ) {
				
				SAXResult result = new SAXResult((ContentHandler) filter);
				((TransformerHandler) previousStep).setResult(result);
			}
			else if ( previousStep instanceof XMLFilter ) {
				
				((XMLFilter) previousStep).setContentHandler((ContentHandler) filter);
			}
		}
	
		pipeline.add(filter);
	}
	
	
	public void setOutput(ContentHandler output) throws TransformerConfigurationException  {
		
		result = new SAXResult(output);
	}
	
	
	private void setOutput() throws TransformerConfigurationException  {
		
		if ( pipeline.size() > 0 ) {
			
			Object lastStep = pipeline.lastElement();
			
			if ( lastStep instanceof TransformerHandler ) {
				
				((TransformerHandler) lastStep).setResult(result);
			}
			else if ( lastStep instanceof XMLFilter ) {

				// serialize the XMLFilter output
				TransformerHandler serializer = saxTransFact.newTransformerHandler();			
				serializer.setResult(result);								
				((XMLFilter) lastStep).setContentHandler(serializer);
			}
		}
	}
	

	public void setOutput(OutputStream outputStream) throws TransformerConfigurationException  {
		
		result = new StreamResult(outputStream);
	}
	

	public void setOutput(DOMResult outputDOM) throws TransformerConfigurationException  {
		
		result = outputDOM;
	}
	
	
	public ContentHandler getContentHandler() throws TransformerConfigurationException {
		
		// The pipeline ContentHandler is the first link in the chain. 
		
		if (pipeline == null )  chain();
		Object first = pipeline.firstElement();
		
		if ( first instanceof TransformerHandler || first instanceof XMLFilter ) {

			return (ContentHandler) first;			
		}	
		else return null;
	}
	
	
	private void chain() throws TransformerConfigurationException {
		
		pipeline = new Vector<Object>();
		
		Enumeration<Object> steps = list.elements();
		
		while ( steps.hasMoreElements() ) {
			
			Object step = steps.nextElement();
			
			if      ( step instanceof Templates )  connectStep((Templates) step);
			else if ( step instanceof XMLFilter )  connectStep((XMLFilter) step);
			else {
				System.err.println("unknown step: " + step);
			}
		}
		
		setOutput();
	}
	
	public String getOutputMethod() {
		
		return outputMethod;
	}


	public void setName(String name) {
		
		pipelineName = name;
	}
	
	
	public String getName() {
		
		if ( pipelineName != null )  return pipelineName;
		else                         return Integer.toString(this.hashCode());
	}

	public TransformerFactory getTransformerFactory() {
		
		return saxTransFact;
	}

	
	public void transform(Source source) throws TransformerException {
		
		if (pipeline == null )  chain();

		Transformer transformer = this.getTransformerFactory().newTransformer();
		Result pipelineInput;
		
		Object firstStep = ( pipeline.size() > 0 ) ? pipeline.elementAt(0) : null;
		
		if ( firstStep != null ) {
			
			pipelineInput = new SAXResult((ContentHandler) firstStep);
			transformer.transform(source, pipelineInput);
		}
		else {
			System.err.printf("The pipeline [%s] is empty.\n", getName());
			// Could just serialize input to output here ...
		}
	}
}
