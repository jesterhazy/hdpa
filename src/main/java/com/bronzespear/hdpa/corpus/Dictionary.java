package com.bronzespear.hdpa.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Dictionary {
	private static final Log LOG = LogFactory.getLog(Dictionary.class);
	
	private List<String> terms = new ArrayList<String>();
	private Map<String, Integer> term2id = new HashMap<String, Integer>();
	private boolean reading;
	
	public Integer addTerm(String term) {
		Integer id = term2id.get(term);
		
		if (id == null) {
			id = terms.size();
			terms.add(term);
			term2id.put(term, id);
		}
		
		else if (reading) {
			LOG.warn(String.format("duplicate term %s with index %d at position %d", term, id, terms.size()));
		}
		
		return id;
	}
	
	public void save(File file) throws IOException {
		LOG.info("saving dictionary to: " + file.getAbsolutePath());
		PrintWriter pw = new PrintWriter(file, "UTF-8");
		for (String term : terms) {
			pw.println(term);
		}
		
		pw.close();
		
		LOG.info("done");
	}
	
	public void read(File file) throws IOException {
		reading = true;
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		while ((line = reader.readLine()) != null) {
			addTerm(line);
		}
		
		reader.close();
		LOG.info(String.format("read %d terms", terms.size()));		
		reading = false;
	}

	public String getTerm(Integer id) {
		return terms.get(id);
	}

	public int size() {
		return terms.size();
	}

	public Integer getId(String term) {
		return term2id.get(term);
	}
}
