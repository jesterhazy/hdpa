package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class TrainHdpa {
	
	public static void main(String[] args) throws Exception {
		File corpusFile = null;
		int batchSize = 10000;
		int passes = 1;
		
		if (args.length >= 1) {
			corpusFile = new File(args[0]);
			
			if (!corpusFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		if (args.length >= 2) {
			batchSize = Integer.parseInt(args[1]);
		}
		
		if (args.length >= 3) {
			passes = Integer.parseInt(args[2]);
		}
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		MathUtils.accurate();
		Hdpa h = new Hdpa(corpus);
		h.processMultiplePasses(passes, batchSize);
	}
}
