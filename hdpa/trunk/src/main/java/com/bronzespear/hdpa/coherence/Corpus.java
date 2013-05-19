package com.bronzespear.hdpa.coherence;

import java.io.IOException;
import java.util.List;

public interface Corpus extends Iterable<Document>{
	void open() throws IOException;
	void close() throws IOException;
	String getTerm(int i);
	List<String> getTerms(int[] topTermIds);
}
