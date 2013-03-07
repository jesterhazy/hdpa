package com.bronzespear.hdpa.corpus;

import java.util.List;

public class DocumentDecorator implements Document {
	private final Document doc;
	
	public DocumentDecorator(Document doc) {
		this.doc = doc;
	}

	public Integer getId() {
		return doc.getId();
	}
	
	public void setId(Integer id) {
		doc.setId(id);
	}
	
	public String getTitle() {
		return doc.getTitle();
	}

	public String getText() {
		return doc.getText();
	}

	public List<String> getWords() {
		return doc.getWords();
	}

	public List<String> getLocations() {
		return doc.getLocations();
	}

	public List<String> getOrganizations() {
		return doc.getOrganizations();
	}

	public List<String> getPersons() {
		return doc.getPersons();
	}
	
	public Document getDocument() {
		return doc;
	}
	
	public boolean isEmpty() {
		return doc.isEmpty();
	}

	public List<String> getTerms(CorpusMode mode) {
		return doc.getTerms(mode);
	}
}
