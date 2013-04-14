package com.bronzespear.hdpa.solr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.CorpusDocument;

public class PreassignedTopicStrategy implements TopicStrategy {

	private static final Log LOG = LogFactory.getLog(PreassignedTopicStrategy.class);
	
	private BufferedReader reader;
	private boolean eof;
	private int linesRead;
	
	/**
	 * The current document id
	 */
	private int currentId = -1;
	
	/**
	 * Topic weights for the current document
	 */
	private double[] currentTopics;
	
	public PreassignedTopicStrategy(File docTopicsFile) throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(docTopicsFile), "UTF-8"));
	}

	public double[] getTopics(CorpusDocument document) {
		
		double[] topics = null;

		int docId = document.getId().intValue();
		while (currentId < docId) {
			readline();
		}
		
		if (currentId == docId) {
			topics = currentTopics;		
		}
		
		return topics;
	}
	
	private void readline() {
		if (!eof) {
			String line = null;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				LOG.error(String.format("error reading line %d: %s", linesRead, e.getMessage()), e);
			}
			
			if (line == null) {
				eof = true;
				LOG.info("reached end of file at line: " + linesRead);
			}
			
			else {
				linesRead++;
				String[] parts = line.split(",");
				
				if (currentTopics == null) {
					currentTopics = new double[parts.length - 1];
				}
				
				try {
					for (int i = 0; i < parts.length; i++) {
						
						if (i == 0) {
							currentId = Integer.parseInt(parts[i]);
						}
						
						else {
							currentTopics[i - 1] = Double.parseDouble(parts[i]);
						}
						
					}
				} catch (NumberFormatException e) {
					LOG.warn(String.format("invalid format at line %d: [%s]", linesRead, line));
				}
			}
		}
	}
	
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.warn("error closing topics file: " + e.getMessage(), e);
			}
			reader = null;
		}
	}
}
