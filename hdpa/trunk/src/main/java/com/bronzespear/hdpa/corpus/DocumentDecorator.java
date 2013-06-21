package com.bronzespear.hdpa.corpus;

import java.util.List;

public class DocumentDecorator extends Document {
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
	
	public Document getDocument() {
		return doc;
	}
	
	public List<String> getTerms(CorpusMode mode) {
		return doc.getTerms(mode);
	}
}
