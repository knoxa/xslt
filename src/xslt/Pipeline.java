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
	private ContentHandler outputContentHandler = null;
	private String outputMethod = "xml";
	private String pipelineName;
	
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
		outputMethod = p.getProperty("method");
		
		list.add(template);	
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
		
		reset();
		
		outputContentHandler = output;
//		setOutput();
	}
	
	
	private void setOutput() throws TransformerConfigurationException  {
		
		if (outputContentHandler == null)  return;
		
		if ( pipeline.size() > 0 ) {
			
			Object previousStep = pipeline.lastElement();
			
			if ( previousStep instanceof TransformerHandler ) {
			
				SAXResult result = new SAXResult(outputContentHandler);
				((TransformerHandler) previousStep).setResult(result);
			}	
			else if ( previousStep instanceof XMLFilter ) {
				
				((XMLFilter) previousStep).setContentHandler(outputContentHandler);				
			}
		}
	}
	

	public void setOutput(OutputStream outputStream) throws TransformerConfigurationException  {
		
		reset();
		
		TransformerHandler identity = saxTransFact.newTransformerHandler();			

		Result output = new StreamResult(outputStream);
		identity.setResult(output);					
		outputContentHandler = identity;
		identity.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		if ( pipeline.size() > 0 ) {
			
			// by default, output method is "xml", set it to be the same as for the previous step
			Object previousStep = pipeline.lastElement();
			
			if ( previousStep instanceof TransformerHandler ) {
								
				TransformerHandler th = ((TransformerHandler) previousStep);
				Transformer t = th.getTransformer();
				identity.getTransformer().setOutputProperty(OutputKeys.METHOD, t.getOutputProperty(OutputKeys.METHOD));
				if (t.getOutputProperty(OutputKeys.DOCTYPE_SYSTEM) != null)  identity.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, t.getOutputProperty(OutputKeys.DOCTYPE_SYSTEM));
				if (t.getOutputProperty(OutputKeys.DOCTYPE_PUBLIC) != null)  identity.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, t.getOutputProperty(OutputKeys.DOCTYPE_PUBLIC));
				//identity.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, t.getOutputProperty(OutputKeys.OMIT_XML_DECLARATION));
			}
		
			setOutput();			
		}
	}
	

	public void setOutput(DOMResult outputDOM) throws TransformerConfigurationException  {
		
		reset();
		
		TransformerHandler identity = saxTransFact.newTransformerHandler();		

		identity.setResult(outputDOM);					
		outputContentHandler = identity;
		identity.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		if ( pipeline.size() > 0 ) {
			
			// by default, output method is "xml", set it to be the same as for the previous step
			Object previousStep = pipeline.lastElement();
			
			if ( previousStep instanceof TransformerHandler ) {
								
				TransformerHandler th = ((TransformerHandler) previousStep);
				Transformer t = th.getTransformer();
				identity.getTransformer().setOutputProperty(OutputKeys.METHOD, t.getOutputProperty(OutputKeys.METHOD));
				if (t.getOutputProperty(OutputKeys.DOCTYPE_SYSTEM) != null)  identity.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, t.getOutputProperty(OutputKeys.DOCTYPE_SYSTEM));
				if (t.getOutputProperty(OutputKeys.DOCTYPE_PUBLIC) != null)  identity.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, t.getOutputProperty(OutputKeys.DOCTYPE_PUBLIC));
				//identity.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, t.getOutputProperty(OutputKeys.OMIT_XML_DECLARATION));
			}
		
			setOutput();			
		}
	}
	
	
	public ContentHandler getContentHandler() throws TransformerConfigurationException {
		
		reset();
		
		if ( pipeline.size() == 0 ) {
			
			// The pipeline is empty - just add a TransformerHandler that will give an identity transform
			System.out.println("empty...");
			
			TransformerHandler identity = saxTransFact.newTransformerHandler();

			if ( outputContentHandler != null ) identity.setResult(new SAXResult(outputContentHandler));
			else                                identity.setResult(new StreamResult(System.out));
			
			pipeline.add(identity);
		}
		
		Object first = pipeline.firstElement();

		if ( first instanceof TransformerHandler ) {

			return (ContentHandler) first;			
		}	
		else if ( first instanceof XMLFilter ) {
			
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
	
	public void init() throws TransformerConfigurationException {
		
		chain();
	}
	
	private void reset() throws TransformerConfigurationException {
		
		chain();
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

	
	public void transform(Source source) {
		
		Transformer transformer = null;
		
		Object firstStep = ( pipeline.size() > 0 ) ? pipeline.elementAt(0) : null;
		
		if ( firstStep != null && firstStep instanceof TransformerHandler ) {
			 
			TransformerHandler handler = (TransformerHandler) firstStep;
			transformer = handler.getTransformer();
		}
		else {
			
			try {
				transformer = this.getTransformerFactory().newTransformer();
			}
			catch (TransformerConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		Result result;
		
		if ( pipeline.size() > 1 ) {
			
			Object secondStep = pipeline.elementAt(1);
			result = new SAXResult((ContentHandler) secondStep);
			
		}
		else {
			
			result = new SAXResult(outputContentHandler);
		}

		try {
			transformer.transform(source, result);
		}
		catch (TransformerException e) {

			e.printStackTrace();
		}
	}
}
