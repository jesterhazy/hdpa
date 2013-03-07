package com.bronzespear.hdpa.corpus.utils.file;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public abstract class QueueIterator<T> implements Iterator<T> {
	Queue<T> outQueue;
	Queue<T> inQueue;
	
	public QueueIterator(T root) {
		this.outQueue = new LinkedList<T>();
		this.inQueue = new LinkedList<T>();
		this.inQueue.add(root);
	}
	
	public boolean hasNext() {
		if (outQueue.isEmpty()) {
			expand();
		}
		return !outQueue.isEmpty();
	}
	
	protected void expand() {
		if (!inQueue.isEmpty()) {
			T current = inQueue.remove();			
			expand(current);
		}
	}
	
	abstract protected void expand(T current);

	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		return outQueue.poll();	
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
