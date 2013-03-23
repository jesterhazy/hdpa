package com.bronzespear.hdpa.corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

public class CorpusDocument implements Document, Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient Corpus corpus;
	private Integer id;
	private String title;
	private String text;
	private int[][] termIds = {{}, {}, {}, {}};
	private int[][] termCounts = {{}, {}, {}, {}};
	
	public CorpusDocument(Corpus corpus) {
		this.corpus = corpus;
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

	public String getText() {
		return text;
	}
	
	public List<String> getWords() {
		return getTerms(CorpusMode.WORD);
	}

	public List<String> getLocations() {
		return getTerms(CorpusMode.LOCATION);
	}

	public List<String> getOrganizations() {
		return getTerms(CorpusMode.ORGANIZATION);
	}

	public List<String> getPersons() {
		return getTerms(CorpusMode.PERSON);
	}
	
	public List<String> getTerms(CorpusMode mode) {
		return expandTerms(mode);
	}

	private List<String> expandTerms(CorpusMode mode) {
		List<String> termStrings = new ArrayList<String>();
		Dictionary dictionary = corpus.getDictionary(mode.ordinal());

		int m = mode.ordinal();
		for (int n = 0; n < termIds[m].length; n++) {
			String term = dictionary.getTerm(termIds[m][n]);
			for (int i = 0; i < termCounts[m][n]; i++) {
				termStrings.add(term);
			}
		}
		
		return termStrings;
	}

	public boolean isEmpty() {
		return termIds == null || termIds[CorpusMode.WORD.ordinal()] == null || termIds[CorpusMode.WORD.ordinal()].length == 0;
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void addTerms(CorpusMode mode, SortedMap<Integer, Integer> terms) {
		int m = mode.ordinal();
		
		int size = terms.size();
		int[] ids = new int[size];
		int[] counts = new int[size];
		
		int n = 0;
		for (Entry<Integer, Integer> entry : terms.entrySet()) {
			
			ids[n] = entry.getKey().intValue();
			counts[n] = entry.getValue().intValue();
			n++;
		}
		
		termIds[m] = ids;
		termCounts[m] = counts;
	}
	
	public int[][] getTermIds() {
		return termIds;
	}

	public int[][] getTermCounts() {
		return termCounts;
	}

	public void setCorpus(CorpusReader corpus) {
		this.corpus = corpus;
	}
}
