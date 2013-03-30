package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class AssignTopics {
	
	public static void main(String[] args) throws Exception {

		File modelFile = null;
		
		if (args.length >= 1) {
			modelFile = new File(args[0]);
			
			if (!modelFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		File corpusFile = HdpaUtils.getCorpusForModel(modelFile);
		File topicsFile = new File(modelFile.getParentFile(), "doctopics-" + modelFile.getName());
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		h.assignTopics(topicsFile);
		corpus.close();
	}
}
