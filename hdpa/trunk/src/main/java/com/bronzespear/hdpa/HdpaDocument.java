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
		
		for (int m = 0; m < getModeCount(); m++) {
			count += MathUtils.sum(termCounts[m]);
		}
		
		return count;
	}
	
	public int[][] getExpandedTermIds() {
		int modes = getModeCount();
		int[][] expanded = new int[modes][];
		
		for (int m = 0; m < modes; m++) {
			int length = MathUtils.sum(termCounts[m]);
			expanded[m] = new int[length];
			
			int j = 0;
			for (int i = 0; i < termIds[m].length; i++) {
				int id = termIds[m][i];
				int count = termCounts[m][i];
				
				for (int k = 0; k < count; k++) {
					expanded[m][j] = id;
					j++;
				}
			}
		}
		
		return expanded;
	}

	public int getModeCount() {
		return termIds.length;
	}
}
