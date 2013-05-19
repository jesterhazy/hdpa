package com.bronzespear.hdpa.coherence.hdpa;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.bronzespear.hdpa.coherence.Corpus;
import com.bronzespear.hdpa.coherence.Document;
import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class HdpaCorpus implements Corpus {
	private static final int WORD_MODE = CorpusMode.WORD.ordinal();
	private CorpusReader corpusReader;

	public HdpaCorpus(CorpusReader corpusReader) {
		this.corpusReader = corpusReader;
	}

	public Iterator iterator() {
		return new Iterator(corpusReader.iterator());
	}

	public void open() throws IOException {
		corpusReader.open();
	}

	public void close() throws IOException {
		corpusReader.close();
	}

	public String getTerm(int id) {
		return corpusReader.getDictionary(WORD_MODE).getTerm(id);
	}
	
	public List<String> getTerms(int[] termIds) {
		String[] terms = new String[termIds.length];
		
		for (int i = 0; i < termIds.length; i++) {
			terms[i] = getTerm(termIds[i]);
		}
		
		return Arrays.asList(terms);
	}
	
	private final class Iterator implements java.util.Iterator<Document> {
		private final CorpusReader.Iterator iterator;
		public Iterator(CorpusReader.Iterator iterator) {
			this.iterator = iterator;
		}
		
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		public Document next() {
			return new HdpaDocument(iterator.next());
		}
		
		public void remove() {
			iterator.remove();
		}
	}
}
