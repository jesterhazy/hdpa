package com.bronzespear.hdpa.corpus;


public class TestDocument extends DocumentBase {
	
	public TestDocument(Document doc) {
		getWords().add("words");
		
		for (String s : doc.getPersons()) {
			getPersons().add("[" + s + "]");
		}
	}
}
