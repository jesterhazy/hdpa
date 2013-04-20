package com.bronzespear.hdpa.solr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.bronzespear.hdpa.HdpaUtils;
import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class IndexDocuments {
	
	private static final String SOLR_URL = "http://localhost:8983/solr/hdpa";
	private static final Log LOG = LogFactory.getLog(IndexDocuments.class);
	private static final int SOLR_BATCH_SIZE = 1000;
	private static final int SOLR_THREAD_COUNT = 5;
	
	private CorpusReader corpus;
	private TopicStrategy topicStrategy;
	
	
	public IndexDocuments(File file, File corpusFile) throws IOException {
		this.corpus = new CorpusReader(corpusFile);	
		
		if (isModelFile(file)) {
			topicStrategy = new HdpaTopicStrategy(corpus, file);
		}
		
		else if (isTopicsFile(file)) {
			topicStrategy = new PreassignedTopicStrategy(file);
		}
		
		else {
			throw new IllegalArgumentException("can't determine type of input file: " + file.getName());
		}
	}
	
	private void run() throws Exception  {
		ConcurrentUpdateSolrServer server = new ConcurrentUpdateSolrServer(SOLR_URL, SOLR_BATCH_SIZE, SOLR_THREAD_COUNT);		
		
		String corpusName = corpus.getBasedir().getName().replaceAll("-.*$", "");
		corpus.open();
		
		int documentCount = 0;
		for (CorpusDocument corpusDocument : corpus) {
			double[] topicWeights = topicStrategy.getTopics(corpusDocument);
			
			if (topicWeights != null) {
				SolrInputDocument solrDocument = createSolrDocument(corpusName, corpusDocument, topicWeights);
				
				server.add(solrDocument);
				documentCount++;
			}
			
			if (documentCount % 1000 == 0) {
				LOG.info(String.format("queued %d documents", documentCount));
			}
			
			if (documentCount % 5000 == 0) {
				LOG.info("solr commit");
				server.commit();	
			}
			
			if (documentCount % 50000 == 0) {
				server.blockUntilFinished();
			}
		}
		
		LOG.info(String.format("finished adding %d documents", documentCount));
		LOG.info("waiting for solr");	
		server.blockUntilFinished();
		
		LOG.info("solr commit");
		server.commit();
		
//		memory intensive for large index - best to do in solr admin ui 
//		after ensuring enough memory is available		
//		server.optimize(); 
		
		topicStrategy.close();
		corpus.close();
	}

	public static void main(String[] args) throws Exception {		
		File file = null;
		File corpusFile = null;
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				file = new File(args[i]);
				break;
			case 1:
				corpusFile = new File(args[i]);
				break;
			default:
			}
		}
		
		if (!file.exists()) {
			throw new IllegalArgumentException("file does not exist: " + args[0]);
		}
		
		if (corpusFile == null) {
			corpusFile = HdpaUtils.getCorpusForModel(file);
		}
		
		if (!corpusFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + corpusFile.getPath());
		}
		
		IndexDocuments app = new IndexDocuments(file, corpusFile);
		app.run();
	}

	private boolean isModelFile(File file) {
		return file.getName().matches("^(\\d+|final)\\.csv$");
	}
	
	private boolean isTopicsFile(File file) {
		return file.getName().matches("^doctopics-.*\\.csv$");
	}

	private SolrInputDocument createSolrDocument(String corpusName,
			CorpusDocument doc, double[] topicWeights) {
		
		SolrInputDocument solr = new SolrInputDocument();
		
		solr.addField("id", corpusName + "-" + doc.getId());
		solr.addField("corpus", corpusName);
		solr.addField("title", doc.getTitle());
		solr.addField("fulltext", doc.getText());
		
		for (int i = 0; i < topicWeights.length; i++) {
			solr.addField("topic_" + i, topicWeights[i]);
		}
		
		return solr;
	}
}
