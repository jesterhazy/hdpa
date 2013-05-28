package com.bronzespear.hdpa.coherence.hdpa;

import java.io.File;
import java.io.IOException;

import com.bronzespear.hdpa.Hdpa;
import com.bronzespear.hdpa.coherence.Model;
import com.bronzespear.hdpa.corpus.CorpusMode;

public class HdpaModel extends Model {
	private Hdpa h;
	private File modelFile;

	public HdpaModel(Hdpa h, File modelFile) {
		this.h = h;
		this.modelFile = modelFile;
	}

	public void load() throws IOException {
		h.loadParameters(modelFile);
		topTermIds = h.topTermIds(CorpusMode.WORD.ordinal(), termLimit);
		topicPrevalence = h.getTopicWeights();
		numberOfTopics = h.getK();
	}
}
