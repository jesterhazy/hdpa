package com.bronzespear.hdpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.math3.linear.MatrixUtils;

public class ArrayUtils {
	public static double[] fill(double value, int dim1) {
		double[] array = new double[dim1];
		Arrays.fill(array, value);
		return array;
	}

	public static double[][] fill(double value, int dim1, int dim2) {
		return MatrixUtils.createRealMatrix(dim1, dim2).scalarAdd(value).getData();
	}

	public static double[] range(int size, double start, double step) {
		double[] array = new double[size];

		double value = start;
		for (int i = 0; i < size; i++) {
			array[i] = value;
			value += step;
		}

		return array;
	}
	
	private static class ArgsortComparator implements Comparator<Integer> {
		private double[] data;
		
		public ArgsortComparator(double[] data) {
			this.data = data;
		}

		public int compare(Integer i1, Integer i2) {
			int compare = Double.compare(data[i1], data[i2]);
			
			// stable order for ties
			if (compare == 0) {
				compare = i1 - i2;
			}
			
			return compare;
		}
	}

	public static int[] argsort(double[] input, boolean reverse) {
		Integer[] sortedIntegers = new Integer[input.length];
		
		for (int i = 0; i < input.length; i++) {
			sortedIntegers[i] = Integer.valueOf(i);
		}
		
		Comparator<Integer> cmp = new ArgsortComparator(input);
		if (reverse) {
			cmp = Collections.reverseOrder(cmp);
		}
			
		Arrays.sort(sortedIntegers, cmp);

		int[] sortedInts = new int[sortedIntegers.length];
		for (int i = 0; i < sortedIntegers.length; i++) {
			sortedInts[i] = sortedIntegers[i].intValue();
		}
		
		return sortedInts;
	}

	public static boolean arrayContains(int[] array, int key) {
		return Arrays.binarySearch(array, key) >= 0;
	}
}
