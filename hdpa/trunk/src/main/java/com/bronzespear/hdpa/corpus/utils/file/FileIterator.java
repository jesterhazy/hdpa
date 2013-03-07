package com.bronzespear.hdpa.corpus.utils.file;

import java.io.File;
import java.util.Arrays;

public class FileIterator extends QueueIterator<File> {
	
	public FileIterator(File root) {
		super(root);
	}
	
	protected void expand(File current) {
		if (current.isDirectory()) {
			inQueue.addAll(Arrays.asList(current.listFiles()));
			expand();
		}
		
		else {
			outQueue.add(current);
		}		
	}
}
