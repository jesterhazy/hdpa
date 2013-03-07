package com.bronzespear.hdpa.corpus.utils.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

public class TGZFileIterator extends QueueIterator<File> {
	private static final Log LOG = LogFactory.getLog(TGZFileIterator.class);
	private static final String TEMP_FILE_NAME = System.getProperty("java.io.tmpdir") + "/temp.xml";
	private TarInputStream tar;
	private TarEntry entry;
	private File temp;
	private boolean initialized;
	private int filesRead;
	private String currentEntryName;
	
	public TGZFileIterator(File root) {
		super(root);
	}
	
	@Override
	public boolean hasNext() {		
		boolean hasNext = super.hasNext();
		
		if (!hasNext) {
			LOG.info("tar entries read: " + filesRead);

			if (tar != null) {
				try {
					tar.close();
				} catch (IOException e) {
					LOG.error(e, e);
				}
			}
			
			if (temp != null) {
				temp.delete();
			}
		}
		
		return hasNext;
	}
	
	protected void expand(File current) {
		try {
			if (!initialized) {
				// init!
				LOG.info("opening file: " + current.getAbsolutePath());
				tar = new TarInputStream(new GZIPInputStream(
						new BufferedInputStream(new FileInputStream(current))));
				initialized = true;
			}
			
			while ((entry = tar.getNextEntry()) != null) {
				currentEntryName = entry.getName();
				if (!entry.isDirectory()) {
					temp = new File(TEMP_FILE_NAME);
					writeEntryToFile();
					outQueue.add(temp);
					filesRead++;
					break;
				}				
			}
			
			inQueue.add(current);
		}
		catch (Exception e) {			
			LOG.error("error reading tgz file: " + current.getAbsolutePath());
			
			if (entry != null) {
				LOG.error("error reading tar entry: " + entry.getName());	
			}
		}
	}
	
	private void writeEntryToFile() throws IOException {
		int count;
		byte data[] = new byte[2048];
		
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
		while ((count = tar.read(data)) != -1) {			
			os.write(data, 0, count);
		}

		os.flush();
		os.close();	
	}

	public String currentEntryName() {
		return currentEntryName;
	}
}
