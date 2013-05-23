package com.bronzespear.hdpa.coherence.ohdp;

import java.io.File;
import java.io.IOException;

import com.bronzespear.hdpa.HdpaUtils;
import com.bronzespear.hdpa.coherence.Corpus;
import com.bronzespear.hdpa.coherence.MimnoCoherence;
import com.bronzespear.hdpa.coherence.Model;

public class Wang {
	public static void main(String[] args) throws IOException {
		Wang app = new Wang();
		app.run();
	}

	private void run() throws IOException {
		String basedir = "/Users/jonathan/Documents/6 uathabasca/project/698 - implementation/wang comparison";
		String documentFile = basedir + "/nyt data/nyt-random-t5000.wang.txt";
		String dictionaryFile = basedir + "/nyt data/nyt-random-t5000.wang.dict";
		String modelFile = basedir + "/dat/20130331 complete runs/corpus-nyt-kappa-0.9-tau-1-batchsize-500/doc_count-1024000.topics";

		String outputFileName = String.format("coherence-%s.txt", HdpaUtils.formattedTimestamp());
		
		Corpus corpus = new WangCorpus(documentFile, dictionaryFile);
		Model model = new WangModel(modelFile);
		
		MimnoCoherence coherence = new MimnoCoherence(corpus, model);
//		coherence.setDocumentLimit(2000);
		coherence.calculate();
		coherence.report(new File(outputFileName));
	}
}
