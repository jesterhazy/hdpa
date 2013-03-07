package com.bronzespear.hdpa.corpus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LemmaNerDocument extends DocumentDecorator {
	private static final Log LOG = LogFactory.getLog(LemmaNerDocument.class);
	private static final String NE_TYPE_LOCATION = "LOCATION";
	private static final String NE_TYPE_ORGANIZATION = "ORGANIZATION";
	private static final String NE_TYPE_PERSON = "PERSON";
	private static final List<String> TARGET_ENTITY_TYPES = Arrays.asList(NE_TYPE_PERSON, NE_TYPE_ORGANIZATION, NE_TYPE_LOCATION);
	private Map<String, List<String>> entityMap = new HashMap<String, List<String>>();
	

	private static StanfordCoreNLP pipeline;
	
	static {
		LOG.info("setting up NLP engine");
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.model.7class", "");
		props.put("ner.model.MISCclass", "");
		pipeline = new StanfordCoreNLP(props);
		LOG.info("done");
	}
	
	private List<String> words = new ArrayList<String>();
	
	public LemmaNerDocument(Document doc) {
		super(doc);
		entityMap.put(NE_TYPE_PERSON, doc.getPersons());
		entityMap.put(NE_TYPE_ORGANIZATION, doc.getOrganizations());
		entityMap.put(NE_TYPE_LOCATION, doc.getLocations());
		initialize();
	}
	
	public List<String> getWords() {
		return words;
	}

	private void initialize() {
		LOG.debug("parsing document");
		
		try {
			String text = getText();
			
			if (text == null || text.isEmpty()) {
				LOG.debug("skipping empty document");
			}
			
			else {
				Annotation doc = new Annotation(text);
				pipeline.annotate(doc);
				List<CoreMap> sentences = doc.get(SentencesAnnotation.class);

				if (sentences != null) {
					for (CoreMap sentence : sentences) {
						if (sentence != null) {
							
							String neType = null;
							StringBuilder neValue = null;
							for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
								String lemma = token.get(LemmaAnnotation.class);
								if (lemma != null) {
									words.add(lemma.toLowerCase());
								}
								
								String ne = token.get(NamedEntityTagAnnotation.class);
								// continuation?
								if (neType != null && neType.equals(ne)) {
									neValue.append(" ").append(token.get(TextAnnotation.class));
								}
								
								else {
									if (neType != null) {
										addEntity(neType, neValue.toString());
										neType = null;
										neValue = null;
									}
									
									if (TARGET_ENTITY_TYPES.contains(ne)) {
										neType = ne;
										neValue = new StringBuilder(token.get(TextAnnotation.class));
									}
								}
							}
						}
					}
				}
				
				LOG.debug("done parsing");
			}		
		} catch (Exception e) {
			words.clear();
			LOG.warn("error parsing doc: " + e.getMessage(), e);
		}
	}

	private void addEntity(String neType, String neValue) {
		List<String> list = entityMap.get(neType);
	
		String lcValue = neValue.toLowerCase();
		boolean found = false;
		for (String s : list) {
			s = s.toLowerCase();
			found = s.equals(lcValue) || s.endsWith(" " + lcValue);
			if (found) break;
		}
		
		if (!found) {
			list.add(neValue);
		}
	}
}
