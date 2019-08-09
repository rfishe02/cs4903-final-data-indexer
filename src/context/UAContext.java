package context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UAContext {
	UAHashTable gh;
	
	public UAContext(int hashTableSize) {
		gh = new UAHashTable(hashTableSize);
	}
	
	public void buildTermContextMatrix(String inputDirectory, String outputDirectory) {
		try {
			File[] files = new File(inputDirectory).listFiles();
			new File(outputDirectory).mkdirs();
			BufferedWriter logs = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDirectory + "/errors.log"), StandardCharsets.UTF_8));
			int distictWords = fillGlobalHashTableGetDistictWords(files, logs);
			
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
				
				for (String word : words.keySet()) {
					if (gh.search(word) != null) {
						gh.search(word).setCount(gh.search(word).getCount() + words.get(word));
					} else {
						gh.insert(new UARecord(word, words.get(word), 0));
						termId++;
					}
				}
			} catch(Exception e) {
				logs.write("Error reading: " + file.getAbsolutePath());
			}
		}
		
		return termId;
	}

}
