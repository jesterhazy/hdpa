package com.bronzespear.hdpa;

import static com.bronzespear.hdpa.ArrayUtils.*;
import static com.bronzespear.hdpa.HdpaUtils.repeatString;
import static com.bronzespear.hdpa.MathUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.util.FastMath;

import com.bronzespear.hdpa.corpus.CorpusDocument;
import com.bronzespear.hdpa.corpus.CorpusMode;
import com.bronzespear.hdpa.corpus.CorpusReader;

public class Hdpa {

	public class BatchStats {
		private int batchSize;
		private double[][] batchVarphi; // ss for beta parameters (u, v)
		private double[][][] batchLambda; // ss for lambda parameters
		private int iterations; // total iterations for this batch
		
		private Map<CorpusMode, Set<Integer>> uniqueTermIds = new HashMap<CorpusMode, Set<Integer>>();

		public BatchStats(int batchSize) {
			this.batchSize = batchSize;
			batchLambda = createEmptyLambdaArray();
		}
		
		public void update(HdpaDocument doc) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("updating batch stats");
			}
			
			iterations += docIterations;
			updateTermIds(doc);
			updateVarphi(docVarphi);
			updateLambda(docVarphi, docZeta, doc);
		}
		
		private void updateTermIds(HdpaDocument doc) {
			int[][] termIds = doc.getTermIds();
			
			for (int m = 0; m < termIds.length; m++) {
				CorpusMode mode = CorpusMode.values()[m];
				
				Set<Integer> idsSet = uniqueTermIds.get(mode);				
				if (idsSet == null) {
					idsSet = new HashSet<Integer>();
					uniqueTermIds.put(mode, idsSet);
				}
				
				for (int id : termIds[m]) {
					idsSet.add(id);
				}
			}
		}

		private void updateLambda(double[][] docVarphi, double[][][] docZeta, HdpaDocument doc) {
			int[][] ids = doc.getTermIds();
			int[][] counts = doc.getTermCounts();
			
			for (int m = 0; m < M; m++) {
				for (int k = 0; k < K; k++) {
					for (int n = 0; n < ids[m].length; n++) {
						int w = ids[m][n];
						for (int t = 0; t < T; t++) {
							batchLambda[m][k][w] += docVarphi[t][k] * docZeta[m][n][t] * counts[m][n];
						}
					}
				}
			}			
		}
		
		private void updateVarphi(double[][] docVarphi) {
			if (batchVarphi == null) {
				batchVarphi = docVarphi;
			}

			else {
				batchVarphi = matrixAdd(batchVarphi, docVarphi);
			}
		}

		public double batchWeight() {
			return (double) D / batchSize;
		}
	}
	
	private double[][] docVarphi;
	private double[][][] docZeta;
	private int docIterations;
	
	private static final Log LOG = LogFactory.getLog(Hdpa.class);
	private static final int MAX_ITERATIONS = 100;
	private static final int MIN_ITERATIONS = 3;
	private static final double MINIMUM_RHO = 0.0d;
	private static final double CONVERGENCE_THRESHOLD = 0.0001d;
	
	// data parameters
	private CorpusReader corpus;
	private int D; // total number of documents
	private int M; // number of modes
	private int[] W; // number of words per mode

	// learning rate parameters
	private int t = 1; // update count (+1)
	private double tau = 1.0d; // base for learning rate
	private double learningScale = 1.0d; // scale parameter for learning rate
	private double kappa = 0.9d; // exponent for learning rate (value from Hoffman et al. 2012) 

	private int K = 300; // top-level topic truncation
	private int T = 20; // doc-level topic truncation

	double gamma = 1.0d; // concentration parameter for top-level sticks
	double alpha = 1.0d; // concentration parameter for document-level sticks
	double eta; // hyperparameter for dirichlet base distribution

	// parameter for q(phi), the per-topic word weights
	double[][][] lambda;

	// dirichlet expectation of lambda
	double[][][] elogPhi;

	// corpusSticks (u, v) are parameters for q(beta'), the top-level sticks
	double[][] corpusSticks;
	
	// expectation of log corpusSticks
	private double[] elogsticksBeta;
	
	private int totalIterations;
	
	// number of documents to skip (because they are used for testing)
	private int skipDocumentsCount;
	
	// how often will model parameters be saved (# batches)
	private int saveFrequency;
	
	// reporting-only fields
	private int documentsProcessed;
	private long startTime;
	private long inferenceTime;
	private long updateTime;
	private int batchSize;
	
	public Hdpa(CorpusReader corpus) {
		this.corpus = corpus;
	}

	void start() throws IOException {
		LOG.debug("initializing corpus parameters");
		
		corpus.open();
		
		this.D = corpus.getDocumentCount();
		this.M = corpus.getModeCount(); 
//		this.M = 1; // use this to limit training to mode 0 (words)
		
		this.W = new int[M];
		this.eta = 0.01d; // Wang2011

		for (int m = 0; m < M; m++) {
			this.W[m] = corpus.getTermCount(m);
		}
		
		initializeCorpusSticks();
		updateElogBeta();
		
		initializeLambda();
		updateElogPhi();
	}

	private void initializeCorpusSticks() {
		corpusSticks = new double[][] { fill(1.0d, K - 1), range(K - 1, K - 1.0d, -1.0d) }; // Wang2011
//		corpusSticks = new double[][] { fill(1.0d, K - 1), fill(gamma, K - 1)}; // Hoffman2012
	}
	
	private void initializeLambda() {
		LOG.debug("initializing lambda");
		GammaDistribution gammaDistribution = new GammaDistribution(5.0d, 0.2d); // diverse word lists, decent weights, good likelihood\
		gammaDistribution.reseedRandomGenerator(999931111L);
		double[][][] array = new double[M][K][];

		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				array[m][k] = new double[W[m]];

				for (int w = 0; w < W[m]; w++) {
					array[m][k][w] = gammaDistribution.sample();
				}
			}
		}
		
		lambda = array;
	}
	
	private void processBatch(List<HdpaDocument> documents) {
		long start = System.currentTimeMillis();
		LOG.debug(String.format("starting batch of %d documents", documents.size()));
		
		BatchStats bstats = new BatchStats(documents.size());

		for (HdpaDocument document : documents) {
			processDocument(document, bstats);
		}
		
		inferenceTime += System.currentTimeMillis() - start;
		
		updateCorpusParameters(bstats);
		
		float elapsed = (System.currentTimeMillis() - start) / 1000.0f;
		LOG.info(String.format("batch time: %8.3fs (%5.3fs/doc): ", elapsed , elapsed / documents.size()));
		LOG.info(String.format("average iterations / document: %.10f", (float) bstats.iterations / documents.size()));
		logElapsedTimes();
	}

	private void logElapsedTimes() {
		LOG.info(String.format("total time:      " + HdpaUtils.formatDuration(System.currentTimeMillis() - startTime)));
		LOG.info(String.format("inference time:  " + HdpaUtils.formatDuration(inferenceTime)));
		LOG.info(String.format("update time:     " + HdpaUtils.formatDuration(updateTime)));
	}

	private void updateCorpusParameters(BatchStats bstats) {
		long start = System.currentTimeMillis();
		LOG.debug("updating corpus parameters");
		
		// set learning rate
		double rho = rho();
		
		// compute gradients and update global parameters
		updateCorpusSticks(rho, bstats);
		updateElogBeta();
		
		updateLambda(rho, bstats);
		updateElogPhi();
		t++;
		
		updateTime += System.currentTimeMillis() - start;
	}
	
	private void processDocument(HdpaDocument doc, BatchStats bstats) {
		inferDocumentParameters(doc);
		totalIterations += docIterations;
		documentsProcessed++;
		bstats.update(doc);
	}
	
	private void inferDocumentParameters(HdpaDocument document) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("processing document: " + document.getId());
		}
		
		else if (document.getId() % 100 == 0) {
			LOG.debug("processing document: " + document.getId());
		}
		
		// these will be updated before first use
		double[][] documentSticks = null;
		double[] elogsticksPi = null;
		
		initializeZeta(document);
		initializeVarphi();
		
		docIterations = 0;
		boolean converged = false;
		double likelihood = -Double.MAX_VALUE;
		while (!converged && docIterations < MAX_ITERATIONS) {
			if (MIN_ITERATIONS > docIterations) {
				updateZeta(document, null);
				updateVarphi(document, null);
			}
			
			else {
				updateZeta(document, elogsticksPi);
				updateVarphi(document, elogsticksBeta);
			}
			
			if (MIN_ITERATIONS - 1 <= docIterations) {
				documentSticks = updateDocumentSticks();
				elogsticksPi = expectationLogSticks(documentSticks);
			}

			docIterations++;
			
			// assess convergence			
			if (MIN_ITERATIONS < docIterations) {
				double oldLikelihood = likelihood;
				likelihood = calculateDocumentScore(document, elogsticksPi, documentSticks);			
				
				if (likelihood < oldLikelihood) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(String.format("likelihood decreasing. old value: %.10f, new value: %.10f", oldLikelihood, likelihood));
					}
					break;
				}
				
				converged = isConverged(oldLikelihood, likelihood);				
			}
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("%s after %d iterations", converged ? "converged" : "stopped", docIterations));
		}
	}
	
	private void initializeZeta(HdpaDocument document) {
		docZeta = new double[M][][];
	
		for (int m = 0; m < M; m++) {
			docZeta[m] = new double[document.termIds[m].length][T];
		}
	}
	
	private void initializeVarphi() {
		docVarphi = fill(1.0d / K, T, K);
	}
	
	private void updateLambda(double rho, BatchStats bstats) {
		for (Entry<CorpusMode, Set<Integer>> entry : bstats.uniqueTermIds.entrySet()) {
			int m = entry.getKey().ordinal();
			if (m < M) {
				for (int k = 0; k < K; k++) {
					for(Integer id : entry.getValue()) {
						int w = id.intValue();
						double gradient = eta + (bstats.batchWeight() * bstats.batchLambda[m][k][w]);
						lambda[m][k][w] = ((1.0d - rho) * lambda[m][k][w]) + (rho * gradient);
					}
				}
			}
		}
	}
	
	private void updateCorpusSticks(double rho, BatchStats bstats) {
		for (int k = 0; k < K - 1; k++) {
			double u = 0.0d;
			double v = 0.0d;
			
			for (int t = 0; t < T; t++) {
				u += bstats.batchVarphi[t][k];
				
				for (int i = k + 1; i < K; i++) {
					v += bstats.batchVarphi[t][i];
				}
			}
			
			double uGradient = 1 + (bstats.batchWeight() * u);
			double vGradient = gamma + (bstats.batchWeight() * v);
			
			corpusSticks[0][k] = ((1.0d - rho) * corpusSticks[0][k]) + (rho * uGradient);
			corpusSticks[1][k] = ((1.0d - rho) * corpusSticks[1][k]) + (rho * vGradient);
		}
	}

	private boolean isConverged(double oldLikelihood, double newLikelihood) {
		double ratio = (newLikelihood - oldLikelihood) / FastMath.abs(oldLikelihood);
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("convergence ratio: %f", ratio));
		}
		return FastMath.abs(ratio) < CONVERGENCE_THRESHOLD;
	}
	
	private double calculateDocumentScore(HdpaDocument doc,
			double[] elogsticksPi, double[][]documentSticks) {

		double score = sum(calculateScoreX(doc, elogPhi),
				calculateScoreZ(doc, elogsticksPi),
				calculateScoreC(),
				calculateScorePi(documentSticks));
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("likelihood: %f", score));
		}
		return score;
	}
	
	private double calculateScoreX(HdpaDocument doc, double[][][] elogPhi) {
		double score = 0.0d;
		
		int[][] ids = doc.getTermIds();
		int[][] counts = doc.getTermCounts();
		
		for (int t = 0; t < T; t++) {
			for (int k = 0; k < K; k++) {
				if (docVarphi[t][k] > 0.0d) {
					double inner = 0.0d;
					for (int m = 0; m < M; m++) {
						for (int n = 0; n < ids[m].length; n++) {
							if (docZeta[m][n][t] > 0.0d) {
								int w = ids[m][n];						
								inner += (docZeta[m][n][t] * elogPhi[m][k][w] * counts[m][n]);
							}
						}
					}
					
					score += (docVarphi[t][k] * inner);
				}
			}
		}
		
		return score;
	}

	private double calculateScoreZ(HdpaDocument doc, double[] elogsticksPi) {
		double score = 0.0d;
		
		int[][] ids = doc.getTermIds();
		int[][] counts = doc.getTermCounts();
		
		for (int m = 0; m < M; m++) {
			for (int n = 0; n < ids[m].length; n++) {
				for (int t = 0; t < T; t++) {
					if (docZeta[m][n][t] > 0.0d) {
						score += (docZeta[m][n][t] * (elogsticksPi[t] - FastMath.log(docZeta[m][n][t])) * counts[m][n]);	
					}
				}
			}
		}
		
		return score;
	}
	
	private double calculateScoreC() {
		double score = 0.0d;

		for (int t = 0; t < T; t++) {
			for (int k = 0; k < K; k++) {
				if (docVarphi[t][k] > 0.0d) {
					score += (docVarphi[t][k] * (elogsticksBeta[k] - FastMath.log(docVarphi[t][k])));
				}
			}
		}
		
		return score;
	}
	
	private double calculateScorePi(double[][] documentSticks) {
		double score = 0.0d;
		double[] a = documentSticks[0];
		double[] b = documentSticks[1];

		for (int t = 0; t < T - 1; t++) {
			double psisum = psi(a[t] + b[t]);
			
			score += (1 - a[t]) * (psi(a[t]) - psisum);
			score += (alpha - b[t]) * (psi(b[t]) - psisum);
		}
				
		return score;
	}
	
	private void stop() throws IOException {
		LOG.info("stopping analysis");
		LOG.info(String.format("average iterations / document: %.10f", (float) totalIterations / documentsProcessed));
		corpus.close();
	}

	private double[][][] createEmptyLambdaArray() {
		double[][][] array = new double[M][K][];
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				array[m][k] = new double[W[m]];
			}
		}
		
		return array;
	}
	
	
	private double[] expectationLogSticks(double[][] sticks) {
		double[] psisum = psi(vectorAdd(sticks[0], sticks[1]));
		double[] eloga = vectorSubtract(psi(sticks[0]), psisum);
		double[] elogb = vectorSubtract(psi(sticks[1]), psisum);
		double[] cumsumb = cumulativeSum(elogb);

		int length = sticks[0].length + 1;
		double[] elogsticks = Arrays.copyOf(eloga, length);

		for (int i = 1; i < length; i++) {
			elogsticks[i] += cumsumb[i - 1];
		}

		return elogsticks;
	}
	
	private void updateElogBeta() {
		elogsticksBeta = expectationLogSticks(corpusSticks);
	}

	private void updateElogPhi() {
		double[][][] array = new double[M][K][];
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				array[m][k] = expectLogDirichlet(lambda[m][k]);
			}
		}
		
		elogPhi = array;
	}

	private double rho() {
		double rho = learningScale * FastMath.pow(tau + t, -kappa);
		rho = FastMath.max(rho, MINIMUM_RHO);	
		LOG.info("rho: " + rho);
		return rho;
	}
	
	private double[][] updateDocumentSticks() {
		double[][] documentSticks = new double[2][T - 1];
		
		for (int t = 0; t < T - 1; t++) {
			double a = 0.0d;
			double b = 0.0d;
			for (int m = 0; m < docZeta.length; m++) {
				for (int n = 0; n < docZeta[m].length; n++) {
					a += docZeta[m][n][t];
					
					for (int i = t + 1; i < T; i++) {
						b += docZeta[m][n][i];
					}
				}
			}
			
			documentSticks[0][t] = 1.0d + a;
			documentSticks[1][t] = alpha + b;
		}
		
		return documentSticks;
	}
	
	private void updateZeta(HdpaDocument doc, double[] elogsticksPi) {	
		int[][] ids = doc.getTermIds();
		int[][] counts = doc.getTermCounts();
		
		double tmp = 0.0d;
		for (int m = 0; m < M; m++) {
			for (int n = 0; n < ids[m].length; n++) {
				int w = ids[m][n];
				int count = counts[m][n];
								
				for (int t = 0; t < T; t++) {
					tmp = 0.0d;
					for (int k = 0; k < K; k++) {
						tmp += docVarphi[t][k] * elogPhi[m][k][w];
					}
					
					docZeta[m][n][t] = tmp * count;
					
					if (elogsticksPi != null) {
						docZeta[m][n][t] += elogsticksPi[t];
					}
				}
				
				docZeta[m][n] = exp(logNormalize(docZeta[m][n]));
			}
		}
	}
	
	private void updateVarphi(HdpaDocument doc, double[] elogsticksBeta) {
		int[][] ids = doc.getTermIds();
		int[][] counts = doc.getTermCounts();
		
		for (int t = 0; t < T; t++) {
			for (int k = 0; k < K; k++) {				
				docVarphi[t][k] = elogsticksBeta == null ? 0.0d : elogsticksBeta[k];

				for (int m = 0; m < M; m++) {
					for (int n = 0; n < ids[m].length; n++) {
						int w = ids[m][n];
						docVarphi[t][k] += (docZeta[m][n][t] * elogPhi[m][k][w] * counts[m][n]);
					}
				}
			}

			docVarphi[t] = exp(logNormalize(docVarphi[t]));
		}
	}
	
	public void loadParameters(File file) throws IOException {
		LOG.info("loading model parameters from: " + file.getAbsolutePath());
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			
			String line = null;
			int lineNumber = 0;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				
				switch (lineNumber) {
				case 0:
					D = Integer.parseInt(parts[1]);
					break;
				case 1:
					M = Integer.parseInt(parts[1]);
					break;
				case 2:
					W = new int[M];
					for (int m = 0; m < M; m++) {
						W[m] = Integer.parseInt(parts[m + 1]);
					}
					break;
				case 3:
					t = Integer.parseInt(parts[1]);
					break;
				case 4:
					tau = Double.parseDouble(parts[1]);
					break;
				case 5:
					learningScale = Double.parseDouble(parts[1]);
					break;
				case 6:
					kappa = Double.parseDouble(parts[1]);
					break;
				case 7:
					K = Integer.parseInt(parts[1]);
					break;
				case 8:
					T = Integer.parseInt(parts[1]);
					break;
				case 9:
					gamma = Double.parseDouble(parts[1]);
					break;
				case 10:
					alpha = Double.parseDouble(parts[1]);
					break;
				case 11:
					eta = Double.parseDouble(parts[1]);
					break;
				case 12:
					inferenceTime = Long.parseLong(parts[1]);
					break;
				case 13:
					documentsProcessed = Integer.parseInt(parts[1]);
					break;
				case 14:
					corpusSticks = new double[2][K - 1];
					// deliberate fall thru
				case 15:
					for (int k = 0; k < K - 1; k++) {
						corpusSticks[lineNumber - 14][k] = Double.parseDouble(parts[k + 1]);
					}
					break;
				case 16:
					LOG.debug("loading lambda...");
					// deliberate fall thru
				default:					
					if (lineNumber <= (M * K) + 16) {
						
						if (lambda == null) {
							lambda = new double[M][K][];
						}
						
						Scanner scanner = new Scanner(parts[0]);
						int m = Integer.parseInt(scanner.findInLine("\\d+"));
						int k = Integer.parseInt(scanner.findInLine("\\d+"));

						lambda[m][k] = new double[W[m]];
						for (int w = 0; w < W[m]; w++) {
							lambda[m][k][w] = Double.parseDouble(parts[w + 1]);
						}
					}
				}
				
				lineNumber++;
			}
		}
		
		finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		updateElogBeta();
		updateElogPhi();
		
		LOG.info("done loading");
	}
	
	private String getModelDir() {
		return String.format("%s-model-b%d-k%3.1f-%s",
				corpus.getBasedir().getName(),
				batchSize,
				kappa,
				HdpaUtils.formattedTimestamp(startTime));
	}
	
	private void saveFinalParameters() throws IOException {
		String filename = getModelDir() + "/final.csv";
		saveParameters(filename);
	}

	private void saveParameters() throws IOException {
		String filename = String.format("%s/%05d.csv", getModelDir(), getBatchNumber());
		saveParameters(filename);
	}

	public int getBatchNumber() {
		return t - 1;
	}
	
	private void saveParameters(String filename) throws IOException {
		File file = new File(corpus.getBasedir().getParentFile(), filename);
		
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		saveParameters(file);
	}
	
	void saveParameters(File file) throws IOException {
		LOG.info("saving model parameters to: " + file.getAbsolutePath());
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(file, "UTF-8");
			pw.print("D,");
			pw.println(D);
			
			pw.print("M,");
			pw.println(M);

			pw.print("W");
			for (int m = 0; m < M; m++) {				
				pw.print(",");
				pw.print(W[m]);
			}
			pw.println();
			
			pw.print("t,");
			pw.println(t);
			
			pw.print("tau,");
			pw.println(tau);
			
			pw.print("learningScale,");
			pw.println(learningScale);
			
			pw.print("kappa,");
			pw.println(kappa);
			
			pw.print("K,");
			pw.println(K);
			
			pw.print("T,");
			pw.println(T);
			
			pw.print("gamma,");
			pw.println(gamma);
			
			pw.print("alpha,");
			pw.println(alpha);
			
			pw.print("eta,");
			pw.println(eta);
			
			pw.print("inferenceTime,");
			pw.println(inferenceTime);
			
			pw.print("documentsProcessed,");
			pw.println(documentsProcessed);
			
			for (int i = 0; i < corpusSticks.length; i++) {
				pw.print("corpusSticks[" + i + "]");
				
				for (int k = 0; k < corpusSticks[i].length; k++) {
					pw.print(",");
					pw.print(corpusSticks[i][k]);
				}
				
				pw.println();
			}
			
			LOG.debug("saving lambda...");
			for (int m = 0; m < M; m++) {
				for (int k = 0; k < K; k++) {
					pw.print("lambda[" + m + "][" + k + "]");
					for (int w = 0; w < W[m]; w++) {
						pw.print(",");
						pw.print(lambda[m][k][w]);
					}
					
					pw.println();
				}
			}
		}
		finally {
			if (pw != null) {
				pw.close();
			}
		}
		
		LOG.info("done saving");
	}
	
	public double[] getTopicWeights() {
		return calculateStickWeights(corpusSticks);
	}
	
	private double[] calculateStickWeights(double[][] sticks) {
		double[] sum = vectorAdd(sticks[0], sticks[1]);
		double[] ea = vectorDivide(sticks[0], sum);
		double[] eb = vectorDivide(sticks[1], sum);
		double[] cumprodb = cumulativeProduct(eb);
				
		int length = sticks[0].length + 1;
		double[] weights = new double[length];
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				weights[i] = ea[i];				
			}
			else if (i < length - 1){
				weights[i] = ea[i] * cumprodb[i - 1];
			}
			
			else {
				weights[i] = cumprodb[i - 1]; // b_k' = 1.
			}
		}
		
		return weights;
	}

	public void printTopics(File output) throws IOException {
		double[] topicWeights = calculateStickWeights(corpusSticks);
		int[] sortedTopics = argsort(topicWeights, true);
		
		// output corpus topic weights
		PrintWriter out = new PrintWriter(output, "UTF-8");
		
		out.println("1. corpus-level topic weights");
		out.println(repeatString("-", 80));
		out.println();		
		out.println("topic           weight     terms");
		for (int k : sortedTopics) {
			out.printf("%5d     %12.10f     %s\n", k, topicWeights[k], topTerms(k, CorpusMode.WORD.ordinal(), 10));
		}
		
		out.println();
		out.println();
		
		out.println("2. topic terms");
		out.println(repeatString("-", 80));
		out.println();

		for (int k : sortedTopics) {
			out.printf("     topic: %5d (%.10f)\n", k, topicWeights[k]);
			
			for (int m = 0; m < M; m++) {
				out.printf("     mode: %s\n\n", CorpusMode.values()[m].name().toLowerCase());
				printTopic(out, 25, k, m);
			}
			
			out.println("     " + repeatString("-", 75));
			out.println();
			out.println();
		}
		
		out.close();
	}

	private void printTopic(PrintWriter out, int limit, int k, int m) {
		double[] lambdamk = Arrays.copyOf(lambda[m][k], lambda[m][k].length);  
		normalize(lambdamk);
		
		int[] termIds = argsort(lambdamk, true);
		limit = FastMath.min(limit, termIds.length); 
		
		out.printf("     %-12s     %-50s\n", "weight", "term");
		out.printf("     %-12s     %-50s\n", repeatString("-", 12), repeatString("-", 50));
		for (int i = 0; i < limit; i++) {
			int w = termIds[i];
			String word = corpus.getDictionary(m).getTerm(w);
			out.printf("     %.10f     %-50s\n", lambdamk[w], word);
		}
		
		out.println();
		out.println();
	}
	
	public void assignTopics(File output) throws IOException {	
		PrintWriter out = new PrintWriter(output, "UTF-8");
		
		for (CorpusDocument document : corpus) {
			assignTopics(new HdpaDocument(document), out);
		}
		
		out.close();
	}
	
	/**
	 * For indexing.
	 */
	public double[] assignTopics(HdpaDocument document) { 
		inferDocumentParameters(document);
		double weights[] = summarizeVarphi();
		
		return weights;
	}
	
	private void assignTopics(HdpaDocument document, PrintWriter out) {
		double weights[] = assignTopics(document);
		out.print(document.getId());
		for (double d : weights) {
			out.print(",");
			out.print(d);
		}
		
		out.println();
	}
	
	private double[] summarizeVarphi() {
		double[] weights = new double[K];
		for (int k = 0; k < K; k++) {
			for (int t = 0; t < T; t++) {
				weights[k] += docVarphi[t][k] / T;
			}
		}
		
		return weights;
	}

	public void processOnce() throws IOException {
		processUntilTime(0);
	}
	
	public void processUntilTime(long endTime) throws IOException {
		startTime = System.currentTimeMillis();
		start();
		
		List<HdpaDocument> batch = new ArrayList<HdpaDocument>(batchSize);
		while(!isTimeExpired(endTime)) {
			
			for (CorpusDocument document : corpus) {
				HdpaDocument doc = new HdpaDocument(document);
				if (skipDocumentsCount > doc.getId()) {
					continue;
				}
				
				batch.add(doc);

				if (batchSize == batch.size()) {
					processBatch(batch);
					batch.clear();
					
					if (saveRequired()) {
						saveParameters();
					}
				}
				
				if (isTimeExpired(endTime)) {
					break;
				}
			}
			
			if  (hasTimeLimit(endTime)) {
				if (!isTimeExpired(endTime)) {
					corpus.reopen(); // will need to skip test docs again.
				}
			}
			
			// if we aren't working towards a time limit, complete any leftover docs and exit.
			else {
				if (!batch.isEmpty()) {
					processBatch(batch);
					batch.clear();
				}			
				break;
			}
		}
		
		saveFinalParameters();	
		stop();
	}
	
	private boolean saveRequired() {
		boolean save = false;
		
		int batchNumber = getBatchNumber();
		
		if (saveFrequency > 0) {
			save = batchNumber % saveFrequency == 0;
		}
		
		else {				
			// power of two?
			save = (batchNumber & (batchNumber-1)) == 0 || batchNumber % 256 == 0;
		}
		
		return save;
	}

	private boolean hasTimeLimit(long endTime) {
		return endTime != 0;
	}

	private boolean isTimeExpired(long endTime) {
		return hasTimeLimit(endTime) && endTime < System.currentTimeMillis();
	}
	
	void evaluateModel(List<HdpaDocument> trainingDocuments, List<HdpaDocument> testDocuments) {
			long start = System.currentTimeMillis();
			LOG.info("evaluating model");
			
			int totalWords = 0;
			double totalLogLikelihood = 0.0d;
			
			for (int i = 0; i < trainingDocuments.size(); i++) {
				HdpaDocument train = trainingDocuments.get(i);
				HdpaDocument test = testDocuments.get(i);
				
				inferDocumentParameters(train);
								
				totalLogLikelihood += logPredictive(test);
				totalWords += test.getTotalTermCount();
			}
			
			LOG.info("finished model evaluation");
			LOG.info("per-word log likelihood: " + (totalLogLikelihood / totalWords));
			LOG.info(String.format("evaluation time: %s", HdpaUtils.formatDuration(System.currentTimeMillis() - start)));
	}
	
	
	private double logPredictive(HdpaDocument doc) {
		double score = calculateScoreX(doc, elogPhi);
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("logPredictive for doc %d: %f", doc.getId(), score / doc.getTotalTermCount()));
		}
		
		return score;
	}
	
	/**
	 * Number of documents to skip at start of corpus (because they will be used for model evaluation). 
	 * @param skipDocumentsCount
	 */
	public void setSkipDocumentsCount(int skipDocumentsCount) {
		this.skipDocumentsCount = skipDocumentsCount;
	}

	/**
	 * Number of batches to process between saves. 0 means only save at end of job. 
	 * @param saveFrequency
	 */
	public void setSaveFrequency(int saveFrequency) {
		this.saveFrequency = saveFrequency;
	}
	
	public int getDocumentsProcessed() {
		return documentsProcessed;
	}
	
	public long getInferenceTime() {
		return inferenceTime;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public int getK() {
		return K;
	}
	
	public int[][] topTermIds(int m, int limit) {
		int[][] termIds = new int[K][];
		for (int k = 0; k < K; k++) {
			termIds[k] = topTermIds(k, m, limit);
		}
		
		return termIds;
	}
	
	private int[] topTermIds(int k, int m, int limit) {
		int[] termIds = argsort(lambda[m][k], true);
		return Arrays.copyOf(termIds, FastMath.min(limit, termIds.length));
	}
	
	public List<String> topTerms(int k, int m, int limit) {
		int[] termIds = topTermIds(k, m, limit);
		
		List<String> terms = new ArrayList<String>(termIds.length);
		
		for (int i = 0; i < termIds.length; i++) {
			int w = termIds[i];
			String term = corpus.getDictionary(m).getTerm(w);
			terms.add(term);
		}
		
		return terms;
	}
}
