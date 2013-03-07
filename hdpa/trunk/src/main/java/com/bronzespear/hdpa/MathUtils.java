package com.bronzespear.hdpa;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;


public class MathUtils {
	/**
	 * error (relative or absolute) for approximate double comparisons
	 */
	static final double EPSILON = 0.0000001d;
//	private static final double LOG_NORMALIZER_MAX = 100.0d;

	private MathUtils() { }

	public static double sum(double... values) {
		return impl.sum(values);
	}
	
	public static double[] psi(double[] x) {
		double[] result = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			result[i] = psi(x[i]);
		}

		return result;
	}
	
	public static double psi(double x) {
		return impl.psi(x);
	}
	
	public static double[] cumulativeSum(double[] x) {	
		double sum = 0.0d;
		double[] y = new double[x.length];

		for (int i = 0; i < x.length; i++) {
			sum += x[i];
			y[i] = sum;
		}

		return y;
	}
	
	public static double[] cumulativeProduct(double[] x) {
		double[] logs = log(x);
		double[] sums = cumulativeSum(logs);
		double[] result = exp(sums);
		return result;
	}
	
	public static double[] exp(double... x) {
		double[] result = new double[x.length];

		for (int i = 0; i < x.length; i++) {
			result[i] = FastMath.exp(x[i]);
		}
		
		return result;
	}
	
	public static double[] log(double... x) {
		double[] result = new double[x.length];

		for (int i = 0; i < x.length; i++) {
			result[i] = FastMath.log(x[i]);
		}
		
		return result;		
	}
	
	public static double[] dirichletExpectation(double[] x) {
		double psisum = psi(sum(x));

		double[] expectation = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			expectation[i] = psi(x[i]) - psisum;
		}

		return expectation;
	}
	
	public static boolean nonZero(double x) {
		return !approximateEquals(x, 0.0d);
	}
	
	public static boolean approximateEquals(double x, double y) {
		return FastMath.abs(x - y) < (FastMath.max(EPSILON, EPSILON * FastMath.max(x, y)));
	}
	
	public static double[] scalarAdd(double[] x, double y) {
		return new ArrayRealVector(x).mapAddToSelf(y).toArray();
	}
	
	public static double[] scalarSubtract(double[] x, double y) {
		return new ArrayRealVector(x).mapSubtractToSelf(y).toArray();
	}
	
	public static double[][] scalarMultiply(double[][] x, double y) {
		return MatrixUtils.createRealMatrix(x).scalarMultiply(y).getData();
	}
	
	public static double[] vectorAdd(double[] x, double[] y) {
		return new ArrayRealVector(x)
				.add(new ArrayRealVector(y))
				.toArray();
	}

	public static double[] vectorSubtract(double[] x, double[] y) {
		return new ArrayRealVector(x)
				.subtract(new ArrayRealVector(y))
				.toArray();
	}
	
	public static double[] vectorDivide(double[] x, double[] y) {
		return new ArrayRealVector(x)
				.ebeDivide(new ArrayRealVector(y))
				.toArray();
	}

	public static double[][] matrixAdd(double[][] x, double[][] y) {
		return MatrixUtils.createRealMatrix(x)
				.add(MatrixUtils.createRealMatrix(y))
				.getData();
	}
	
	public static double max(double... values) {
		double max = 0.0d;
		
		if (values.length > 0) {
			max = values[0];

			for (int i = 1; i < values.length; i++) {
				if (max < values[i]) {
					max = values[i];
				}
			}
		}
		
		return max;
	}

	/**
	 * Shift an array of log probabilities so that the maximum value is 0. The result can be 
	 * exponentiated to produce normalized probabilities in the range 0..1.
	 */
	public static double[] logNormalize(double[] values) {
		double total = logSumExp(values);
		return scalarSubtract(values, total);
	}
	
	public static double logSumExp(double... values) {
		double max = max(values);
		double[] adjusted = scalarSubtract(values, max);
		double result = max + log(sum(exp(adjusted)))[0];
		return result;
	}
	
	public static void normalize(double[] values) {
		double sum = sum(values);

		for (int i = 0; i < values.length; i++) {
			values[i] /= sum;
		}
	}
	
