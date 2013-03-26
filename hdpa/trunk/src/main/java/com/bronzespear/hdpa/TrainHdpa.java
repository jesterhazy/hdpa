package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class TrainHdpa {
	
	public static void main(String[] args) throws Exception {
		File corpusFile = null;
		int hourLimit = 0;
		int testDocumentCount = 0;
		int batchSize = 1000;

		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				corpusFile = new File(args[i]);
				break;
			case 1:
				hourLimit = Integer.parseInt(args[i]);
				break;
			case 2:
				testDocumentCount = Integer.parseInt(args[i]);
				break;
			case 3:
				batchSize = Integer.parseInt(args[i]);
				break;
			default:
			}
		}
		
		if (corpusFile == null) {
			throw new IllegalArgumentException("no corpus specified!");
		}
		
		if (!corpusFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + args[0]);
		}
		
		CorpusReader corpus = new CorpusReader(corpusFile);		
		Hdpa h = new Hdpa(corpus);
		
		if (testDocumentCount > 0) {
			h.setTestDocumentCount(testDocumentCount);
		}

		if (hourLimit > 0) {
			long endTime = System.currentTimeMillis() + (1000 * 60 * 60 * hourLimit);
			h.processUntilTime(endTime, batchSize);
		}
		
		else {
			h.processOnce(batchSize);
		}
	}
}
