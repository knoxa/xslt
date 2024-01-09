package test.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xslt.Pipeline;
import xslt.PipelineBuilder;

public class PipelineBuilderTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void build() throws IOException, TransformerException {
		
		// Build a pipeline
		
		File pipelineFile = temp.newFile("pipeline.xml");
		File sample = temp.newFile("sample.xml");
		File output = temp.newFile("build.xml");
		File expected = temp.newFile("expected.xml");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("pipelines/sample.xml"), pipelineFile);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/sample.xml"), sample);
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("data/count2.xml"), expected);

		File xsl = temp.newFolder("xsl");
		FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("xsl/increment.xsl"), new File(xsl, "increment.xsl"));

		PipelineBuilder builder = new PipelineBuilder();				  
		Pipeline p = builder.getPipeline(pipelineFile);
		p.setOutput(new FileOutputStream(output));

		Source s = new StreamSource(new FileInputStream(sample));
		p.transform(s);	

		String expect = FileUtils.readFileToString(expected, "UTF-8");
		String result = FileUtils.readFileToString(output, "UTF-8");
		assertEquals(expect, result);
	}


}
