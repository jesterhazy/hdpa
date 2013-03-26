package com.bronzespear.hdpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class EvaluateHdpa {
	
	public static void main(String[] args) throws Exception {
		File modelFile = null;
		int testDocumentCount = 1000;

		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				modelFile = new File(args[i]);
				break;
			case 1:
				testDocumentCount = Integer.parseInt(args[i]);
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
		
		String basename = modelFile.getName().replaceAll("-model.*$", "");
		File corpusFile = new File(modelFile.getParentFile(), basename);
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		
		List<List<HdpaDocument>> docs = extractTestDocuments(corpus, testDocumentCount);
		List<HdpaDocument> train = docs.get(0);
		List<HdpaDocument> test = docs.get(1);
		corpus.close();
		
		Hdpa h = new Hdpa(corpus);
		h.loadParameters(modelFile);
		h.setTestData(train, test);
		h.evaluateModel();
	}		

	private static List<List<HdpaDocument>> extractTestDocuments(
			CorpusReader corpus, int testDocumentCount) {

		List<List<HdpaDocument>> docs = new ArrayList<List<HdpaDocument>>();
		List<HdpaDocument> train = new ArrayList<HdpaDocument>();
		List<HdpaDocument> test = new ArrayList<HdpaDocument>();
		docs.add(train);
		docs.add(test);
		
		for (CorpusDocument doc : corpus) {
			HdpaDocument[] split = HdpaUtils.splitTestDocument(new HdpaDocument(doc));
			
			// if original document is very short, 
			// test doc will have 0 length (and -Infinity log probability)
			if (split[1].getTotalTermCount() > 0) {
				train.add(split[0]);
				test.add(split[1]);	
			}
			
			if (testDocumentCount <= train.size()) {
				break;
			}
		}
		
		return docs;
	}
}
