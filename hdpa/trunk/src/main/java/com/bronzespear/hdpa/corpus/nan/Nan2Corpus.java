package com.bronzespear.hdpa.corpus.nan;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.CorpusWriter;
import com.bronzespear.hdpa.corpus.Document;
import com.bronzespear.hdpa.corpus.FilteredDocument;
import com.bronzespear.hdpa.corpus.LemmaNerDocument;
import com.bronzespear.hdpa.corpus.utils.file.FileIterator;

public class Nan2Corpus {
	private static final Log LOG = LogFactory.getLog(Nan2Corpus.class);

	public static void main(String[] args) throws Exception {
		
		String source = "/Users/jonathan/Documents/6 uathabasca/project/ldc data/nanc_pub/data";
		String target = "nan";
		
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
		
		Iterator<File> fileIterator = new FileIterator(root);
		
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();			
			
			LOG.info("reading file: " + file.getAbsolutePath());
			
			NanIterator np = new NanIterator(file);
			int docNumber = 1;
			while (np.hasNext()) {

				Document doc = np.next();
				
				doc = new LemmaNerDocument(doc);
				doc = new FilteredDocument(doc);
				
				if (doc.getWords().isEmpty()) {
					LOG.debug(String.format("skipping empty document: doc %d in file %s", docNumber, file.getAbsolutePath()));
				}
				else {
					corpus.addDocument(doc);
				}
				
				if (corpus.getDocumentCount() % 10000 == 0) {
					LOG.info(String.format("processed %d documents", corpus.getDocumentCount()));
				}
				
				docNumber++;
			}
		}
		
		corpus.close();
	}
}
