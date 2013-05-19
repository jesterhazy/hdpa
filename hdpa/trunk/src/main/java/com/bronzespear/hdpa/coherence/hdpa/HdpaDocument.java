package com.bronzespear.hdpa.coherence.hdpa;

import com.bronzespear.hdpa.coherence.Document;
import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusMode;

public class HdpaDocument extends Document {
	public HdpaDocument(CorpusDocument document) {
		this.termIds = document.getTermIds()[CorpusMode.WORD.ordinal()];
	}
}
