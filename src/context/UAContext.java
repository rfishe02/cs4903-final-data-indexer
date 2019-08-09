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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UAContext {
	private static final int LONG_SIZE = 8;
	private static final int FLOAT_SIZE = 4;
	private static final int DICT_RECORD_LENGTH = 14;
	
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
			int distinctWords = fillGlobalHashTableGetDistictWords(files, logs);
			termContextRecordSize = distinctWords;
			fillLongs(files, outputDirectory, logs, windowSize, distinctWords);
			
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
				logs.newLine();
			}
		}
		
		return termId;
	}
	
	private void fillLongs(File[] files, String outputDirectory, BufferedWriter logs, int windowSize, int totalWords) throws Exception {
		List<String> words;
		BufferedReader in;
		String word;
		int i;
		int j;
		
		RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "rw");
		
		for (File file : files) {
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				words = new ArrayList<>(5000);
				
				while ((word = in.readLine()) != null) {
					words.add(word);
				}
				
				for (i = 0; i < words.size(); i++) {
					word = words.get(i);
					for (j = 1; j <= windowSize; j++) {
						if (i - j >= 0) {
							updateLongRecord(gh.search(word).getPosition(), gh.search(words.get(i - j)).getPosition(), totalWords, longs);
						}
						if (i + j < words.size()) {
							updateLongRecord(gh.search(word).getPosition(), gh.search(words.get(i + j)).getPosition(), totalWords, longs);
						}
					}
				}
				
				in.close();
			} catch(Exception e) {
				logs.write("Error reading: " + file.getAbsolutePath());
				logs.newLine();
			}
		}
		
		longs.close();
	}
	
	private void fillFloats(String outputDirectory, int totalWords, BufferedWriter logs) throws Exception {
		RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "r");
		RandomAccessFile tc = new RandomAccessFile(outputDirectory + "/term-context.raf", "rw");
		
		
		longs.close();
		tc.close();
	}
	
	private void makeRAFFile(String outputDirectory, int totalWords) throws IOException {
		try {
			RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "rw");
			RandomAccessFile dict = new RandomAccessFile(outputDirectory + "/dict.raf", "rw");
			UARecord[] records = gh.getTable();
			
			for (int i = 0; i < records.length; i++) {
				dict.seek(i * (DICT_RECORD_LENGTH + 2));
				if (records[i] != null) {
					dict.writeUTF(formatDictRecord(records[i].getWord(), records[i].getPosition()));
					fillFullLongRecord(totalWords, records[i].getPosition(), records[i].getCount(), longs);
				} else {
					dict.writeUTF(formatDictRecord("-1", -1));
				}
			}
			
			longs.close();
			dict.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String formatDictRecord(String term, int position) {
		if (term.length() > 8) {
			term = term.substring(0, 8);
		}
		return String.format("%-" + DICT_RECORD_LENGTH + "s", String.format("%s%06d", term, position));
	}
	
	private void fillFullLongRecord(int totalWords, int position, long count, RandomAccessFile longs) throws Exception {
		for (int i = 0; i < totalWords; i++) {
			longs.seek((position * termContextRecordSize * (LONG_SIZE + 2)) + (i * (LONG_SIZE + 2)));
			longs.writeLong(0);
		}
	}
	
	private void updateLongRecord(int firstPos, int secondPos, int totalWords, RandomAccessFile longs) throws Exception {
		longs.seek((firstPos * termContextRecordSize * (LONG_SIZE + 2)) + (secondPos * (LONG_SIZE + 2)));
		longs.writeLong(longs.readLong() + 1);
	}
	
	private float log2(float x) {
		return (float)(Math.log(x) / Math.log(2));
	}

}
