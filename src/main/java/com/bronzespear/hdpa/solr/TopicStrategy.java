package com.bronzespear.hdpa.solr;

import com.bronzespear.hdpa.corpus.CorpusDocument;

public interface TopicStrategy {
	double[] getTopics(CorpusDocument document);

	void close();
}
