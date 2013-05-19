package com.bronzespear.hdpa.coherence;

import java.util.HashSet;
import java.util.Set;

public class Document {
	protected int[] termIds;

	public int[] getTermIds() {
		return termIds;
	}
	
	public Set<Integer> uniqueTermIds() {
		Set<Integer> set = new HashSet<Integer>(termIds.length);
		for (int i = 0; i < termIds.length; i++) {
			set.add(termIds[i]);
		}
		
		return set;
	}
}
