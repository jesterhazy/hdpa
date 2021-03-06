package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class AssignTopics {
	
	public static void main(String[] args) throws Exception {

		File modelFile = null;
		File corpusFile = null;
		File topicsFile = null;
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				modelFile = new File(args[i]);
				break;
			case 1:
				corpusFile = new File(args[i]);
				break;
			case 2:
				topicsFile = new File(args[i]);
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
		
		if (topicsFile == null) {
			topicsFile = new File(modelFile.getParentFile(), String.format(
					"doctopics-%s-%s.csv",
					modelFile.getName().replaceAll("\\.csv$", ""),
					HdpaUtils.formattedTimestamp())); 					
		}
		
		if (!topicsFile.getParentFile().exists()) {
			throw new IllegalArgumentException("file does not exist: " + topicsFile.getParent());
		}
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		h.assignTopics(topicsFile);
		corpus.close();
	}
}
