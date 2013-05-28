package com.bronzespear.hdpa.coherence.ohdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bronzespear.hdpa.ArrayUtils;
import com.bronzespear.hdpa.MathUtils;
import com.bronzespear.hdpa.coherence.Model;

public class WangModel extends Model {
	private static final Log LOG = LogFactory.getLog(WangModel.class);
	private File topicsFile;
	
	public WangModel(String topicsFile) {
		this.topicsFile = new File(topicsFile);
	}

	public void load() throws IOException {
		LOG.info("reading model from " + topicsFile.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(topicsFile), "UTF-8"));

		List<int[]> termIdsList = new ArrayList<int[]>();
		List<Double> topicPrevalenceList = new ArrayList<Double>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			double[] topic = parseLine(line);
			int[] topIds = Arrays.copyOf(ArrayUtils.argsort(topic, true), termLimit);
			termIdsList.add(topIds);
			topicPrevalenceList.add(MathUtils.sum(topic));
		}
		
		topTermIds = termIdsList.toArray(new int[0][]);
		numberOfTopics = topTermIds.length;
		
		topicPrevalence = new double[numberOfTopics];
		for (int i = 0; i < numberOfTopics ; i++) {
			topicPrevalence[i] = topicPrevalenceList.get(i).doubleValue();
		}
		MathUtils.normalize(topicPrevalence);
		
		reader.close();
		
		LOG.info("finished loading model");
	}

	private double[] parseLine(String line) {
		String[] parts = line.trim().split(" ");
		
		double[] weights = new double[parts.length];
		
		for (int i = 0; i < parts.length; i++) {
			weights[i] = Double.parseDouble(parts[i]);
		}
		
		return weights;
	}
}
