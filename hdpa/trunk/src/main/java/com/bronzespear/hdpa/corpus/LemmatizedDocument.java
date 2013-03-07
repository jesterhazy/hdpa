package com.bronzespear.hdpa.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LemmatizedDocument extends DocumentDecorator {
	private static final Log LOG = LogFactory.getLog(LemmatizedDocument.class);
	private static StanfordCoreNLP pipeline;
	
	static {
		LOG.info("setting up NLP engine");
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		LOG.info("done");
	}
	
	private List<String> words = new ArrayList<String>();
	private boolean initialized;
	
	public LemmatizedDocument(Document doc) {
		super(doc);
	}
	
	public List<String> getWords() {
		if (!initialized) {
			lemmatize();
		}
		
		return words;
	}

	private void lemmatize() {		
		LOG.debug("lemmatizing document");
		initialized = true;
		
		try {
			Annotation doc = new Annotation(getText());
			pipeline.annotate(doc);
			List<CoreMap> sentences = doc.get(SentencesAnnotation.class);

			if (sentences != null) {
				for (CoreMap sentence : sentences) {
					if (sentence != null) {
						for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
							String lemma = token.get(LemmaAnnotation.class);
							if (lemma != null) {
								words.add(lemma.toLowerCase());
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			words.clear();
			LOG.warn("error lemmatizing doc: " + getDocument().getTitle());
		}
		
		LOG.debug("done lemmatizing");
	}
}
