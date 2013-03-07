package com.bronzespear.hdpa.corpus;

import java.util.List;

public interface Document {
	Integer getId();
	void setId(Integer id);
	String getTitle();
	String getText();
	List<String> getWords();
	List<String> getLocations();
	List<String> getOrganizations();
	List<String> getPersons();
	boolean isEmpty();
	List<String> getTerms(CorpusMode mode);
}
