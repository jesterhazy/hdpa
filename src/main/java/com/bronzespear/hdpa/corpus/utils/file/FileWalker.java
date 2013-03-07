package com.bronzespear.hdpa.corpus.utils.file;

import java.io.File;

public class FileWalker {
	public static interface Visitor {
		public void visit(File file);
	}
	
	public static class DefaultVisitor implements Visitor {
		public void visit(File file) {
			if (file.isDirectory()) {
				visitDirectory(file);
			}
			
			else {
				visitFile(file);
			}
		}

		public void visitFile(File file) {
			System.out.println("f: " + file.getAbsolutePath());
		}

		public void visitDirectory(File file) {
			System.out.println("d: " + file.getAbsolutePath());
		}
	}

	private Visitor visitor;
	private int depthLimit;
	
	public FileWalker() {
		this(new DefaultVisitor());
	}
	
	public FileWalker(Visitor visitor) {		
		this(visitor, 0);
	}
	
	public FileWalker(Visitor visitor, int depthLimit) {
		this.visitor = visitor;
		this.depthLimit = depthLimit;		
	}
	
	public void walk(File file) {
		walk(file, 0);
	}
	
	private void walk(File file, int depth) {
		visitor.visit(file);
		if (file.isDirectory()) {
			if (depthLimit == 0 || depth < depthLimit) {
				File[] files = file.listFiles();
				for (File child : files) {
					walk(child, depth + 1);
				}
			}
		}
	}
}
