package com.bronzespear.hdpa.corpus;

import java.util.List;

public abstract class Document {
	public abstract Integer getId();
	public abstract void setId(Integer id);
	public abstract String getTitle();
	public abstract String getText();
	
	public final List<String> getWords() {
		return getTerms(CorpusMode.WORD);
	}
	
	public final List<String> getLocations() {
		return getTerms(CorpusMode.LOCATION);
	}
	
	public final List<String> getOrganizations() {
		return getTerms(CorpusMode.ORGANIZATION);
	}

	public final List<String> getPersons() {
		return getTerms(CorpusMode.PERSON);
	}
	
	public boolean isEmpty() {
		return getWords().isEmpty();
	}
	
	public abstract List<String> getTerms(CorpusMode mode);
}
