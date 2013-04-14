package com.bronzespear.hdpa.solr;

import java.io.File;
import java.io.IOException;

import com.bronzespear.hdpa.Hdpa;
import com.bronzespear.hdpa.HdpaDocument;
import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class HdpaTopicStrategy implements TopicStrategy {
	private final Hdpa hdpa;

	public HdpaTopicStrategy(CorpusReader corpus, File modelFile) throws IOException {
		this.hdpa = new Hdpa(corpus);
		hdpa.loadParameters(modelFile);
	}

	public double[] getTopics(CorpusDocument document) {
		HdpaDocument hdpaDocument = new HdpaDocument(document);
		double[] topicWeights = hdpa.assignTopics(hdpaDocument);
		return topicWeights;
	}
	
	public void close() {
		// do nothing.
	}
}
