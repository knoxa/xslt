package test.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

import xslt.Pipeline;
import xslt.TeeFilter;

public class PipelineTest {

	@Test
	public void name() {
		
		Pipeline p = new Pipeline();
		p.setName("Testing");
		assertEquals(p.getName(), "Testing");
	}

	//@Test
	public void zz() throws TransformerConfigurationException {
		
		Pipeline p = new Pipeline();
		
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p.setOutput(System.out);
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		//p.transform(s);
	}
	

	@Test
	public void chain() throws TransformerConfigurationException, FileNotFoundException {
		
		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));

		
		p2.setOutput(new FileOutputStream("sratch2.xml"));
		p1.setOutput(p2.getContentHandler());
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		p1.transform(s);
	}

/*	

	@Test
	public void tee() throws TransformerConfigurationException, FileNotFoundException {
		
		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		TeeFilter t = new TeeFilter();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));
		p1.addStep(t);
		
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("increment.xsl")));

		p1.setOutput(new FileOutputStream("sratch1.xml"));
		
		p2.setOutput(new FileOutputStream("sratch2.xml"));
		
		// it seems to matter exactly when you do this ...
		t.setTeeHandler(p2.getContentHandler());
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		p1.transform(s);
	}
*/
}
