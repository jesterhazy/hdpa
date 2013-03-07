package com.bronzespear.hdpa.corpus.utils.matrix;

public class MMColumn implements Comparable<MMColumn> {
	public int id;
	public int value;
	
	public MMColumn(int id, int value) {
		this.id = id;
		this.value = value;
	}

	public int compareTo(MMColumn other) {
		return id - other.id;
	}
}
