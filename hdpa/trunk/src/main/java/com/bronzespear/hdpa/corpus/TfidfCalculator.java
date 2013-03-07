package com.bronzespear.hdpa.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TfidfCalculator {
	private static final Log LOG = LogFactory.getLog(TfidfCalculator.class);
	
	public static final class Score implements Comparable<Score> {
		private final Integer id;
		private final double score;

		public Score(Integer id, double score) {
			this.id = id;
			this.score = score;
		}

		public int compareTo(Score other) {
			return Double.compare(score, other.score);
		}
		
		public Integer getId() {
			return id;
		}
		
		public double getScore() {
			return score;
		}
	}
	
	/**
	 * A Matrix Market file that represents a document-term matrix.
	 */
	private final File input;
	
	/**
	 * File where the tfidf scores will be written.  
	 */
	private final File output;
	
	private Map<Integer, Integer> id2tf = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> id2df = new HashMap<Integer, Integer>();
	private List<Score> scores;
	
	int documentCount = 0;
	
	public TfidfCalculator(File input, File output) {
		this.input = input;
		this.output = output;
	}
	
	public void calculateScores() {
		LOG.info("calculating tfidf scores");
		scores = new ArrayList<Score>(id2tf.size());
		
		for (Integer id : id2tf.keySet()) {
			calculateScore(id);
		}
		
		Collections.sort(scores, Collections.reverseOrder());
	}

	private void calculateScore(Integer id) {
		double score = 0.0;
		Integer tf = id2tf.get(id);
		Integer df = id2df.get(id);
		
		if (tf != null && df != null) {
			double idf = Math.log(documentCount / df.doubleValue());
			score = tf * idf;
		}
		
		scores.add(new Score(id, Double.valueOf(score)));
	}
	
	public void calculate() {
		readCorpus();
		calculateScores();
		writeScores();
	}

	private void writeScores() {
		LOG.info("writing tfidf scores to " + output.getAbsolutePath());
		try {
			PrintWriter pw = new PrintWriter(output, "UTF-8");
			pw.printf("%10s %10s %10s %s (n = %d)\n", "id", "tf", "df", "score", documentCount);
			
			for (Score score : scores) {
				Integer id = score.getId();
				pw.printf("%10d %10d %10d %f\n", id, id2tf.get(id), id2df.get(id), score.getScore());
			}
			
			pw.close();
		}

		catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
		}
	}

	private void readCorpus() {
		LOG.info("reading corpus data from " + input.getAbsolutePath());
		try {
			String currentDocument = null;

			BufferedReader reader = new BufferedReader(new FileReader(input));
			String line = null;
			
			// discard headers
			reader.readLine();
			reader.readLine();
			
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				
				
				String docId = parts[0];
				Integer id = Integer.valueOf(parts[1]);
				Integer count = Integer.valueOf(parts[2]);
				
				if (!docId.equals(currentDocument)) {
					currentDocument = docId;
					documentCount++;
					
					if (documentCount % 10000 == 0) {
						LOG.info(String.format("read %d documents", documentCount));
					}
				}
				
				Integer tf = id2tf.get(id);
				
				if (tf == null) {
					id2tf.put(id, count);
				}
				
				else {
					id2tf.put(id, tf + count);
				}
				
				Integer df = id2df.get(id);
				
				if (df == null) {
					id2df.put(id, 1);
				}
				
				else {
					id2df.put(id, df + 1);
				}
			}
			
			reader.close();
			
		} catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
		}
	}
}