//	public static void normalize(double[] values) {
//		// compute exp(log(sum(exp(v[])))) per Wang
//		int length = values.length;
//	
//		double max = max(values);
//		double logShift = LOG_NORMALIZER_MAX - FastMath.log(length + 1.0d) - max;
//		RealVector v = new ArrayRealVector(values).mapAddToSelf(logShift).mapToSelf(new Exp()); 
//		double total = sum(v.toArray());
//		double logNorm = FastMath.log(total) - logShift;
//		
//		double sum = 0.0d;
//		for (int i = 0; i < length; i++) {			
//			values[i] = FastMath.exp(values[i] - logNorm);
//			sum += values[i];
//		}
//		
//		if (!approximateEquals(sum, 1.0d)) {
//			LOG.error(String.format("normalized array sums to %f", sum));
//			throw new IllegalStateException();
//		}
//		
//		if (LOG.isTraceEnabled()) {
//			LOG.trace(String.format("max value: %f, logNorm: %f", max, logNorm));
//		}
//	}
	
	/* begin Impl interface and implementation classes */
	
	private static Impl impl = new Fast();
	
	public static void fast() {
		impl = new Fast();
	}
	
	public static void accurate() {
		impl = new Accurate();
	}
	
	private static interface Impl {
		double[] cumulativeProduct(double... x);
		double psi(double x);
		double sum(double... values);
	}
	
	private static final class Fast implements Impl {
		public double[] cumulativeProduct(double... values) {
			double product = 1.0d;
			double[] y = new double[values.length];

			for (int i = 0; i < values.length; i++) {
				product *= values[i];
				y[i] = product;
			}

			return y;
		}
		
		public double psi(double x) {
			if (Double.isNaN(x)) {
				throw new IllegalArgumentException();
			}
			
			return Gamma.digamma(x);
		}
		
		public double sum(double... values) {
			double sum = 0.0;
			for (double d : values) {
				sum += d;
			}
			
			return sum;
		}
	}
	
	private static final class Accurate implements Impl {
		public double[] cumulativeProduct(double... x) {
			double[] logs = log(x);
			double[] sums = cumulativeSum(logs);
			double[] result = exp(sums);
			return result;
		}
		
		public double psi(double x) {
			
			// per. Beal (2003)
			
			if (Double.isNaN(x)) {
				throw new IllegalArgumentException();
			}
			
			double psi = 0.0d;
			
			if (x > 6){
				psi = FastMath.log(x);
				psi -= 1 / (2 * x);
				psi -= 1 / (12 * FastMath.pow(x, 2));
				psi += 1 / (120 * FastMath.pow(x, 4));
				psi -= 1 / (252 * FastMath.pow(x, 6));
				psi += 1 / (240 * FastMath.pow(x, 8));
				psi -= 5 / (660 * FastMath.pow(x, 10));
				psi += 691 / (32760 * FastMath.pow(x, 12));
				psi -= 1 / (12 * FastMath.pow(x, 14));
			}
			
			else {
				psi = psi(x + 1) - (1 / x);
			}
			
			return psi;
		}
		
		public double sum(double... values) {
			double result = 0.0d;
			
			if (values.length == 1) {
				result = values[0];
			}
			
			else if (values.length == 2) {
				result = values[0] + values[1];
			}
			
			else {
				double sum = 0.0d;
				double c = 0.0d;
				for (int i = 0; i < values.length; i++) {
					double y = values[i] - c;
					double t = sum + y;
					c = (t - sum) - y;
					sum = t;
				}
				
				result = sum;
			}
			
			
			return result;
		}
	}
}
