package com.bronzespear.hdpa.corpus.utils.matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MMReader {
	private File file;
	private BufferedReader reader;
	private int m, n, nnz;
	private MMRow currentRow;
	private boolean adjustZeroBasedIds = true;

	public MMReader(File file) throws IOException {
		this.file = file;
		open();
	}

	public void open() throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		readDimensions();
	}

	private void readDimensions() throws IOException {
		reader.readLine(); // header
		String line = reader.readLine(); // dimensions
		
		if (line != null) {
			String[] dimensions = line.split(" ");
			m = Integer.parseInt(dimensions[0]);
			n = Integer.parseInt(dimensions[1]);
			nnz = Integer.parseInt(dimensions[2]);
		}
	}
	
	public MMRow readRow() throws IOException {
		MMRow row = null;
		
		String line = null;
		while((line = reader.readLine()) != null) {
			int[] values = parse(line);
			
			if (currentRow == null) {
				currentRow = createRow(values);
			}
			
			else if (currentRow.id == values[0]) {
				currentRow.addColumn(values[1], values[2]);
			}		
		
			else {
				row = currentRow;
				currentRow = createRow(values);
				break;
			}
		}
		
		if (row == null) {
			row = currentRow;
			currentRow = null;
		}
				
		return row;
	}

	private MMRow createRow(int[] values) {
		MMRow row = new MMRow(values[0]);
		row.addColumn(values[1], values[2]);
		
		return row;
	}
	
	private int[] parse(String line) {
		String[] unparsed = line.split(" ");
		int[] parsed = new int[unparsed.length];
		
		for (int i = 0; i < unparsed.length; i++) {
			parsed[i] = Integer.parseInt(unparsed[i]);
		}
		
		if (adjustZeroBasedIds) {
			parsed[0]--;
			parsed[1]--;
		}
		return parsed;
	}
	

	public void close() throws IOException {
		reader.close();
	}
	
	public int getRows() {
		return m;
	}
	
	public int getColumns() {
		return n;
	}
	
	public int size() {
		return nnz;
	}
}
