package com.bronzespear.hdpa;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.FastMath;

import com.bronzespear.hdpa.corpus.CorpusMode;

public class LdaEvaluator {
	private static final Log LOG = LogFactory.getLog(LdaEvaluator.class);
	private static final String RESULTS_LINE_FORMAT = "%5d   %10d   %10.2f   %10.2f   %17.5f   %10d   %10.5f\n";
	private static final String RESULTS_HEADER_FORMAT = "%5s   %10s   %10s   %10s   %17s   %10s   %10s\n";
	private static final int WORD_MODE = CorpusMode.WORD.ordinal();
	private static final int MAX_ITERATION = 100;
	private static final double CONVERGENCE_LIMIT = 0.00001d;
	private double[] alpha;
	private double[][] beta;
	private int K;
	private int W;
	private int batch;
	private int totalDocs;
	private float inferenceTime;
	private PrintWriter resultsWriter;

	public void setModelFile(File modelFile) throws IOException {
		if (resultsWriter == null) {
			initializeResultsWriter(modelFile);
		}
		
		Hdpa hdpa = new Hdpa(null);
		hdpa.loadParameters(modelFile);
		
		this.K = hdpa.K;
		this.W = hdpa.lambda[WORD_MODE][0].length;
		this.batch = hdpa.getBatchNumber();
		this.totalDocs = hdpa.getDocumentsProcessed();
		this.inferenceTime = hdpa.getInferenceTime() / 1000.0f;
		
		initializeAlpha(hdpa);
		initializeBeta(hdpa);
	}

	private void initializeResultsWriter(File modelFile) throws IOException {
		File resultsFile = 
				new File(modelFile.getParentFile(), String.format("eval-%s.txt",
				HdpaUtils.formattedTimestamp(System.currentTimeMillis())));

		resultsWriter = new PrintWriter(resultsFile, "UTF-8");
		resultsWriter.printf(RESULTS_HEADER_FORMAT,
				"batch", "total docs", "train time", "test time",
				"test score", "test words", "per-word");
	}

	private void initializeAlpha(Hdpa hdpa) {
		alpha = new double[K];
		
		double[] sticks = MathUtils.vectorDivide(hdpa.corpusSticks[0], 
				MathUtils.vectorAdd(hdpa.corpusSticks[0], hdpa.corpusSticks[1]));
		
		double left = 1.0d;
		
		for (int k = 0; k < K - 1; k++) {
			alpha[k] = sticks[k] * left;
			left = left - alpha[k];
		}
		
		alpha[K - 1] = left;
		alpha = MathUtils.scalarMultiply(alpha, hdpa.alpha);
	}

	private void initializeBeta(Hdpa hdpa) {
		beta = new double[K][W];
		for (int k = 0; k < K; k++) {
			double sum = MathUtils.sum(hdpa.lambda[WORD_MODE][k]);
			for (int w = 0; w < W; w++) {
				// unlike Wang, our lamba values include eta component
				beta[k][w] = hdpa.lambda[WORD_MODE][k][w] / sum;
			}
		}
	}
	
	public void evaluate(List<HdpaDocument> trainingDocuments, List<HdpaDocument> testDocuments) {
		long start = System.currentTimeMillis();
		LOG.info("evaluating model");
		
		int totalWords = 0;
		double totalLogLikelihood = 0.0d;
		
		for (int i = 0; i < trainingDocuments.size(); i++) {
			HdpaDocument train = trainingDocuments.get(i);
			HdpaDocument test = testDocuments.get(i);
			
			totalLogLikelihood += evaluateDocument(train, test);
			totalWords += test.getTotalTermCount();
		}
	
		double testingTime = (System.currentTimeMillis() - start) / 1000.0;
		resultsWriter.printf(RESULTS_LINE_FORMAT,
				batch, totalDocs, inferenceTime, testingTime,
				totalLogLikelihood, totalWords, 
				totalLogLikelihood / totalWords);
		
		LOG.info("finished model evaluation");
		LOG.info("per-word log likelihood: " + (totalLogLikelihood / totalWords));
		LOG.info(String.format("evaluation time: %s", HdpaUtils.formatDuration(System.currentTimeMillis() - start)));
	}

	
	private double evaluateDocument(HdpaDocument train, HdpaDocument test) {
		
		int[] ids = train.getTermIds()[WORD_MODE];
		int[] counts = train.getTermCounts()[WORD_MODE];
		
		double[] gamma = ArrayUtils.fill(1.0d, alpha.length);
		double[] expElogtheta = MathUtils.exp(MathUtils.expectLogDirichlet(gamma));
		double[] phinorm = updatePhinorm(ids, expElogtheta);
		
		// lda e-step
		int iteration = 0;
		while (iteration < MAX_ITERATION) {
			double[] lastgamma = gamma;
			iteration++;
			
			gamma = updateGamma(ids, counts, expElogtheta, phinorm);
			double[] elogtheta = MathUtils.expectLogDirichlet(gamma);
			expElogtheta = MathUtils.exp(elogtheta);
			phinorm = updatePhinorm(ids, expElogtheta);
			
			double meanchange = calculateMeanChange(gamma, lastgamma);
			if (meanchange < CONVERGENCE_LIMIT) {
				break;
			}
		}
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("iterations: " + iteration);
		}

		// now score test portion
		MathUtils.normalize(gamma);
		ids = test.getTermIds()[WORD_MODE];
		counts = test.getTermCounts()[WORD_MODE];
		
		double score = 0.0d;
		for (int i = 0; i < ids.length; i++) {
			int w = ids[i];
			
			double x = 0.0d;
			for (int k = 0; k < K; k++) {
				x += gamma[k] * beta[k][w];
			}
			
			score += FastMath.log(x) * counts[i];
		}
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("doc: %4d - score: %f", train.getId(), score / test.getTotalTermCount()));
		}
		
		return score;
	}

	private double calculateMeanChange(double[] gamma, double[] lastgamma) {
		return MathUtils.mean(MathUtils.abs(MathUtils.vectorSubtract(gamma, lastgamma)));
	}

	private double[] updatePhinorm(int[] ids, double[] expElogtheta) {
		double[] phinorm = new double[ids.length];
		for (int i = 0; i < phinorm.length; i++) {
			int w = ids[i];
			for (int k = 0; k < K; k++) {
				phinorm[i] += expElogtheta[k] * beta[k][w];
			}
		}
		return phinorm;
	}

	private double[] updateGamma(int[] ids, int[] counts,
			double[] expElogtheta, double[] phinorm) {
		double[] g1 = MathUtils.vectorDivide(counts, phinorm);
		
		// [k]
		double[] g2 = new double[K];
		for (int k = 0; k < K; k++) {
			for (int i = 0; i < g1.length; i++) {
				int w = ids[i];
				g2[k] += g1[i] * beta[k][w];
			}
		}
		
		double[] g3 = MathUtils.vectorMultiply(g2, expElogtheta);
		double[] gamma = MathUtils.vectorAdd(alpha, g3);
		return gamma;
	}

	public void close() {
		resultsWriter.close();
		resultsWriter = null;
	}
}
