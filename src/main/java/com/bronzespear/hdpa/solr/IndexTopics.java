package com.bronzespear.hdpa.solr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.bronzespear.hdpa.Hdpa;
import com.bronzespear.hdpa.HdpaUtils;
import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class IndexTopics {
	
	private static final String SOLR_URL = "http://localhost:8983/solr/hdpa";
	private static final Log LOG = LogFactory.getLog(IndexTopics.class);
	private static final int SOLR_BATCH_SIZE = 300;
	private static final int SOLR_THREAD_COUNT = 1;
	
	private File modelFile;
	private File corpusFile;
	private String corpusName;
	private Hdpa hdpa;
	
	public IndexTopics(File modelFile, File corpusFile) throws IOException {
		this.modelFile = modelFile;
		this.corpusFile = corpusFile;
		this.corpusName = corpusFile.getName().replaceAll("-.*$", "");
	}
	
	private void run() throws Exception  {
		ConcurrentUpdateSolrServer server = new ConcurrentUpdateSolrServer(SOLR_URL, SOLR_BATCH_SIZE, SOLR_THREAD_COUNT);
		
		CorpusReader corpus = new CorpusReader(corpusFile);
		corpus.open();
		
		hdpa = new Hdpa(corpus);
		hdpa.loadParameters(modelFile);
				
		int topics = hdpa.getK();
		for (int topic = 0; topic < topics; topic++) {
			SolrInputDocument solrDocument = createSolrDocument(topic);
			server.add(solrDocument);
			
			if (topic > 0 && topic % 100 == 0) {
				LOG.info(String.format("added %d topics", topic));
			}
		}
						
		LOG.info(String.format("finished adding %d documents", topics));
		LOG.info("waiting for solr");
		server.blockUntilFinished();
		
		LOG.info("solr commit");
		server.commit();

//		memory intensive for large index - best to do in solr admin ui 
//		after ensuring enough memory is available		
//		server.optimize(); 

		corpus.close();
	}
	
	private SolrInputDocument createSolrDocument(int topic) {
		SolrInputDocument solr = new SolrInputDocument();
		solr.addField("id", corpusName + "-t" + topic);
		solr.addField("corpus", corpusName);
		solr.addField("title", "Topic " + topic);

		int limit = 25;
		for (CorpusMode mode : CorpusMode.values()) {
			List<String> terms = hdpa.topTerms(topic, mode.ordinal(), limit);
			solr.addField(mode.name().toLowerCase(), HdpaUtils.join(",", HdpaUtils.quote(terms)));
		}
		
		return solr;
	}
	
	public static void main(String[] args) throws Exception {		
		File modelFile = null;
		File corpusFile = null;
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				modelFile = new File(args[i]);
				break;
			default:
			}
		}
		
		if (!modelFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + args[0]);
		}
		
		if (corpusFile == null) {
			corpusFile = HdpaUtils.getCorpusForModel(modelFile);
		}
		
		if (!corpusFile.exists()) {
			throw new IllegalArgumentException("file does not exist: " + corpusFile.getPath());
		}
		
		IndexTopics app = new IndexTopics(modelFile, corpusFile);
		app.run();
	}
}
