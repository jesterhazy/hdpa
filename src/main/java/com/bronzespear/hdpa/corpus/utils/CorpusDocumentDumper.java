package com.bronzespear.hdpa.corpus.utils;

import com.bronzespear.hdpa.corpus.CorpusReader;
import com.bronzespear.hdpa.corpus.Document;

public class CorpusDocumentDumper {
	
	public static void main(String[] args) throws Exception {
		CorpusReader corpusReader = new CorpusReader(args[0]);
		corpusReader.open();
		
		for (Document document : corpusReader) {
			System.out.println("id: " + document.getId());
			System.out.println("  words: " + document.getWords());
			System.out.println("  pers: " + document.getPersons());
			System.out.println("  orgs: " + document.getOrganizations());
			System.out.println("  locs: " + document.getLocations());
			System.out.println("  title: " + document.getTitle().substring(0, Math.min(20, document.getTitle().length())));
			System.out.println("  text: " + document.getText().substring(0, Math.min(20, document.getText().length())));
		}
		
		corpusReader.close();
	}
}
