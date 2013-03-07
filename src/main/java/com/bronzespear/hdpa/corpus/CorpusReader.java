package com.bronzespear.hdpa.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.utils.matrix.MMColumn;
import com.bronzespear.hdpa.corpus.utils.matrix.MMReader;
import com.bronzespear.hdpa.corpus.utils.matrix.MMRow;

public class CorpusReader extends Corpus implements Iterable<CorpusDocument> {
	private static final Log LOG = LogFactory.getLog(CorpusReader.class);

	private BufferedReader titleReader;
	private BufferedReader textReader;
	private MMReader[] mmReaders;
	private int currentDoc;
	private boolean open;
	
	private MMRow[] mmRows = new MMRow[CorpusMode.SIZE];

	public final class Iterator implements java.util.Iterator<CorpusDocument> {
		
		public boolean hasNext() {
			return currentDoc < getDocumentCount();
		}

		public CorpusDocument next() {
			CorpusDocument doc = null;
			
			try {
				doc = new CorpusDocument(CorpusReader.this);
				
				for (CorpusMode mode : CorpusMode.values()) {
					int m = mode.ordinal();
					
					if (CorpusMode.WORD == mode) {
						mmRows[m] = mmReaders[m].readRow();
						doc.setId(mmRows[m].id);
						
						String title = readTitle();
						doc.setTitle(title);
						
						String text = readText();
						doc.setText(text);
						
						doc.addTerms(CorpusMode.WORD, extractTerms(mmRows[m]));
					}			
					
					else {
						// if row for this mode has not been read or is from older document... 
						if (mmRows[m] == null || mmRows[m].id < doc.getId()) {
							mmRows[m] = mmReaders[m].readRow();
						}
						
						// if the row we just read is for the current document...
						if (mmRows[m] != null && mmRows[m].id == doc.getId()) {
							doc.addTerms(CorpusMode.values()[m], extractTerms(mmRows[m]));
						}
					}
				}
			}
			
			catch (IOException e) {
				doc = null;
				LOG.error(e, e);
			}
			
			currentDoc++;
			return doc;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public CorpusReader(String path) {
		this(new File(path));
	}
	
	public CorpusReader(File basedir) {
		super(basedir);
	}

	public Iterator iterator() {
		return new Iterator();
	}
	
	public void open() throws IOException {
		open(true);
	}

	private void open(boolean readDictionaries)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		
		if (!open) {
			open = true;
			LOG.info("opening corpus at " + getBasedir().getAbsolutePath());
			
			currentDoc = 0;
			
			titleReader = new BufferedReader(new InputStreamReader(new FileInputStream(titleFile), "UTF-8"));
			textReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), "UTF-8"));
			
			mmReaders = new MMReader[4];
			for (CorpusMode mode : CorpusMode.values()) {
				int m = mode.ordinal();
				
				mmReaders[m] = new MMReader(mmFiles[m]);
				
				if (CorpusMode.WORD == mode) {
					setDocumentCount(mmReaders[m].getRows());
				}
				
				if (readDictionaries) {
					dictionaries[m].read(dictionaryFiles[m]);
				}
			}
		}
	}
	
	public void reopen() throws IOException {
		close();
		open(false);
	}
	
	public void close() throws IOException {
		LOG.info("closing corpus at " + getBasedir().getAbsolutePath());

		titleReader.close();
		textReader.close();
		
		for (CorpusMode mode : CorpusMode.values()) {
			int m = mode.ordinal();
			mmReaders[m].close();
		}
		
		open = false;
	}
	
	private String readTitle() throws IOException {
		return titleReader.readLine();
	}
	
	private String readText() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		while ((line = textReader.readLine()) != null) {
			if (line.trim().matches("^<doc id=\"\\d+\">.*$")) {
				// skip
			}
			
			else if ("</doc>".equals(line.trim())) {
				break;
			}
			
			else {
				sb.append(line.trim());
				sb.append("\n");
			}
		}
		
		return sb.toString().trim();
	}
	
	private SortedMap<Integer, Integer> extractTerms(MMRow row) {
		SortedMap<Integer, Integer> terms = new TreeMap<Integer, Integer>();
		for (MMColumn col : row.columns) {
			terms.put(Integer.valueOf(col.id), Integer.valueOf(col.value));
		}
		
		return terms;
	}
}
