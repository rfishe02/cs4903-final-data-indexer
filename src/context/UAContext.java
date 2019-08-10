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
	private static final int INT_SIZE = 4;
	private static final int FLOAT_SIZE = 4;
	private static final int DICT_RECORD_LENGTH = 14;
	private static final float ALPHA = 0.75f;
	
	private UAHashTable gh;
	private Map<String, Integer> termToPosMap;
	private long termContextRecordSize;
	private long totalCount;
	private float allToAlpha;
	
	public UAContext(int hashTableSize) {
		gh = new UAHashTable(hashTableSize);
		termToPosMap = new HashMap<>(hashTableSize);
		totalCount = 0;
		allToAlpha = -1;
	}
	
	public boolean buildTermContextMatrix(File[] files, String outputDirectory, int windowSize) {
		try {
			if (new File(outputDirectory).exists()) {
				return false;
			} else {
				new File(outputDirectory).mkdirs();
				BufferedWriter logs = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDirectory + "/errors.log"), StandardCharsets.UTF_8));
				termContextRecordSize = fillGlobalHashTableGetDistictWords(files, logs);
				System.out.println("Done with global hash table");
				saveDictionary(outputDirectory);
				fillInts(files, outputDirectory, logs, windowSize);
				logs.close();
				return true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
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
				
