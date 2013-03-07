package com.bronzespear.hdpa.corpus.utils.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MMRow {
	public int id;
	public List<MMColumn> columns = new ArrayList<MMColumn>();
	
	public MMRow(int id) {
		this.id = id;
	}
	
	public MMRow(int id, Map<Integer, Integer> values) {
		this(id);
		
		for (Entry<Integer, Integer> entry : values.entrySet()) {
			addColumn(entry.getKey(), entry.getValue());
		}
		
		Collections.sort(columns);
	}

	public void addColumn(int id, int value) {
		addColumn(new MMColumn(id, value));
	}

	public void addColumn(MMColumn column) {
		columns.add(column);
	}
}
