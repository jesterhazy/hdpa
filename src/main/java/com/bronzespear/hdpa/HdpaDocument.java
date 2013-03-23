package com.bronzespear.hdpa;

import com.bronzespear.hdpa.corpus.CorpusDocument;

public class HdpaDocument {
	int id = -1;
	int[][] termIds;
	int[][] termCounts;
	
	public HdpaDocument(int id, int modes) {
		this(id, new int[modes][], new int[modes][]);
	}
	
	public HdpaDocument(CorpusDocument doc) {
		this(doc.getId(), doc.getTermIds(), doc.getTermCounts());
	}
	
	public HdpaDocument(int id, int[][] termIds, int[][] termCounts) {
		this.id = id;
		this.termIds = termIds;
		this.termCounts = termCounts;
	}

	public int getId() {
		return id;
	}

	public int[][] getTermIds() {
		return termIds;
	}

	public int[][] getTermCounts() {
		return termCounts;
	}

	public int getTotalTermCount() {
		int count = 0;
		
		for (int m = 0; m < termIds.length; m++) {
			count += termIds[m].length;
		}
		
		return count;
	}
}
