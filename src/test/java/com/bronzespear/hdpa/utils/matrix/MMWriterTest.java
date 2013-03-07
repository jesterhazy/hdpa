package com.bronzespear.hdpa.utils.matrix;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.bronzespear.hdpa.corpus.utils.matrix.MMRow;
import com.bronzespear.hdpa.corpus.utils.matrix.MMWriter;

public class MMWriterTest {

	@Test
	public void test() throws Exception {
		
		Map<Integer, Integer> values = new HashMap<Integer, Integer>();
		values.put(1, 2);
		values.put(2, 3);
		
		File f = File.createTempFile("test", null);
		System.out.println(f.getAbsolutePath());
		MMWriter writer = new MMWriter(f);
		writer.open();
		writer.appendRow(new MMRow(1, values));
		writer.close();
		
		// maybe some testing should happen here!
	}
}
