package com.bronzespear.hdpa.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RestrictedVocabularyDocument extends DocumentDecorator {
	private static final Log LOG = LogFactory.getLog(RestrictedVocabularyDocument.class);

	private Map<CorpusMode, List<String>> allowedMap = new HashMap<CorpusMode, List<String>>();
	private Map<CorpusMode, List<String>> filteredMap = new HashMap<CorpusMode, List<String>>();
	

	public RestrictedVocabularyDocument(Document doc, Map<CorpusMode, List<String>> allowedMap) {
		super(doc);
		this.allowedMap = allowedMap;
	}
	
	@Override
	public boolean isEmpty() {
		return getTerms(CorpusMode.WORD).isEmpty();
	}
	
	@Override
	public List<String> getTerms(CorpusMode mode) {
		if (filteredMap.get(mode) == null) {
			List<String> unfilteredList = super.getTerms(mode);
			List<String> allowedList = allowedMap.get(mode);
			List<String> filteredList = new ArrayList<String>();
			
			if (allowedList.isEmpty()) {
				filteredList.addAll(unfilteredList);
			}
			
			else {
				for (String term : unfilteredList) {
					if (allowedList.contains(term)) {
						filteredList.add(term);
					}
				}
			}
			
			filteredMap.put(mode, filteredList);
			LOG.trace(String.format("retained %d of %d tokens", filteredList.size(), unfilteredList.size()));
		}
		
		return filteredMap.get(mode);
	}
}
