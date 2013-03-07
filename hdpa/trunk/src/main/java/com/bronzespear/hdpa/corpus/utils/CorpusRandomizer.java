package com.bronzespear.hdpa.corpus.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.RandomDataGenerator;

import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusReader;
import com.bronzespear.hdpa.corpus.CorpusWriter;

public class CorpusRandomizer {
	private static final Log LOG = LogFactory.getLog(CorpusRandomizer.class);

	private CorpusReader source;
	private CorpusWriter target;
	
	public CorpusRandomizer(File sourceFile, File targetFile) {
		this(new CorpusReader(sourceFile), new CorpusWriter(targetFile));
	}

	public CorpusRandomizer(CorpusReader source, CorpusWriter target) {
		this.source = source;
		this.target = target;		
	}

	public static void main(String[] args) throws Exception {
		File sourceFile = null;
		
		if (args.length >= 1) {
			sourceFile = new File(args[0]);
			
			if (!sourceFile.exists()) {
				throw new IllegalArgumentException("file does not exist: " + args[0]);
			}
		}
		
		File targetFile = new File(sourceFile.getParentFile(), sourceFile.getName() + "-random");
		
		CorpusRandomizer app = new CorpusRandomizer(sourceFile, targetFile);
		app.randomize();
	}

	private void randomize() throws IOException, ClassNotFoundException {
		LOG.info(String.format("rewriting source corpus %s to target corpus %s in random order", 
				source.getBasedir().getName(), target.getBasedir().getName()));
		
		
		File out = new File(System.getProperty("java.io.tmpdir"), "CorpusRandomizer-" + System.currentTimeMillis());
		out.mkdirs();
		out.deleteOnExit();
		
		LOG.info("writing tmp files to " + out.getAbsolutePath());
		
		source.open();		
		int documentCount = source.getDocumentCount();
		int[] randomIds = new RandomDataGenerator().nextPermutation(documentCount, documentCount);
		
		LOG.info("spooling source corpus to disk");
		int i = 0;
		for (CorpusDocument document : source) {
			String fname = filenameForIndex(randomIds[i]);
			File f = new File(out, fname);
			f.deleteOnExit();
			
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(document);
			oos.close();
			i++;
			
			if (i % 1000 == 0) {
				LOG.info(String.format("spooled %d / %d documents", i, documentCount));
			}
		}
			
		randomIds = null;
		
		LOG.info("done spooling source corpus to disk");
		
		LOG.info("adding documents to target corpus");
		target.open();
		for (i = 0; i < documentCount; i++) {
			File f = new File(out, filenameForIndex(i));
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
			CorpusDocument document = (CorpusDocument) ois.readObject();
			document.setCorpus(source); // needed for dictionary expansion
			target.addDocument(document);
			
			if (i % 1000 == 0) {
				LOG.info(String.format("added %d / %d documents", i, documentCount));
			}
		}
		
		target.close();
		source.close();
	}

	private String filenameForIndex(int i) {
		return String.format("%010d.obj", i);
	}
}
