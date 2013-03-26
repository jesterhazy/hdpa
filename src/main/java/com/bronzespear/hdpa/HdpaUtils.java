package com.bronzespear.hdpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.random.RandomDataGenerator;

public class HdpaUtils {
	private static final Log LOG = LogFactory.getLog(HdpaUtils.class);
	private static final RandomDataGenerator rand = new RandomDataGenerator();
	
	public static String repeatString(String s, int times) {
		if (times <= 0) { 
			return s;
		}
		
		int slen = s.length();
		StringBuilder sb = new StringBuilder(slen * times);
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		
		return sb.toString();
	}

	public static String formatDuration(long duration) {
		
		long[] parts = {
			TimeUnit.MILLISECONDS.toHours(duration),
			TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
			TimeUnit.MILLISECONDS.toSeconds(duration) % 60,
			duration % 1000,			
		};
		
		return String.format("%03d:%02d:%02d.%03d", parts[0], parts[1], parts[2], parts[3]);
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<List<HdpaDocument>> splitTestDocuments(List<HdpaDocument> documents) {
		List<HdpaDocument> train = new ArrayList<HdpaDocument>();
		List<HdpaDocument> test = new ArrayList<HdpaDocument>();
		
		int i = 0;
		for (HdpaDocument doc : documents) {
			HdpaDocument[] split = splitTestDocument(doc);
			
			// if original document is very short, 
			// test doc will have 0 length (and -Infinity log probability)
			if (split[1].getTotalTermCount() > 0) {
				train.add(split[0]);
				test.add(split[1]);	
			}
			
			else {
				LOG.warn(String.format("skipping document %d. test split is empty.", i));
			}
			
			i++;
		}
		
		return Arrays.asList(train, test);
	}
	
	public static HdpaDocument[] splitTestDocument(HdpaDocument doc) {
		Integer id = doc.getId();
		int[][] termIds = doc.getTermIds();
		int[][] termCounts = doc.getTermCounts();
		int modes = termIds.length;
		
		HdpaDocument train = new HdpaDocument(id, modes);
		HdpaDocument test = new HdpaDocument(id, modes);
		
		for (int m = 0; m < modes; m++) {
			int docTerms = termIds[m].length;
			int testTerms = docTerms / 10; // 90:10 split per. Wang2011
			
			test.getTermIds()[m] = new int[testTerms];
			test.getTermCounts()[m] = new int[testTerms];
			
			if (testTerms == 0) {
				train.getTermIds()[m] = termIds[m];
				train.getTermCounts()[m] = termCounts[m];
			}
			
			else {
				train.getTermIds()[m] = new int[docTerms - testTerms];
				train.getTermCounts()[m] = new int[docTerms - testTerms];

				int[] ids = rand.nextPermutation(docTerms, testTerms);
				Arrays.sort(ids);

				// and distribute to the new documents
				int j = 0;
				int k = 0;
				for (int i = 0; i < docTerms; i++) {
					if (ArrayUtils.arrayContains(ids, i)) {
						test.getTermIds()[m][j] = termIds[m][i];
						test.getTermCounts()[m][j] = termCounts[m][i];
						j++;
					}
					
					else {
						train.getTermIds()[m][k] = termIds[m][i];
						train.getTermCounts()[m][k] = termCounts[m][i];
						k++;
					}
				}
			}
		}
		
		return new HdpaDocument[] {train, test};
	}
	
	public static HdpaDocument[] splitTestDocument2(HdpaDocument doc) {
		Integer id = doc.getId();
		int modes = doc.getModeCount();
		
		HdpaDocument train = new HdpaDocument(id, modes);
		HdpaDocument test = new HdpaDocument(id, modes);
		
		int[][] expandedTermIds = doc.getExpandedTermIds();
		
		for (int m = 0; m < modes; m++) {
			int docTerms = expandedTermIds[m].length;
			int testTerms = docTerms / 10; // 90:10 split per. Wang2011
			
			test.getTermIds()[m] = new int[testTerms];
			test.getTermCounts()[m] = new int[testTerms];
						
			if (testTerms == 0) {
				train.getTermIds()[m] = doc.getTermIds()[m];
				train.getTermCounts()[m] = doc.getTermCounts()[m];
			}
			
			else {
				// collect test ids
				int[] testIds = rand.nextPermutation(docTerms, testTerms);
				Arrays.sort(testIds);
				
				// and rebuild train ids
				int[] trainIds = new int[docTerms - testTerms];
				int j = 0;
				for (int i = 0; i < docTerms; i++) {
					if (!ArrayUtils.arrayContains(testIds, i)) {
						trainIds[j] = expandedTermIds[m][i];						
						j++;
					}					
				}
				
				// flatten into ids and counts
				int[][] grouped = groupTermIds(testIds);
				test.getTermIds()[m] = grouped[0];
				test.getTermCounts()[m] = grouped[1];
				
				grouped = groupTermIds(trainIds);
				train.getTermIds()[m] = grouped[0];
				train.getTermCounts()[m] = grouped[1];
			}
		}
		
		return new HdpaDocument[] {train, test};
	}
	
	private static int[][] groupTermIds(int[] termIds) {
		SortedMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
		
		for (int id: termIds) {
			Integer count = map.get(id);
			if (count == null) {
				map.put(id, Integer.valueOf(1));
			}
			
			else {
				map.put(id, Integer.valueOf(count.intValue() + 1));
			}
		}
		
		int[] ids = new int[map.size()];
		int[] counts = new int[map.size()];
		
		int i = 0;
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			ids[i] = entry.getKey().intValue();
			counts[i] = entry.getValue().intValue();
			i++;
		}
		
		return new int[][] {ids, counts};
	}

	public static String formattedTimestamp() {
		return String.format("%1$tY%1$tm%1$td-%1$tH%1$tM", new Date());
	}
}
