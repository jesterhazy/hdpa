package com.bronzespear.hdpa.coherence;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.FastMath;

import com.bronzespear.hdpa.Hdpa;
import com.bronzespear.hdpa.HdpaUtils;
import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class MimnoCoherence {
	
	private static final Log LOG = LogFactory.getLog(MimnoCoherence.class);

	private File modelFile;
	private int numberOfTerms;
	private int[][] topTerms;
	private int numberOfTopics;
	private int[] id2term;
	private Map<Integer, Integer> term2id;
	private int uniqueTermCount;
	private int[][] cooccurrenceCounts;
	private int[] occurrenceCounts;
	private double[] coherenceScores;

	private CorpusReader corpus;
	

	public MimnoCoherence(File modelFile) {
		this.modelFile = modelFile;
		this.numberOfTerms = 10;
	}

	public static void main(String[] args) throws Exception {
		
		File modelFile = null;
		
		if (args.length >= 1) {
			modelFile = new File(args[0]);
			
			if (!modelFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		MimnoCoherence app = new MimnoCoherence(modelFile);
		app.run();
	}

	private void run() throws IOException {
		File corpusFile = HdpaUtils.getCorpusForModel(modelFile);
		corpus = new CorpusReader(corpusFile);
		corpus.open();
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		
		LOG.info("getting top terms");
		topTerms = h.topTermIds(CorpusMode.WORD.ordinal(), numberOfTerms);
		numberOfTopics = topTerms.length;
		
		buildTermIdMaps();
		countOccurrences(corpus);
		calculateCoherence();
		
		reportResults(h);

		corpus.close();
	}

	private void reportResults(Hdpa h) {
		PrintStream out = System.out; // TODO
		
		out.println("topic   coherence   terms");
		out.println("-----   ---------   -----");
		for (int k = 0; k < numberOfTopics; k++) {
			out.printf("%5d   %f   %s\n", k, coherenceScores[k], h.topTerms(k, CorpusMode.WORD.ordinal(), numberOfTerms));
		}
		
		
		for (int k = 0; k < numberOfTopics; k++) {
			out.printf("\n\ntopic %d:\n", k);
			
			out.printf("%18s", "");
			for (int m = 0; m < numberOfTerms; m++) {
				out.printf("%6.6s  ", corpus.getDictionary(CorpusMode.WORD.ordinal()).getTerm(topTerms[k][m]));
			}
			out.println("total");
			
			
			for (int m = 0; m < numberOfTerms; m++) {
				out.printf("%15s   ", corpus.getDictionary(CorpusMode.WORD.ordinal()).getTerm(topTerms[k][m]));

				int i = term2id.get(topTerms[k][m]);
				for (int n = 0; n < numberOfTerms; n++) {
					int j = term2id.get(topTerms[k][n]);
					out.printf("%6d  ", cooccurrenceCounts[i][j]);
				}
				out.printf("%6d\n", occurrenceCounts[i]);
			}
		}
	}

	private void calculateCoherence() {
		LOG.info("calculating coherence scores");

		coherenceScores = new double[numberOfTopics];
		
		for (int k = 0; k < numberOfTopics; k++) {
			for (int m = 1; m < numberOfTerms; m++) {
				for (int n = 0; n < m; n++) {
					int i = term2id.get(topTerms[k][m]);
					int j = term2id.get(topTerms[k][n]);
					
					if (occurrenceCounts[j] > 0) {
						coherenceScores[k] += FastMath.log((double) (cooccurrenceCounts[i][j] + 1d) / (double) occurrenceCounts[j]);
					}
				}
			}
		}
	}

	private void countOccurrences(CorpusReader corpus) {
		LOG.info("counting occurrences");

		occurrenceCounts = new int[uniqueTermCount];
		cooccurrenceCounts  = new int[uniqueTermCount][uniqueTermCount];
		
		int documentsCounted = 0;
		for (CorpusDocument doc : corpus) {
			Set<Integer> docTerms = getDocumentTermSet(doc);
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
			
//			if (documentsCounted == 1000) break;
		}
	}
	
	private Set<Integer> getDocumentTermSet(CorpusDocument doc) {
		int[] terms = doc.getTermIds()[CorpusMode.WORD.ordinal()];
		
		Set<Integer> set = new HashSet<Integer>(terms.length);
		for (int i = 0; i < terms.length; i++) {
			set.add(terms[i]);
		}
		
		return set;
	}
	
	private void buildTermIdMaps() {
		LOG.info("building term id maps");

		term2id = new HashMap<Integer, Integer>();
		List<Integer> id2termList = new ArrayList<Integer>();
		for (int k = 0; k < topTerms.length; k++) {
			for (int i = 0; i < topTerms[k].length; i++) {
				int w = topTerms[k][i];
				
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
