package com.bronzespear.hdpa.corpus.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rewrite all .mm files in a directory with all row and column ids adjusted by an offset (currently +1). Saves original files as <filename>.old.
 * 
 * Usage: java RebaseMM <path>
 */
public class RebaseMM {
	private static final Log LOG = LogFactory.getLog(RebaseMM.class);
	private int offset;
	private File dir;
	
	public RebaseMM(String path) {
		dir = new File(path);
		offset = 1;
	}

	public static void main(String[] args) throws Exception {
		String path = args[0];
		
		RebaseMM app = new RebaseMM(path);
		app.rebase();
	}
	
	private void rebase() throws IOException {
		LOG.info("rebasing files in: " + dir.getAbsolutePath());
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".mm");
			}
		});
		
		LOG.info(String.format("found %d files", files.length));
		
		for (File file : files) {
			rebase(file);
		}
	}

	private void rebase(File file) throws IOException {
		LOG.info("updating file: " + file.getAbsolutePath());
		
		// move the input file
		File input = new File(file.getParentFile(), file.getName().replaceAll(".mm$", ".mm.old")); 
		file.renameTo(input);
		
		BufferedReader reader = new BufferedReader(new FileReader(input));
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
		
		int linesRead = 0;
		String line = null;
		while((line = reader.readLine()) != null ) {
			if (linesRead < 2) {
				writer.println(line);
				
				if (linesRead == 1) {
					String[] parts = line.split(" ");
					LOG.info(String.format("input matrix has %s rows and %s columns", parts[0], parts[1]));
				}
			}
			
			else {
				String[] parts = line.split(" ");
				int rid = Integer.parseInt(parts[0]) + offset;
				int cid = Integer.parseInt(parts[1]) + offset;
				
				writer.printf("%d %d %s\n", rid, cid, parts[2]);
			}
			
			linesRead++;
		}
		
		LOG.info(linesRead + " lines read");
		
		writer.close();
		reader.close();
		
		LOG.info("finished file: " + file.getAbsolutePath());
	}
}
