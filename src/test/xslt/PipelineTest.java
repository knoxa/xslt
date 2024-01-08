package test.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import xslt.Pipeline;
import xslt.TeeFilter;

public class PipelineTest {

	//@Test
	public void name() {
		
		Pipeline p = new Pipeline();
		p.setName("Testing");
		assertEquals(p.getName(), "Testing");
	}

	@Test
	public void identity() throws TransformerException {
		
		Pipeline p = new Pipeline();
		//p.addStep(new StreamSource(this.getClass().getResourceAsStream("identity.xsl")));
		p.setOutput(System.out);
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		p.transform(s);
	}

	//@Test
	public void zz() throws TransformerException {
		
		XMLFilter f = new XMLFilterImpl();
		Pipeline p = new Pipeline();
		
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p.addStep(f);
		p.addStep(new XMLFilterImpl());
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		//p.addStep(new StreamSource(this.getClass().getResourceAsStream("sum.xsl")));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		p.setOutput(out);
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		p.transform(s);
		
		String result = new String(out.toByteArray());
		System.out.println("result=" + result);
	}
	

	//@Test
	public void chain() throws FileNotFoundException, TransformerException {
		
		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));

		
		p2.setOutput(new FileOutputStream("sratch2.xml"));
		ContentHandler ch = p2.getContentHandler();
		assertNotNull(ch);
		
		System.out.println("CH :" + ch);
		p1.setOutput(ch);
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		p1.transform(s);
	}



	@Test
	public void tee() throws FileNotFoundException, TransformerException {
		
		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		TeeFilter t = new TeeFilter();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p1.addStep(t);
		//p1.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));

		p1.setOutput(new FileOutputStream("sratch1.xml"));
		
		p2.setOutput(new FileOutputStream("sratch2.xml"));
		
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		// it seems to matter exactly when you do this ...
		t.setTeeHandler(p2.getContentHandler());
		p1.transform(s);
	}

}
