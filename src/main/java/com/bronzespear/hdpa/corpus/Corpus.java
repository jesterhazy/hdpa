package com.bronzespear.hdpa.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Corpus {
	private static final Log LOG = LogFactory.getLog(Corpus.class);

	private File basedir;
	private int modeCount;
	private int documentCount;
	protected File[] dictionaryFiles;
	File[] mmFiles;
	private File[] tfidfFiles;
	protected File textFile;
	protected File titleFile;
	protected final Dictionary[] dictionaries;

	public Corpus(File basedir) {
		this.basedir = basedir;
		this.modeCount = CorpusMode.values().length;
		
		this.dictionaryFiles = new File[4];
		this.mmFiles = new File[4];
		this.tfidfFiles = new File[4];
		this.dictionaries = new Dictionary[4];
		
		
		for (CorpusMode mode : CorpusMode.values()) {
			int m = mode.ordinal();
			String baseName = mode.name().toLowerCase();
			dictionaryFiles[m] = new File(getBasedir(), baseName + ".dict"); 
			mmFiles[m] = new File(getBasedir(), baseName + ".mm");
			tfidfFiles[m] = new File(getBasedir(), baseName + ".tfidf");
			dictionaries[m] = new Dictionary();
		}
		
		titleFile = new File(getBasedir(), "titles.txt");
		textFile = new File(getBasedir(), "text.txt");
	}
	
	public int getDocumentCount() {
		return documentCount;
	}
	
	void setDocumentCount(int documentCount) {
		this.documentCount = documentCount;
	}
	
	public File getBasedir() {
		return basedir;
	}
	
	void incrementDocumentCount() {
		documentCount++;
		
		if (documentCount % 10000 == 0) {
			LOG.info(String.format("corpus has %d documents", documentCount));
		}
	}

	public int getModeCount() {
		return modeCount;
	}
	
	public Dictionary getDictionary(int m) {
		return dictionaries[m];
	}
	
	public int getTermCount(int m) {
		return getDictionary(m).size();
	}
	
	public void updateTfidfData() {
		for (CorpusMode mode : CorpusMode.values()) {
			updateTfidfData(mode);
		}
	}
	
	private void updateTfidfData(CorpusMode mode) {
		int m = mode.ordinal();
		TfidfCalculator tfidf = new TfidfCalculator(mmFiles[m], tfidfFiles[m]);
		tfidf.calculate();
	}
	
	public List<String> topTerms(CorpusMode mode, int limit) throws IOException {
		int m = mode.ordinal();
		List<String> topTerms = new ArrayList<String>(limit);
		
		if (limit > 0) {
			int linesRead = 0;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tfidfFiles[m]), "UTF-8"));
			
			String line = null;
			while(linesRead <= limit && (line = reader.readLine()) != null) {
				if (linesRead > 0) {
					String[] parts = line.split(" +");
					Integer id = Integer.valueOf(parts[1]) - 1;
					topTerms.add(getDictionary(m).getTerm(id));
				}
				
				linesRead++;
			}
			
			reader.close();
		}
		
		return topTerms;
	}
}
