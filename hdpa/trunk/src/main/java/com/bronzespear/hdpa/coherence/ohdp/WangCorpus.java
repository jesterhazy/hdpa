package com.bronzespear.hdpa.coherence.ohdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.coherence.Corpus;
import com.bronzespear.hdpa.coherence.Document;

public class WangCorpus implements Corpus {
	private static final Log LOG = LogFactory.getLog(WangCorpus.class);

	private File documentFile;
	private File dictionaryFile;
	private BufferedReader documentReader;

	private String[] dictionary;
	
	public WangCorpus(String documentFile, String dictionaryFile) {
		this.documentFile = new File(documentFile);
		this.dictionaryFile = new File(dictionaryFile);
	}
	
	public void open() throws IOException{
		LOG.info("opening corpus at " + documentFile.getAbsolutePath());
		documentReader = new BufferedReader(new InputStreamReader(new FileInputStream(documentFile), "UTF-8"));
		
		readDictionary();
	}
	
	public void close() throws IOException {
		LOG.info("closing corpus at " + documentFile.getAbsolutePath());
		documentReader.close();
	}

	private void readDictionary() throws IOException {
		List<String> terms = new ArrayList<String>();
		
		LOG.info("reading dictionary from " + dictionaryFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryFile), "UTF-8"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			terms.add(line.trim());
		}
		
		reader.close();
		LOG.info(String.format("read %d terms", terms.size()));
		
		dictionary = terms.toArray(new String[0]);
	}

	public Iterator iterator() {
		return new Iterator();
	}
	
	private final class Iterator implements java.util.Iterator<Document> {
		private Document next;
		private int count;

		public boolean hasNext() {
			next = null;
			
			try {
				String line = documentReader.readLine();
				if (line != null) {
					next = new WangDocument(line);
				}
			} catch (IOException e) {
				LOG.error(String.format("error reading line %d: %s", count, e.getMessage()), e);
			}
			
			count++;
			return next != null;
		}

		public Document next() {
			return next;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public String getTerm(int id) {
		return dictionary[id];
	}
	
	public List<String> getTerms(int[] termIds) {
		String[] terms = new String[termIds.length];
		
		for (int i = 0; i < termIds.length; i++) {
			terms[i] = getTerm(termIds[i]);
		}
		
		return Arrays.asList(terms);
	}
}
