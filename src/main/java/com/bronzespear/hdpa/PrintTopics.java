package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class PrintTopics {
	
	public static void main(String[] args) throws Exception {
		
		File modelFile = null;
		
		
		if (args.length >= 1) {
			modelFile = new File(args[0]);
			
			if (!modelFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		
		String basename = modelFile.getName().replaceAll("-model.*$", "");
		File corpusFile = new File(modelFile.getParentFile(), basename);
		File topicsFile = new File(modelFile.getParentFile(), basename + "-topics.txt");
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		h.printTopics(topicsFile);
		corpus.close();
	}
}
