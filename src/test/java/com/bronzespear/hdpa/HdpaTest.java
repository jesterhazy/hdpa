package com.bronzespear.hdpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class HdpaTest {

	@Test
	public void testSaveParameters() throws Exception {
		CorpusReader corpus = mock(CorpusReader.class);
		
		when(corpus.getDocumentCount()).thenReturn(1000);
		when(corpus.getModeCount()).thenReturn(4);
		when(corpus.getTermCount(anyInt())).thenReturn(100);
		when(corpus.getAverageTermsPerDoc()).thenReturn(new double[4]);

		File file = new File("save.csv");
		file.deleteOnExit();
		
		Hdpa hdpa = new Hdpa(corpus);
		hdpa.start();
		hdpa.saveParameters(file);
		
		int lines = countLines(file);
		assertEquals(1216, lines); // 16 lines + M * K lines for lambda 
		
		hdpa = new Hdpa(corpus);
		hdpa.loadParameters(file);
		// no exception == good
		
		assertNotNull(hdpa.eta);
		
		assertEquals(1200, hdpa.lambda.length * hdpa.lambda[0].length);
		assertEquals(hdpa.getK() - 1, hdpa.corpusSticks[0].length);
		assertNotNull(hdpa.elogPhi);
	}
	
	private int countLines(File file) throws IOException {
		
		int lines = 0;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.readLine() != null) {
				lines++;
			}
		}

		finally {
			reader.close();
		}
		
		return lines;
	}
}
