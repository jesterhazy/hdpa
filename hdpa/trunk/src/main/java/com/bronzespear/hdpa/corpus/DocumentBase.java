package com.bronzespear.hdpa.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentBase extends Document {
	private Integer id;
	private String title;
	private String text;
	private Map<CorpusMode, List<String>> terms = new HashMap<CorpusMode, List<String>>();
	private Corpus corpus;
	
	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}
	
	public Corpus getCorpus() {
		return corpus;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public List<String> getTerms(CorpusMode mode) {
		List<String> list = terms.get(mode);
		
		if (list == null) {
			list = new ArrayList<String>();
			terms.put(mode, list);
		}
		
		return list;
	}
	
	public void setTerms(CorpusMode mode, List<String> terms) {
		this.terms.put(mode, terms);
	}
	
	public void setWords(List<String> words) {
		setTerms(CorpusMode.WORD, words);
	}
	
	public void setPersons(List<String> persons) {
		setTerms(CorpusMode.PERSON, persons);
	}

	public void setOrganizations(List<String> organizations) {
		setTerms(CorpusMode.ORGANIZATION, organizations);
	}

	public void setLocations(List<String> locations) {
		setTerms(CorpusMode.LOCATION, locations);
	}

	protected void addWords(List<String> words) {
		addTerms(CorpusMode.WORD, words);
	}
	
	protected void addPersons(List<String> persons) {
		addTerms(CorpusMode.PERSON, persons);
	}
	
	protected void addOrganizations(List<String> organizations) {
		addTerms(CorpusMode.ORGANIZATION, organizations);
	}
	
	protected void addLocations(List<String> locations) {
		addTerms(CorpusMode.LOCATION, locations);
	}
	
	private void addTerms(CorpusMode mode, List<String> terms) {
		for (String string : terms) {
			if (string != null && !string.isEmpty()) {
				getTerms(mode).add(string);
			}
		}
	}
}