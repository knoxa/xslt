package xslt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

public class Utils {

	public static Set<String> getTextResultsAsSet(File input, Pipeline pipeline) throws TransformerException, IOException {
		
		assert pipeline.getOutputMethod().equals("text") : "Pipeline must output text";
		
		Set<String> results = new HashSet<String>();
		PipedInputStream pin = new PipedInputStream();
		PipedOutputStream pout = new PipedOutputStream(pin);
		
		Reader reader = new InputStreamReader(pin);
		
		Runnable loader = new Runnable() {

			@Override
			public void run() {
				
				try {
					
					Source s = new StreamSource(new FileInputStream(input));
					pipeline.setOutput(pout);					
					pipeline.transform(s);
					pout.close();
				}
				catch (TransformerException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		};
		
		new Thread(loader).start();
		
		BufferedReader buffer = new BufferedReader(reader);
        String line = null;
		
		while ( (line = buffer.readLine()) != null ) {
			
			results.add(line);
		}
		
		buffer.close();
		
		return(results);
	}
	
}
