package com.bronzespear.hdpa.corpus.nyt;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.CorpusWriter;
import com.bronzespear.hdpa.corpus.Document;
import com.bronzespear.hdpa.corpus.FilteredDocument;
import com.bronzespear.hdpa.corpus.LemmatizedDocument;
import com.bronzespear.hdpa.corpus.utils.file.FileIterator;
import com.bronzespear.hdpa.corpus.utils.file.TGZFileIterator;


public class Nyt2Corpus {
	private static final Log LOG = LogFactory.getLog(Nyt2Corpus.class);

	public static void main(String[] args) throws Exception {
		
		String source = "/Users/jonathan/Documents/6 uathabasca/project/ldc data/nyt_corpus/data";
		String target = "nyt";
		
		switch (args.length) {
		case 2:
			source = args[0];
			target = args[1];
			break;
		case 1:
			target = args[0];
			break;
		default:
			break;
		}
		
		File root = new File(source);
		CorpusWriter corpus = new CorpusWriter(target);
		corpus.open();
		
		Iterator<File> fileIter = new FileIterator(root);

		while (fileIter.hasNext()) {
			File f = fileIter.next();
			
			TGZFileIterator tgzIter = new TGZFileIterator(f);
			
			while (tgzIter.hasNext())  {
				File f2 = tgzIter.next();				
				Document doc = new NYTDocument(f2);
				
				// lemmatize
				doc = new LemmatizedDocument(doc);
				
				// regex and stopword filters
				doc = new FilteredDocument(doc);
				
				if (doc.getWords().isEmpty()) {
					LOG.debug("skipping empty document: " + tgzIter.currentEntryName());
				}
				else {
					corpus.addDocument(doc);
				}
				
				if (corpus.getDocumentCount() % 10000 == 0) {
					LOG.info(String.format("processed %d documents", corpus.getDocumentCount()));
				}
				
			}
		}
		
		corpus.close();
	}	
}
