package com.bronzespear.hdpa.coherence;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.FastMath;

public class MimnoCoherence {
	
	private static final Log LOG = LogFactory.getLog(MimnoCoherence.class);

	private int numberOfTerms = 10;
	private int documentLimit = 0; // maximum number of documents to count (0 == all)

	private Model model;
	private Corpus corpus;

	private int[] id2term;
	private Map<Integer, Integer> term2id;
	private int uniqueTermCount;
	private int[][] cooccurrenceCounts;
	private int[] occurrenceCounts;
	private double[] coherenceScores;

	public MimnoCoherence(Corpus corpus, Model model) {
		this.corpus = corpus;
		this.model = model;
	}
	
	public void setNumberOfTerms(int numberOfTerms) {
		this.numberOfTerms = numberOfTerms;
	}
	
	public void setDocumentLimit(int documentLimit) {
		this.documentLimit = documentLimit;
	}
	
	public void calculate() throws IOException {
		corpus.open();
		
		model.setTermLimit(numberOfTerms);
		model.load();
		
		buildTermIdMaps();
		countOccurrences();
		calculateCoherence();
		corpus.close();
	}

	public void report(File outputFile) throws IOException {
		PrintWriter out = new PrintWriter(outputFile);
		
		out.println("topic   coherence   terms");
		out.println("-----   ---------   -----");
		for (int k = 0; k < model.numberOfTopics(); k++) {
			out.printf("%5d   %f   %s\n", k, coherenceScores[k], corpus.getTerms(model.topTermIds(k)));
		}
		
		
		for (int k = 0; k < model.numberOfTopics(); k++) {
			out.printf("\n\ntopic %d:\n", k);
			
			out.printf("%18s", "");
			for (int m = 0; m < numberOfTerms; m++) {
				out.printf("%6.6s  ", corpus.getTerm(model.topTermIds()[k][m]));
			}
			out.println("total");
			
			
			for (int m = 0; m < numberOfTerms; m++) {
				out.printf("%15s   ", corpus.getTerm(model.topTermIds()[k][m]));

				int i = term2id.get(model.topTermIds()[k][m]);
				for (int n = 0; n < numberOfTerms; n++) {
					int j = term2id.get(model.topTermIds()[k][n]);
					out.printf("%6d  ", cooccurrenceCounts[i][j]);
				}
				out.printf("%6d\n", occurrenceCounts[i]);
			}
		}
		
		out.close();
	}

	private void calculateCoherence() {
		LOG.info("calculating coherence scores");

		coherenceScores = new double[model.numberOfTopics()];
		
		for (int k = 0; k < model.numberOfTopics(); k++) {
			for (int m = 1; m < numberOfTerms; m++) {
				for (int n = 0; n < m; n++) {
					int i = term2id.get(model.topTermIds()[k][m]);
					int j = term2id.get(model.topTermIds()[k][n]);
					
					if (occurrenceCounts[j] > 0) {
						coherenceScores[k] += FastMath.log((double) (cooccurrenceCounts[i][j] + 1d) / (double) occurrenceCounts[j]);
					}
				}
			}
		}
	}

	private void countOccurrences() {
		LOG.info("counting occurrences");

		occurrenceCounts = new int[uniqueTermCount];
		cooccurrenceCounts  = new int[uniqueTermCount][uniqueTermCount];
		
		int documentsCounted = 0;
		for (Document doc : corpus) {
			Set<Integer> docTerms = doc.uniqueTermIds();
			for (int i = 0; i < uniqueTermCount; i++) {
				
				if (docTerms.contains(id2term[i])) {
					occurrenceCounts[i]++;
					
					for (int j = 0; j < uniqueTermCount; j++) {
						if (docTerms.contains(id2term[j])) {
							cooccurrenceCounts[i][j]++;
						}
					}
				}
			}
			
			documentsCounted++;
			if (documentsCounted % 1000 == 0) {
				LOG.info(String.format("counted %d documents", documentsCounted));
			}
			
			if (documentLimit > 0 && documentLimit == documentsCounted) break;
		}
	}
	
	private void buildTermIdMaps() {
		LOG.info("building term id maps");

		term2id = new HashMap<Integer, Integer>();
		List<Integer> id2termList = new ArrayList<Integer>();
		for (int k = 0; k < model.numberOfTopics(); k++) {
			for (int i = 0; i < model.topTermIds(k).length; i++) {
				int w = model.topTermIds()[k][i];
				
				if (!term2id.containsKey(w)) {
					term2id.put(w, term2id.size());
					id2termList.add(w);
				}
			}
		}
		
		id2term = new int[id2termList.size()];
		for (int i = 0; i < id2term.length; i++) {
			id2term[i] = id2termList.get(i).intValue();
		}
		
		uniqueTermCount = id2term.length;
	}
}
