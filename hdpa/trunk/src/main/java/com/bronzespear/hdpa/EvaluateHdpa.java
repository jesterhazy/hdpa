package com.bronzespear.hdpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class EvaluateHdpa {
	
	public static void main(String[] args) throws Exception {
		int testDocumentCount = 2000;
		
		List<File> modelFiles = new ArrayList<File>();
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				testDocumentCount = Integer.parseInt(args[i]);
				break;
			default:
				File file = new File(args[i]);
				if (!file.exists()) {
					throw new IllegalArgumentException("file does not exist: " + args[i]);
				}
				
				if (!file.getName().matches("^.*\\.csv$")) {
					throw new IllegalArgumentException("not a model file: " + args[i]);
				}
				
				modelFiles.add(file);
			}
		}
		
		if (modelFiles.isEmpty()) {
			throw new IllegalArgumentException("no model specified!");
		}
		
		File corpusFile = HdpaUtils.getCorpusForModel(modelFiles.get(0));
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		List<HdpaDocument> documents = collectTestDocuments(testDocumentCount, corpus);
		corpus.close();
		
		List<List<HdpaDocument>> splitDocuments = HdpaUtils.splitTestDocuments(documents);			
		List<HdpaDocument> train = splitDocuments.get(0);
		List<HdpaDocument> test = splitDocuments.get(1);
		
		LdaEvaluator eval = new LdaEvaluator();
		for (File modelFile : modelFiles) {			
			eval.setModelFile(modelFile);
			eval.evaluate(train, test);
		}
		
		eval.close();
	}

	private static List<HdpaDocument> collectTestDocuments(int testDocumentCount,
			CorpusReader corpus) {
		List<HdpaDocument> documents = new ArrayList<HdpaDocument>();
		int i = 0;
		for (CorpusDocument document : corpus) {
			documents.add(new HdpaDocument(document));
			i++;
			
			if (i == testDocumentCount) {
				break;
			}
		}
		
		return documents;
	}		
}
