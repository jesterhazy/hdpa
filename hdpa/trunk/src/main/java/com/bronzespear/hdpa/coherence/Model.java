package com.bronzespear.hdpa.coherence;

import java.io.IOException;

public class Model {	
	protected int termLimit;
	protected int numberOfTopics;
	protected int[][] topTermIds;
	protected double[] topicPrevalence;
	
	public void load() throws IOException {
		// default
	}
	
	public void setTermLimit(int numberOfTerms) {
		this.termLimit = numberOfTerms;
	}

	public int[][] topTermIds() {
		return topTermIds;
	}

	public int[] topTermIds(int topic) {
		return topTermIds[topic];
	}
	
	public int numberOfTopics() {
		return numberOfTopics;
	}

	public double[] getTopicPrevalence() {
		return topicPrevalence;
	}
}
