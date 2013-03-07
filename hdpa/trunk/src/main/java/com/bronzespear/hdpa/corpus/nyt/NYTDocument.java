package com.bronzespear.hdpa.corpus.nyt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.DocumentBase;
import com.nytlabs.corpus.NYTCorpusDocument;
import com.nytlabs.corpus.NYTCorpusDocumentParser;

public class NYTDocument extends DocumentBase {
	private static final Log LOG = LogFactory.getLog(NYTDocument.class);

	public NYTDocument(File file) {
		this(new NYTCorpusDocumentParser().parseNYTCorpusDocumentFromFile(file, false));
	}

	public NYTDocument(NYTCorpusDocument nyt) {
		if (nyt.getHeadline() != null) {
			setTitle(nyt.getHeadline());
		} 
		
		else if (nyt.getOnlineHeadline() != null) {
			setTitle(nyt.getOnlineHeadline());
		}
		
		else {
			setTitle("no title");
		}
		
		String text = nyt.getBody();
		if (text != null) {
			if (nyt.getLeadParagraph() != null && nyt.getLeadParagraph().length() < text.length()) {
				text = text.substring(nyt.getLeadParagraph().length());
				setText(text.trim());
			}
		}
		
		addPersons(cleanEntityTerms(nyt.getPeople(), nyt.getOnlinePeople()));
		addOrganizations(cleanEntityTerms(nyt.getOrganizations(), nyt.getOnlineOrganizations()));
		addLocations(cleanEntityTerms(nyt.getLocations(), nyt.getOnlineLocations()));
	}

	private List<String> cleanEntityTerms(List<String> indexTerms, List<String> onlineTerms) {
		List<String> combined = new ArrayList<String>();
		combined.addAll(indexTerms);
		combined.addAll(onlineTerms);
		
		Set<String> unique = new HashSet<String>();
		for (String term : combined) {
			if (term.length() < 65) {
				term = term.toLowerCase().replaceAll("\\s", " ")
						.replaceAll("[<>]", " ")
						.replace("<p>", "").replace("<br>", "")
						.replaceAll("\\s+", " ")
						.trim();
				if (!term.isEmpty()) {
					unique.add(term);
				} 
			}
		}
		
		if (combined.size() != unique.size()) {
			LOG.debug(String.format("removed %d entity terms", combined.size() - unique.size()));
		}
		
		return new ArrayList<String>(unique);
	}
}