//				for (String word : words.keySet()) {
//					if (!termToPosMap.containsKey(word)) {
//						termToPosMap.put(word, termId);
//						termId++;
//					}
//				}
				
				for (String word : words.keySet()) {
					if (gh.search(word) != null) {
						gh.search(word).setCount(gh.search(word).getCount() + words.get(word));
					} else {
						gh.insert(new UARecord(word, words.get(word), 0, termId));
//						posToTermMap.put(termId, word);
						termId++;
					}
				}
				
				
			} catch(Exception e) {
				logs.write("Error reading: " + file.getAbsolutePath());
				logs.newLine();
			}
			System.out.println("Done with a file for global hash table");
		}
		
		return termId;
	}
	
	private void fillInts(File[] files, String outputDirectory, BufferedWriter logs, int windowSize) throws Exception {
		List<String> words;
		BufferedReader in;
		Map<Integer, Map<Integer, Integer>> denseMatrix = null;
		String word;
		long[] counts = new long[(int)termContextRecordSize];
		int i;
		int j;
		int k;
		int pos1;
		int pos2;
		
		RandomAccessFile ints = new RandomAccessFile(outputDirectory + "/ints.raf", "rw");
		
		for (i = 0; i < files.length; i++) {
			try {
				if (i % 10 == 0) {
					if (denseMatrix != null) {
						writeMatrix(denseMatrix, ints);
					}
					
					denseMatrix = new HashMap<>(1000);
				}
				reinitializeArray(counts);
				in = new BufferedReader(new InputStreamReader(new FileInputStream(files[i])));
				words = new ArrayList<>(5000);
				
				while ((word = in.readLine()) != null) {
					words.add(word);
				}
				
				for (j = 0; j < words.size(); j++) {
					System.out.println(words.get(j));
					word = words.get(j);
					pos1 = gh.search(word).getPosition();
					System.out.println(pos1);
					if (!denseMatrix.containsKey(pos1)) {
						denseMatrix.put(pos1, new HashMap<>(100));
					}
					
					for (k = 1; k <= windowSize; k++) {
						if (j - k >= 0) {
							pos2 = gh.search(words.get(j - k)).getPosition();
							if (denseMatrix.get(pos1).containsKey(pos2)) {
								denseMatrix.get(pos1).put(pos2, denseMatrix.get(pos1).get(pos2) + 1);
							} else {
								denseMatrix.get(pos1).put(pos2, 1);
							}
						}
						if (j + k < words.size()) {
							pos2 = gh.search(words.get(j + k)).getPosition();
							if (denseMatrix.get(pos1).containsKey(pos2)) {
								denseMatrix.get(pos1).put(pos2, denseMatrix.get(pos1).get(pos2) + 1);
							} else {
								denseMatrix.get(pos1).put(pos2, 1);
							}
						}
					}
//					pos = j;
//					while (!used.contains(word) && pos < words.size()) {
//						for (k = 1; k <= windowSize; k++) {
//							if (pos - k >= 0) {
//								counts[gh.search(words.get(pos - k)).getPosition()]++;
////								counts[termToPosMap.get(word)]++;
//								totalCount++;
//							}
//							if (pos + k < words.size()) {
//								counts[gh.search(words.get(pos + k)).getPosition()]++;
////								counts[termToPosMap.get(word)]++;
//								totalCount++;
//							}
//						}
//						for (k = pos + 1; k < words.size(); k++) {
//							if (words.get(k).equals(word)) {
//								break;
//							}
//						}
//						pos = j;
//					}
//					used.add(word);
//					updateLongRecord(gh.search(word).getPosition(), counts, longs);
//					updateLongRecord(termToPosMap.get(word), counts, longs);
				}
				
				System.out.println("Done with a long file");
				
				in.close();
			} catch(Exception e) {
				logs.write("Error reading: " + files[i].getAbsolutePath());
				logs.newLine();
			}
		}
		
		writeMatrix(denseMatrix, ints);
		ints.close();
	}
	
	private void writeMatrix(Map<Integer, Map<Integer, Integer>> denseMatrix, RandomAccessFile ints) throws Exception {
		for (Integer pos1 : denseMatrix.keySet()) {
			for (Integer pos2 : denseMatrix.get(pos1).keySet()) {
				ints.seek(pos1 * termContextRecordSize * INT_SIZE + pos2 * INT_SIZE);
//				System.out.println(pos1 + " " + pos2 + " " + denseMatrix.get(pos1).get(pos2));
				try {
					ints.writeInt(ints.readInt() + denseMatrix.get(pos1).get(pos2));
				} catch(Exception e) {
					ints.writeInt(denseMatrix.get(pos1).get(pos2));
				}
			}
		}
	}
	
	private void reinitializeArray(long[] counts) {
		for (int i = 0; i < counts.length; i++) {
			counts[i] = 0;
		}
	}
	
	private void fillFloats(String outputDirectory, BufferedWriter logs) throws Exception {
		RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "r");
		RandomAccessFile tc = new RandomAccessFile(outputDirectory + "/term-context.raf", "rw");
		
		for (int i = 0; i < termContextRecordSize; i++) {
			for (int j = 0; j < termContextRecordSize; j++) {
				tc.seek((i * termContextRecordSize * (FLOAT_SIZE)) + (j * (FLOAT_SIZE)));
				tc.writeFloat(ppmi(i, j, longs));
			}
			System.out.println("Done with a float row");
		}
		System.out.println("Done with ppmis");
		
		longs.close();
		tc.close();
	}
	
	private float ppmi(int firstWord, int secondWord, RandomAccessFile longs) throws Exception {
		longs.seek((firstWord * termContextRecordSize * (INT_SIZE)) + (firstWord * (INT_SIZE)));
		long countFirstWord;
		try {
			countFirstWord = longs.readLong();
		} catch(Exception e) {
			countFirstWord = 0;
		}
		float probFirst = (float)countFirstWord / totalCount;
		longs.seek((secondWord * termContextRecordSize * (INT_SIZE)) + (secondWord * (INT_SIZE)));
		long countSecondWord;
		try {
			countSecondWord = longs.readLong();
		} catch(Exception e) {
			countSecondWord = 0;
		}
		float probSecond = getProb(countSecondWord, longs);
		longs.seek((firstWord * termContextRecordSize * (INT_SIZE)) + (secondWord * (INT_SIZE)));
		long countFirstAndSecond;
		try {
			countFirstAndSecond = longs.readLong();
		} catch(Exception e) {
			countFirstAndSecond = 0;
		}
		float probFirstAndSecond = (float)countFirstAndSecond / totalCount;
		
		if (Math.abs(probFirst) < 0.0001 || Math.abs(probSecond) < 0.0001 || Math.abs(probFirstAndSecond) < 0.0001) {
			return 0;
		}
		
		return Math.max(log2(probFirstAndSecond / (probFirst * probSecond)), 0);
	}
	
	private float getProb(long countWord, RandomAccessFile longs) throws Exception {
		if (allToAlpha == -1) {
			allToAlpha = 0;
			long count;
			for (int i = 0; i < termContextRecordSize; i++) {
				for (int j = 0; j < termContextRecordSize; j++) {
					longs.seek((i * termContextRecordSize * (INT_SIZE)) + (j * (INT_SIZE)));
					try {
						count = longs.readLong();
						allToAlpha += ((float)Math.pow(count, ALPHA));
					} catch(Exception e) {
						
					}
				}
			}
		}
		
		return (float)countWord / allToAlpha;
	}
	
	private void saveDictionary(String outputDirectory) throws Exception {
		RandomAccessFile dict = new RandomAccessFile(outputDirectory + "/dict.raf", "rw");
		UARecord[] records = gh.getTable();
		
		for (int i = 0; i < records.length; i++) {
			dict.seek(i * (DICT_RECORD_LENGTH + 2));
			if (records[i] != null) {
				dict.writeUTF(formatDictRecord(records[i].getWord(), records[i].getPosition()));
			} else {
				dict.writeUTF(formatDictRecord("-1", -1));
			}
		}
		
		System.out.println("Done writing dictionary random access file");
		
		dict.close();
	}
	
	private void makeRAFFile(String outputDirectory) throws IOException {
		try {
			RandomAccessFile longs = new RandomAccessFile(outputDirectory + "/longs.raf", "rw");
			RandomAccessFile dict = new RandomAccessFile(outputDirectory + "/dict.raf", "rw");
//			UARecord[] records = gh.getTable();
			
//			for (int i = 0; i < records.length; i++) {
//				dict.seek(i * (DICT_RECORD_LENGTH + 2));
//				if (records[i] != null) {
//					dict.writeUTF(formatDictRecord(records[i].getWord(), records[i].getPosition()));
////					fillFullLongRecord(records[i].getPosition(), records[i].getCount(), longs);
//				} else {
//					dict.writeUTF(formatDictRecord("-1", -1));
//				}
//			}
			
			System.out.println("Done with dictionary random access file");
			
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
	
	private void fillFullLongRecord(int position, long count, RandomAccessFile longs) throws Exception {
		for (int i = 0; i < termContextRecordSize; i++) {
			longs.seek((position * termContextRecordSize * (INT_SIZE)) + (i * (INT_SIZE)));
			longs.writeLong(0);
		}
		System.out.println("Done with initial long record vector");
	}
	
	private void updateLongRecord(int wordPos, long[] counts, RandomAccessFile longs) throws Exception {
		for (int i = 0; i < termContextRecordSize; i++) {
			if (counts[i] != 0) {
				longs.seek((wordPos * termContextRecordSize * INT_SIZE) + (i * INT_SIZE));
				try {
					longs.writeLong(longs.readLong() + counts[i]);
				} catch(Exception e) {
					longs.writeLong(counts[i]);
				}
			}
		}
	}
	
//	private void updateLongRecord(int firstPos, int secondPos, RandomAccessFile longs) throws Exception {
//		longs.seek((firstPos * termContextRecordSize * (LONG_SIZE)) + (secondPos * (LONG_SIZE)));
//		longs.writeLong(longs.readLong() + 1);
//	}
	
	private float log2(float x) {
		return (float)(Math.log(x) / Math.log(2));
	}
	
	public static void main(String[] args) {
		UAContext context = new UAContext(150001);
		File[] files = new File("output/test").listFiles();
		context.buildTermContextMatrix(files, "output/testout", 3);
		System.out.println(context.termContextRecordSize);
	}

}
