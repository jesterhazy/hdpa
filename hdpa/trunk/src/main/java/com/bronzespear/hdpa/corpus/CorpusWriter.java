package com.bronzespear.hdpa.corpus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.utils.matrix.MMRow;
import com.bronzespear.hdpa.corpus.utils.matrix.MMWriter;

public class CorpusWriter extends Corpus {
	private static final Log LOG = LogFactory.getLog(CorpusWriter.class);
	
	private MMWriter mmWriters[];
	private PrintWriter titleWriter;
	private PrintWriter textWriter;
	
	public CorpusWriter(String path) {
		this(new File(path));
	}
	
	public CorpusWriter(File basedir) {
		super(basedir);
	}
	
	public void addDocument(Document doc) {
		if (!doc.isEmpty()) {
			doc.setId(getDocumentCount());
			addTitle(doc);
			addText(doc);
			addTerms(doc);
			incrementDocumentCount();
		}
	}
	
	private void addTitle(Document doc) {
		titleWriter.println(doc.getTitle());
	}
	
	private void addText(Document doc) {
		textWriter.println("<doc id=\"" + doc.getId() + "\">");
		textWriter.println(doc.getText().trim());
		textWriter.println("</doc>");
	}

	public void open() throws IOException {
		LOG.info("initializing corpus at " + getBasedir().getAbsolutePath());
		getBasedir().mkdirs();
		
		mmWriters = new MMWriter[CorpusMode.SIZE];
		for (CorpusMode mode : CorpusMode.values()) {
			int m = mode.ordinal();
			mmWriters[m] = new MMWriter(mmFiles[m]);
			mmWriters[m].open();
		}
		
		titleWriter = new PrintWriter(titleFile, "UTF-8");
		textWriter = new PrintWriter(textFile, "UTF-8");
	}
	
	public void close() throws IOException {
		LOG.info("finalizing corpus at " + getBasedir().getAbsolutePath());

		titleWriter.close();
		textWriter.close();
		
		for (CorpusMode mode : CorpusMode.values()) {
			int m = mode.ordinal();
			mmWriters[m].close();
			dictionaries[m].save(dictionaryFiles[m]);
		}
		
		updateTfidfData();
		LOG.info("done finalizing corpus");
	}
	
	private void addTerms(Document doc) {
		for (CorpusMode mode : CorpusMode.values()) {
			addTerms(mode, doc.getTerms(mode));
		}
	}
	
	private void addTerms(CorpusMode mode, Collection<String> terms) {
		if (!terms.isEmpty()) {
			int m = mode.ordinal();
			Map<Integer, Integer> bow = terms2bow(getDictionary(m), terms);
			MMRow row = new MMRow(getDocumentCount(), bow);
			mmWriters[m].appendRow(row);
			mmWriters[m].flush();
		}
	}

	private Map<Integer, Integer> terms2bow(Dictionary dictionary, Collection<String> terms) {
		Map<Integer, Integer> bow = new HashMap<Integer, Integer>();
		
		for (String term : terms) {
			Integer id = dictionary.addTerm(term);
			
			Integer count = bow.get(id);
			if (count == null) {
				bow.put(id, Integer.valueOf(1));
			}
			
			else {
				bow.put(id, Integer.valueOf(count.intValue() + 1));
			}
		}
		
		return bow;
	}
}
