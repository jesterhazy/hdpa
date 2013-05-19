package com.bronzespear.hdpa.coherence.hdpa;

import java.io.File;
import java.io.IOException;

import com.bronzespear.hdpa.Hdpa;
import com.bronzespear.hdpa.HdpaUtils;
import com.bronzespear.hdpa.coherence.Corpus;
import com.bronzespear.hdpa.coherence.MimnoCoherence;
import com.bronzespear.hdpa.coherence.Model;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class HdpaCoherence {
	private File corpusFile;
	private File modelFile;
	
	public HdpaCoherence(File corpusFile, File modelFile) {
		this.corpusFile = corpusFile;
		this.modelFile = modelFile;
	}
	
	public static void main(String[] args) throws IOException {
		File modelFile = null;
		
		if (args.length >= 1) {
			modelFile = new File(args[0]);
			
			if (!modelFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		File corpusFile = HdpaUtils.getCorpusForModel(modelFile);
		if (!corpusFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + corpusFile.getAbsolutePath());
		}
		
		HdpaCoherence app = new HdpaCoherence(corpusFile, modelFile);
		app.run();
	}
	
	private void run() throws IOException {
		CorpusReader corpusReader = new CorpusReader(corpusFile);
		Hdpa h = new Hdpa(corpusReader);
		
		Corpus corpus = new HdpaCorpus(corpusReader);
		Model model = new HdpaModel(h, modelFile);
		
		MimnoCoherence coherence = new MimnoCoherence(corpus, model);
//		coherence.setDocumentLimit(2000);
		coherence.calculate();
		coherence.report();
	}
}
