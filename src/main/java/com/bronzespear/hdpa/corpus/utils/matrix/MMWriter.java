package com.bronzespear.hdpa.corpus.utils.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.List;

public class MMWriter {
	private static final String HEADER = "%%MatrixMarket matrix coordinate integer general\n";
	
	private File file;
	private PrintWriter writer;
	private int m; // rows
	private int n; // cols
	private int nnz;
	private boolean adjustZeroBasedIds = true;
	
	public MMWriter(File file) {
		this.file = file;
	}
	
	public void open() throws IOException {
		writer = new PrintWriter(file, "UTF-8");
		writeHeaders();
	}
	
	public void close() throws IOException {
		writer.close();
		updateHeaders();
	}
		
	private void writeHeaders() {
		writer.print(HEADER);
		writer.printf("%50s\n", ""); // allows 48 digits for matrix size info
	}
	
	private void updateHeaders() throws IOException {
		String stats = String.format("%d %d %d", m, n, nnz);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(HEADER.getBytes("UTF-8").length);
		raf.write(stats.getBytes("UTF-8"));
		raf.close();
	}
	
	public void appendRow(MMRow row) {
		List<MMColumn> columns = row.columns;
		Collections.sort(columns);
		for (MMColumn column : columns) {
			appendColumn(row.id, column);
		}
		
		m++;
	}
	
	private void appendColumn(int rid, MMColumn column) {
		int cid = column.id;
		
		if (adjustZeroBasedIds) {
			rid++;
			cid++;
		}
		
		writer.print(rid);
		writer.print(' ');
		writer.print(cid);
		writer.print(' ');
		writer.println(column.value);
		n = Math.max(n, cid); 
		nnz++;
	}

	public void flush() {
		writer.flush();
	}
}
