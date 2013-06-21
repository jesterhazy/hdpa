package com.bronzespear.hdpa.corpus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilteredDocument extends DocumentDecorator {
	private static final Log LOG = LogFactory.getLog(FilteredDocument.class);
	private static final List<String> STOP_WORDS = Arrays.asList("a about above across after afterwards again against all almost alone along already also although always am among amongst amoungst amount an and another any anyhow anyone anything anyway anywhere are around as at back be became because become becomes becoming been before beforehand behind being below beside besides between beyond bill both bottom but by call can cannot cant co computer con could couldnt cry de describe detail did do doesn done down due during each eg eight either eleven else elsewhere empty enough etc even ever every everyone everything everywhere except few fifteen fifty fill find fire first five for former formerly forty found four from front full further get give go had has hasnt have he hence her here hereafter hereby herein hereupon hers herself him himself his how however hundred i ie if in inc indeed interest into is it its itself keep last latter latterly least less ltd just kg km made many may me meanwhile might mill mine more moreover most mostly move much must my myself name namely neither never nevertheless next nine no nobody none noone nor not nothing now nowhere of off often on once one only onto or other others otherwise our ours ourselves out over own part per perhaps please put rather re quite rather really regarding same say see seem seemed seeming seems serious several she should show side since sincere six sixty so some somehow someone something sometime sometimes somewhere still such system take ten than that the their them themselves then thence there thereafter thereby therefore therein thereupon these they thick thin third this those though three through throughout thru thus to together too top toward towards twelve twenty two un under until up unless upon us used using various very very via was we well were what whatever when whence whenever where whereafter whereas whereby wherein whereupon wherever whether which while whither who whoever whole whom whose why will with within without would yet you your yours yourself yourselves".split(" "));
	
	private List<String> words = new ArrayList<String>();
	
	public FilteredDocument(Document doc) {
		super(doc);
		initialize();
	}
	
	@Override
	public List<String> getTerms(CorpusMode mode) {
		return (CorpusMode.WORD == mode) ? words : super.getTerms(mode);
	}
	
	private void initialize() {
		for (String token : super.getTerms(CorpusMode.WORD)) {
			if (accept(token)) {
				words.add(token);
			}	
		}

		LOG.debug(String.format("retained %d of %d tokens", words.size(), getDocument().getWords().size()));
	}
	
	private boolean accept(String token) {	
		boolean accept = token != null;
		
		accept = accept && (
				token.matches("[1-2][0-9]{3}") || // 4 digit years
				token.matches("^[a-z]+$") || // words
				token.matches("^([a-z]+-)+[a-z]+$") || // hyphenated terms
				token.matches("^[a-z]+'[a-z]+$") || // contractions
				token.matches("^[a-z]\\.([a-z]\\.)+$") // punctuated acronyms
			);
				
		accept = accept && !STOP_WORDS.contains(token);
		
		return accept;
	}
}
