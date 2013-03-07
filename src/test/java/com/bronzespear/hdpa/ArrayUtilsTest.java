package com.bronzespear.hdpa;

import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilsTest {
	
	@Test
	public void testArgsort() throws Exception {
		double[] input = {0.3d, 0.1d, 0.2d, 0.4d, 0.1d};
		int[] expected = {1, 4, 2, 0, 3};
		int[] reversed = {3, 0, 2, 4, 1};
				
		Assert.assertArrayEquals(expected, ArrayUtils.argsort(input, false));
		Assert.assertArrayEquals(reversed, ArrayUtils.argsort(input, true));
	}
}
