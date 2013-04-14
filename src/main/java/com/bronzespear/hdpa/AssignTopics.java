package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class AssignTopics {
	
	public static void main(String[] args) throws Exception {

		File modelFile = null;
		File corpusFile = null;
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				modelFile = new File(args[i]);
				break;
			case 1:
				corpusFile = new File(args[i]);
				break;
			default:
			}
		}
		
		if (modelFile == null) {
			throw new IllegalArgumentException("no model specified!");
		}
		
		
		if (!modelFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + args[0]);
		}
		
		
		if (corpusFile == null) {
			corpusFile = HdpaUtils.getCorpusForModel(modelFile);
		}
		
		if (!corpusFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + args[1]);
		}
		
		
		File topicsFile = new File(modelFile.getParentFile(), "doctopics-" + modelFile.getName());
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		h.assignTopics(topicsFile);
		corpus.close();
	}
}
