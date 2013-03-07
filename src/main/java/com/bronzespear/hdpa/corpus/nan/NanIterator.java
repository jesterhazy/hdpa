package com.bronzespear.hdpa.corpus.nan;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.Document;
import com.bronzespear.hdpa.corpus.DocumentBase;

public class NanIterator implements Iterator<Document> {
	private static final String END_TAG_HEADLINE = "</HEADLINE>";
	private static final String END_TAG_TEXT = "</TEXT>";
	private static final String END_TAG_DOC = "</DOC>";
	private static final String START_TAG_DOC = "<DOC>";
	private static final String START_TAG_HEADLINE = "<HEADLINE>";
	private static final String START_TAG_TEXT = "<TEXT>";
	private static final Log LOG = LogFactory.getLog(NanIterator.class);
	private File file;
	private BufferedReader reader;
	private Document next;
	
	public NanIterator(File file) throws IOException {
		this.file = file;
		open();
		readNext();
	}
	
	private void open() throws IOException {
		LOG.debug("opening file: " + file.getAbsolutePath());
		reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				new BufferedInputStream(new FileInputStream(file))), Charset.forName("MacRoman")));
	}
	
	private void readNext() {
		String line = null;
		DocumentBase doc = null;
		String tag = null;
		StringBuilder text = null;
		
		try {
			while((line = reader.readLine()) != null) {
				if (line.equals(START_TAG_DOC)) {
					doc = new DocumentBase();
				}
				
				else if (line.equals(END_TAG_DOC)) {
					if (doc != null && doc.getText() != null) {
						next = doc;
					}
					
					break;
				}
				
				// start tag
				else if (line.equals(START_TAG_TEXT) || line.equals(START_TAG_HEADLINE)) {
					tag = line;
					text = new StringBuilder();
				}
				
				else if (line.equals(END_TAG_TEXT) || line.equals(END_TAG_HEADLINE)) {
					if (doc != null && text != null && text.length() > 0) {
						
						if (START_TAG_HEADLINE.equals(tag)) {
							doc.setTitle(text.toString().trim());
						}
						
						else if (START_TAG_TEXT.equals(tag)) {
							doc.setText(text.toString().trim());
						}
						
						text = null;
						tag = null;
					}
				}
				
				else if (text != null) {
					String clean = clean(tag, line);
					if (!clean.isEmpty()) {
						text.append(clean);
					}
				}
			}
		} catch (IOException e) {
			LOG.error("error reading file: " + file.getAbsolutePath());
			doc = null;
		}
		
		next = doc;
	}

	private String clean(String tag, String line) {
		String clean = null;
		if (START_TAG_TEXT.equals(tag) && line.matches("^\\s*<p>\\s*$")) {
			clean = "\n";
		}
		
		else {
			clean = line
					.replace("<p>", " ")
					.replace("``", "\"")
					.replace("''", "\"")
					.replace("`", "'")
					.replaceAll("^ *&MD; ", "")
					.replaceAll(" +&MD; +", " -- ")
					.replaceAll(" *&QL; *$", "");
			
			if (START_TAG_HEADLINE.equals(tag)) {
				clean = clean
					.replace("\n", " ")
					.replaceAll("^ *&UR; *", "");
			}
			
			else if (START_TAG_TEXT.equals(tag)) {
				clean = clean.replaceAll("^ *&UR; .*$", "");
			}
			
			clean = clean.trim() + " ";
		}		
		
		return clean;
	}

	public boolean hasNext() {
		return next != null;
	}

	public Document next() {
		Document doc = next;
		readNext();
		return doc;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
