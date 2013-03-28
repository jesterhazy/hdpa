package com.bronzespear.hdpa;

import java.io.File;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class TrainHdpa {
	
	public static void main(String[] args) throws Exception {
		File corpusFile = null;
		int saveFrequency = 0;
		int skipDocumentCount = 0;
		int batchSize = 500;
		int hourLimit = 0;

		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				corpusFile = new File(args[i]);
				break;
			case 1:
				saveFrequency = Integer.parseInt(args[i]);
				break;
			case 2:
				skipDocumentCount = Integer.parseInt(args[i]);
				break;
			case 3:
				batchSize = Integer.parseInt(args[i]);
				break;
			case 4:
				hourLimit = Integer.parseInt(args[i]);
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
		
		if (saveFrequency > 0) {
			h.setSaveFrequency(saveFrequency);
		}
		
		if (skipDocumentCount > 0) {
			h.setSkipDocumentsCount(skipDocumentCount);
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
