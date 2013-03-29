package com.bronzespear.hdpa.corpus.utils;

import java.io.File;
import java.io.PrintWriter;

import com.bronzespear.hdpa.HdpaDocument;
import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class MM2Wang {
	
	public static void main(String[] args) throws Exception {
		
		File corpusFile = null;

		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				corpusFile = new File(args[i]);
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
		
		
		int wordMode = CorpusMode.WORD.ordinal();
		
		// dictionary format is identical, so only need to convert mm file
		File file = new File(corpusFile.getParentFile(), corpusFile.getName() + ".wang.txt");
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		
		CorpusReader corpusReader = new CorpusReader(corpusFile);
		corpusReader.open();

		for (CorpusDocument corpusDocument : corpusReader) {
			HdpaDocument doc = new HdpaDocument(corpusDocument);

			int[] ids = doc.getTermIds()[wordMode];
			int[] counts = doc.getTermCounts()[wordMode];

			writer.write(String.valueOf(doc.getTotalTermCount()));
			
						
			for (int i = 0; i < ids.length; i++) {
				writer.write(String.format(" %d:%d", ids[i], counts[i]));
			}
			
			writer.write("\n");
		}
		
		writer.close();
		corpusReader.close();
	}
}
