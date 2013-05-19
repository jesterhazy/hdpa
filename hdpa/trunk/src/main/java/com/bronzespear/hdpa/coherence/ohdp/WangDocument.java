package com.bronzespear.hdpa.coherence.ohdp;

import com.bronzespear.hdpa.coherence.Document;

public class WangDocument extends Document {
	public WangDocument(String data) {
		String[] terms = data.trim().split(" ");
		
		termIds = new int[terms.length - 1];
		
		int i = 0;
		for (String term : terms) {
			// skip first "term" (nnz)
			if (i > 0) {
				String[] termParts = term.split(":");
				termIds[i - 1] = Integer.parseInt(termParts[0]);
			}
			
			i++;
		}
	}
}
