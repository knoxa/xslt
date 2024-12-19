package test.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import xslt.Pipeline;
import xslt.TeeFilter;

public class PipelineTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	
	@Test
	public void name() {
		
		// A pipeline can be named
		
		Pipeline p = new Pipeline();
		p.setName("Testing");
		assertEquals(p.getName(), "Testing");
	}

	@Test
	public void empty() throws TransformerException, IOException {
		
		// When there are no steps in this pipeline, you just get an error message if you try to use it

		Pipeline p = new Pipeline();
		p.setOutput(System.out);
		
		Source s = new StreamSource(this.getClass().getResourceAsStream("sample.xml"));
		
		ByteArrayOutputStream myErr = new ByteArrayOutputStream();
		System.setErr(new PrintStream(myErr));	
		p.transform(s);
		String err = new String(myErr.toByteArray(), "UTF-8");
		System.setErr(System.out);
		myErr.close();
		
		assertTrue(err.trim().matches("^The pipeline .*? empty\\.$"));
	}

	@Test
	public void identityTransform() throws TransformerException, IOException {
		
		// A single identity transform step - the output is the same as the input

		File sample = temp.newFile("sample.xml");
		File output = temp.newFile("identity.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);

		Pipeline p = new Pipeline();
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/identity.xsl")));
		p.setOutput(new FileOutputStream(output));
						
		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);	
		
		String input  = FileUtils.readFileToString(sample, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(input, result);
	}

	@Test
	public void identityFilter() throws TransformerException, IOException {
		
		// A single "pass through" XMLFilter step - the output is the same as the input

		File sample = temp.newFile("sample.xml");
		File output = temp.newFile("identity.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);

		Pipeline p = new Pipeline();
		p.addStep(new XMLFilterImpl());
		p.setOutput(new FileOutputStream(output));
						
		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);	
		
		String input  = FileUtils.readFileToString(sample, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(input, result);
	}

	@Test
	public void testFilter() throws TransformerException, IOException {
		
		// A single "pass through" XMLFilter step - the output is the same as the input

		File sample = temp.newFile("sample.xml");
		File added = temp.newFile("sample-added.xml");
		File output = temp.newFile("output.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample-added.xml"), added);

		Pipeline p = new Pipeline();
		p.addStep(new TestFilter());
		p.setOutput(new FileOutputStream(output));
						
		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);	
		
		String expect = FileUtils.readFileToString(added, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(expect, result);
	}

	@Test
	public void pipeline() throws TransformerException, IOException {
		
		// A 2-step pipeline: At each step 'increment.xsl' increments the @count attribute in the sample
		// XML by 1 - so all these attributes should have value '2' at the end of this pipeline (as in 'count2.xml').
		
		File sample   = temp.newFile("sample.xml");
		File output   = temp.newFile("pipeline.xml");
		File expected = temp.newFile("expected.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/count2.xml"), expected);

		Pipeline p = new Pipeline();		
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p.setOutput(new FileOutputStream(output));
		
		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);
		
		String expect = FileUtils.readFileToString(expected, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(expect, result);
	}

	@Test
	public void text() throws TransformerException, IOException {
		
		// A 3-step pipeline: In each of the first two steps, 'increment.xsl' increments the 3 @count attributes
		// in the sampleXML by 1; the final 'sum'xsl' stylesheet adds all the @count attributes. It has its
		// output methods set to 'text'. 3 x 2  = 6
		
		File sample   = temp.newFile("sample.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);

		Pipeline p = new Pipeline();		
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/sum.xsl")));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		p.setOutput(out);
		
		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);
		
		String result = new String(out.toByteArray());
		assertEquals("6", result);
	}
	

	@Test
	public void chain() throws TransformerException, IOException {
			
		// Chaining pipelines: The first has one increment step, and the second has two. The result should
		// therefore have all @count attributes set to 3.
		
		File sample   = temp.newFile("sample.xml");
		File output   = temp.newFile("pipeline.xml");
		File expected = temp.newFile("expected.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/count3.xml"), expected);
		
		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
	
		p2.setOutput(new FileOutputStream(output));
		ContentHandler ch = p2.getContentHandler();
		assertNotNull(ch);
		p1.setOutput(ch);
		Source s = new StreamSource(new FileInputStream(sample));
		p1.transform(s);
		
		String expect = FileUtils.readFileToString(expected, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(expect, result);
	}

	@Test
	public void tee() throws TransformerException, IOException {
		
		// The Tee Filter lets you branch off from the middle of a pipeline. Here, we have p1 that increments 3 times,
		// and p2 that increments once. Since the first increment of p1 is before the Tee filter, it applies to both pipelines.
		// The result is a count of 3 from p1, and a count of 2 from p2.

		File sample  = temp.newFile("sample.xml");
		File output1 = temp.newFile("tee1.xml");
		File output2 = temp.newFile("tee2.xml");
		File expected1 = temp.newFile("expected1.xml");
		File expected2 = temp.newFile("expected2.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/count3.xml"), expected1);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/count2.xml"), expected2);

		Pipeline p1 = new Pipeline();
		Pipeline p2 = new Pipeline();
		
		TeeFilter t = new TeeFilter();
		
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p1.addStep(t);
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		p1.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));
		
		p2.addStep(new StreamSource(this.getClass().getResourceAsStream("xsl/increment.xsl")));

		p1.setOutput(new FileOutputStream(output1));		
		p2.setOutput(new FileOutputStream(output2));
				
		Source s = new StreamSource(new FileInputStream(sample));
		// it seems to matter exactly when you do this ...
		t.setTeeHandler(p2.getContentHandler());
		p1.transform(s);
		
		String result1 = FileUtils.readFileToString(output1, "UTF-8");
		String expect1 = FileUtils.readFileToString(expected1, "UTF-8");
		assertEquals(expect1, result1);
		
		String result2 = FileUtils.readFileToString(output2, "UTF-8");
		String expect2 = FileUtils.readFileToString(expected2, "UTF-8");
		assertEquals(expect2, result2);
	}

}
