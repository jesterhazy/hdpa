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
	private static PsiFunction psi = new BealPsiFunction();
	private static SumFunction sum = new SimpleSumFunction();
	private static ExpFunction exp = new CommonsExpFunction();

	private MathUtils() { }

	public static double sum(double... values) {
		return sum.sum(values);
	}
	
	public static int sum(int... values) {
		int sum = 0;
		for (int i : values) {
			sum += i;
		}
		
		return sum;
	}
	
	public static double[] psi(double[] x) {
		double[] result = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			result[i] = psi(x[i]);
		}

		return result;
	}
	
	public static double psi(double x) {
		return psi.psi(x);
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
			result[i] = exp.exp(x[i]);
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
	
	public static double[] expectLogDirichlet(double[] x) {
		double psisum = psi(sum(x));

		double[] expectation = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			expectation[i] = psi(x[i]) - psisum;
		}

		return expectation;
	}
	
	public static double[] expectDirichlet(double[] x) {
		double sum = sum(x);

		double[] expectation = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			expectation[i] = x[i] / sum;
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
	
	public static double[] scalarMultiply(double[] x, double y) {
		return new ArrayRealVector(x).mapMultiplyToSelf(y).toArray();
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
	
	public static double[] vectorMultiply(double[] x, double[] y) {
		return new ArrayRealVector(x)
				.ebeMultiply(new ArrayRealVector(y))
				.toArray();
	}
	
	public static double[] vectorDivide(int[] x, double[] y) {
		double[] d = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			d[i] = x[i];
		}
		
		return vectorDivide(d, y);
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
	
	public static double[] abs(double[] x) {
		double[] d = new double[x.length];
		
		for (int i = 0; i < x.length; i++) {
			d[i] = FastMath.abs(x[i]);
		}
		
		return d;
	}

	public static double mean(double[] x) {
		return sum(x) / (double) x.length;
	}

	
	static interface PsiFunction {
		double psi(double x);
	}
	
	static void setPsiFunction(PsiFunction psi) {
		MathUtils.psi = psi; 
	}
	
	static final class BealPsiFunction implements PsiFunction {
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
	}
	
	static final class CommonsPsiFunction implements PsiFunction {
		public double psi(double x) {
			if (Double.isNaN(x)) {
				throw new IllegalArgumentException();
			}
			
			return Gamma.digamma(x);
		}
	}
	
	static interface SumFunction {
		double sum(double... values);
	}
	
	static void setSumFunction(SumFunction sum) {
		MathUtils.sum = sum; 
	}
	
	static final class SimpleSumFunction implements SumFunction {
		public double sum(double... values) {
			double sum = 0.0;
			for (double d : values) {
				sum += d;
			}
			
			return sum;
		}
	}
	
	static final class KahanSumFunction implements SumFunction {
		public double sum(double... values) {
			// kahan summation
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
	
	static interface ExpFunction {
		double exp(double x);
	}
	
	static void setExpFunction(ExpFunction exp) {
		MathUtils.exp = exp;
	}
	
	static final class CommonsExpFunction implements ExpFunction {
		public double exp(double x) {
			return FastMath.exp(x);
		}
	}
	
	static final class SchraudolphExpFunction implements ExpFunction {
		public double exp(double x) {
			// http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
			final long tmp = (long) (1512775 * x + (1072693248 - 60801));
			return Double.longBitsToDouble(tmp << 32);
		}
	}
	
	static final class SchraudolphBetterExpFunction implements ExpFunction {
		public double exp(double x) {
			// http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
			// + Schraudolph's comments (better_exp)
			final double x2 = x / 2d;
			return exp2(x2) / exp2(-x2);
		}
		
		public double exp2(double val) {
			// http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/
		    final long tmp = (long) (1512775 * val + (1072693248));
		    return Double.longBitsToDouble(tmp << 32);
		}
	}
}