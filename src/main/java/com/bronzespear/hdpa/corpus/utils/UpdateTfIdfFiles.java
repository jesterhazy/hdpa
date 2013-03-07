package com.bronzespear.hdpa.corpus.utils;

import com.bronzespear.hdpa.corpus.CorpusReader;

public class UpdateTfIdfFiles {
	
	public static void main(String[] args) throws Exception {
		CorpusReader corpusReader = new CorpusReader(args[0]);		
		corpusReader.open();
		corpusReader.updateTfidfData();
		corpusReader.close();
	}
}
