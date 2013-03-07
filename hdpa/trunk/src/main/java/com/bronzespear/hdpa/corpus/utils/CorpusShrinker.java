package com.bronzespear.hdpa.corpus.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;
import com.bronzespear.hdpa.corpus.CorpusWriter;
import com.bronzespear.hdpa.corpus.Document;
import com.bronzespear.hdpa.corpus.RestrictedVocabularyDocument;

public class CorpusShrinker {
	private static final Log LOG = LogFactory.getLog(CorpusShrinker.class);

	private CorpusReader source;
	private CorpusWriter target;
	private int[] targetTermCounts;
	private int targetDocumentCount;
	private int sourceDocsSeen;
	
	public CorpusShrinker(File source, File target, int docs, int[] terms) {
		this(new CorpusReader(source), new CorpusWriter(target), docs, terms);
	}
	
	public CorpusShrinker(CorpusReader source, CorpusWriter target, int docs, int[] terms) {
		this.source = source;
		this.target = target;
		this.targetTermCounts = terms;
		this.targetDocumentCount = docs;
	}

	public void shrink() throws IOException {
		LOG.info(String.format("reducing source corpus %s to target corpus %s with %d documents and %s terms", 
				source.getBasedir().getName(), target.getBasedir().getName(), targetDocumentCount, Arrays.toString(targetTermCounts)));
		source.open();
		target.open();
		
		Map<CorpusMode, List<String>> topTerms = new HashMap<CorpusMode, List<String>>();
		for (CorpusMode mode : CorpusMode.values()) {
			topTerms.put(mode, source.topTerms(mode, targetTermCounts[mode.ordinal()]));
		}	
	
		for (Document doc : source) {
			if (targetDocumentCount == 0 || includeCurrentDocument()) {
				RestrictedVocabularyDocument shrunk = new RestrictedVocabularyDocument(doc, topTerms);
//              opportunity to chop more stop words or patterns
//				FilteredDocument shrunk = new FilteredDocument(doc); 
				if (!shrunk.isEmpty()) {
					target.addDocument(shrunk);
				}
			}
			
			if (targetDocumentCount != 0 && target.getDocumentCount() >= targetDocumentCount) {
				break;
			}
			
			if (sourceDocsSeen % 10000 == 0) {
				LOG.debug(String.format("read %d source documents, kept %d target documents", sourceDocsSeen, target.getDocumentCount()));
			}
		}
		
		source.close();
		target.close();
		
		LOG.info("corpus reduction complete");
		LOG.info(String.format("%-15s     %25s     %25s", "", "source", "target"));
		LOG.info(String.format("%-15s     %25s     %25s", "path", source.getBasedir().getName(), target.getBasedir().getName()));
		LOG.info(String.format("%-15s     %25d     %25d", "docs", source.getDocumentCount(), target.getDocumentCount()));
		for (CorpusMode mode : CorpusMode.values()) {
			LOG.info(String.format("%-15s     %25d     %25d", 
					mode.name().toLowerCase(), 
					source.getTermCount(mode.ordinal()), 
					target.getTermCount(mode.ordinal())));
		}
	}
	
	private boolean includeCurrentDocument() {
		boolean include = false;
		int needed = targetDocumentCount - target.getDocumentCount();
		
		if (needed > 0) {
			int remaining = source.getDocumentCount() - sourceDocsSeen;
			
			if (needed < remaining) {
				include = Math.random() < (double) needed / remaining;
			}
			
			else {				
				include = true;
			}
		}
		
		sourceDocsSeen++;
		return include;
	}

	/**
	 * Usage:
	 * 
	 * <pre>java CorpusShrinker source-path doc-limit term-limit</pre>
	 * 
	 * or
	 *  
	 * <pre>java CorpusShrinker source-path doc-limit word-limit pers-limit org-limit loc-limit</pre>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File source = new File(args[0]);
		
		int docLimit = args.length < 1 ? 100 : Integer.parseInt(args[1]);
				
		int[] termLimits = new int[4];
	
		if(args.length == 6) {
			for (int i = 0; i < termLimits.length; i++) {
				termLimits[i] = Integer.parseInt(args[i + 2]);
			}
		}
		
		else if (args.length == 3) {
			Arrays.fill(termLimits, Integer.parseInt(args[2]));
		}
		
		else {
			Arrays.fill(termLimits, 2000);
		}
		
		String basename = source.getName().replaceAll("-\\d+x\\d+$", "");
		String filename = docLimit > 0 ? String.format("%s-d%d-t%d", basename, docLimit, termLimits[0]) : String.format("%s-t%d", basename, termLimits[0]);
		
		File target = new File(source.getAbsoluteFile().getParentFile(), filename);
		
		if (target.exists()) {
			target = new File(source.getAbsoluteFile().getParentFile(), String.format("%s-%2$tF-%2$tH%2$tM%2$tS", filename, new Date()));			
		}
		
		CorpusShrinker app = new CorpusShrinker(source, target, docLimit, termLimits);
		app.shrink();
	}
}
