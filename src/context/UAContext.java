package context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UAContext {
	private static final long LONG_SIZE = 8;
	private static final long FLOAT_SIZE = 4;
	
	private UAHashTable gh;
	private long termContextRecordSize;
	
	public UAContext(int hashTableSize) {
		gh = new UAHashTable(hashTableSize);
	}
	
	public void buildTermContextMatrix(String inputDirectory, String outputDirectory, int windowSize) {
		try {
			File[] files = new File(inputDirectory).listFiles();
			new File(outputDirectory).mkdirs();
			BufferedWriter logs = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDirectory + "/errors.log"), StandardCharsets.UTF_8));
			int distictWords = fillGlobalHashTableGetDistictWords(files, logs);
			termContextRecordSize = distictWords * LONG_SIZE;
			fillLongs(files, outputDirectory, logs, windowSize);
			
			logs.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private int fillGlobalHashTableGetDistictWords(File[] files, BufferedWriter logs) throws IOException {
		int termId = 0;
		String line;
		BufferedReader in;
		Map<String, Long> words = new HashMap<>();
		
		for (File file : files) {
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
				
				while((line = in.readLine()) != null) {
					if (!words.containsKey(line)) {
						words.put(line, 1L);
					} else {
						words.put(line, words.get(line) + 1);
					}
				}
				
				in.close();
				
				for (String word : words.keySet()) {
					if (gh.search(word) != null) {
						gh.search(word).setCount(gh.search(word).getCount() + words.get(word));
					} else {
						gh.insert(new UARecord(word, words.get(word), 0, termId));
						termId++;
					}
				}
			} catch(Exception e) {
				logs.write("Error reading: " + file.getAbsolutePath());
			}
		}
		
		return termId;
	}
	
	private void fillLongs(File[] files, String outputDirectory, BufferedWriter logs, int windowSize) throws IOException {
		
	}
	
	private void makeRAFFile(String outputDirectory) throws IOException {
		try {
			RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "rw");
			RandomAccessFile dict = new RandomAccessFile(outputDirectory + "/dict.raf", "rw");
			UARecord[] records = gh.getTable();
			
			for (int i = 0; i < records.length; i++) {
				
			}
			
			longs.close();
			dict.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
