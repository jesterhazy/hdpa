package com.bronzespear.hdpa;

import org.junit.Assert;
import org.junit.Test;

public class HdpaUtilsTest {
	
	@Test
	public void testRepeatString() throws Exception {
		Assert.assertEquals("-----", HdpaUtils.repeatString("-", 5));
		Assert.assertEquals("-", HdpaUtils.repeatString("-", 0));
		Assert.assertEquals("-", HdpaUtils.repeatString("-", -1));
		Assert.assertEquals("a a a a a ", HdpaUtils.repeatString("a ", 5));
	}
}
