/******************************
 * Name: 		Martin Tran
 * Username:	uatext
 * Problem Set:	PS3
 * Due Date:	7-30-19
 ******************************/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
    
    public class UAInvertedIndex {
    	
    	protected GlobalHashTable ght;
    	
		public static void main(String[] args) throws IOException, FileNotFoundException {
			UAInvertedIndex index = new UAInvertedIndex();
			index.buildInvertedIndex("./processedTokens/", "./invertedIndexFiles"); 
		}	
		
		/**
		 * Container class to hold various different information
		 * about a word.
		 */
		
		public static class GlobalWord {
			
			protected int termId, numDocs, count;
			protected int start;
			protected String word;
			protected LinkedList<Integer> link;
			
			public GlobalWord(String word) {
				this.word = word;
				start = 0;
				link = new LinkedList<Integer>();
				numDocs = 0;
				count = 1;
			}

			public int getStart() {
				return start;
			}

			public void setStart(int start) {
				this.start = start;
			}

			public String getWord() {
				return word;
			}

			public void setTermId(int termId) {
				this.termId = termId;
			}
			
			public boolean equals(GlobalWord gw) {
				if (this.word.equals(gw.word)) {
					return true;
				}
				return false;
			}
			
			/**
			 * Resizing function that makes the original hash table
			 * 1.5x bigger. 
			 * @param ght The table to be resized
			 * @return Resized hash table
			 * Time Complexity: O(V)
			 * Space Complexity: O(V^2)
			 */
			
			public GlobalWord[] resize(GlobalHashTable ght) {
				
				GlobalWord[] temp = new GlobalWord[(int) (ght.globalTable.length * 1.5)];
				
				for (int i = 0; i < ght.globalTable.length; i++) {
					if (ght.globalTable[i] != null) {
						int increment = 0;
						int code = hash(ght.globalTable[i], increment, ght);
						while (ght.globalTable[i].equals(temp[code])) {
							code = hash(ght.globalTable[i], increment, ght);
						}
						temp[code] = ght.globalTable[i];
					}
				}
				
				return temp;
			}
			
			/**
			 * Hashing function for the word
			 * @param word The word to be hashed
			 * @param increment Increment value used for linear probing
			 * @param ght Used to get the length of the global hash table
			 * @return Position to hash to
			 * Time Complexity: O(1)
			 * Space Complexity: O(V)
			 */
			
			public int hash(GlobalWord word, int increment, GlobalHashTable ght) {
				return Math.abs((word.word.hashCode() + increment) % ght.globalTable.length);
			}

			public String toString() {
				return word;
			}
			
		}
		
		
		/**
		 * Custom hash table in order to write to the dict.raf file
		 * 
		 *
		 */
		public static class GlobalHashTable {
			
			protected GlobalWord[] globalTable;
			protected int currentSize;
			
			public GlobalHashTable() {
				globalTable = new GlobalWord[150000];
				currentSize = 0;
			}
			
			/**
			 * Get function to retrieve a GlobalWord
			 * @param word "String" word to get back a GlobalWord
			 * @return GlobalWord found
			 * Time Complexity: O(1)
			 * Space Complexity: O(V)
			 */
			
			public GlobalWord get(GlobalWord input) {
				
				if(input == null) {
					return null;
				}
				
				int increment = 0;
				int code = input.hash(input, increment, this);
				int i = 0;
				while (globalTable[code] != null && !input.word.equals(globalTable[code].word)) {
					if (i == globalTable.length)
						return null;
					increment++;
					code = input.hash(input, increment, this);
					i++;
				}
				return globalTable[code];
			}
			
			public int getIndex(GlobalWord input) {
				int increment = 0;
				int code = input.hash(input, increment, this);
				int i = 0;
				while (globalTable[code] != null && !input.word.equals(globalTable[code].word)) {
					if (i == globalTable.length)
						return -1;
					increment++;
					code = input.hash(input, increment, this);
					i++;
				}
				return code;
			}
			
			public boolean remove(GlobalWord word) {
				int index = getIndex(word);
				if (index == -1) {
					return false;
				} else {
					globalTable[index] = null;
					return true;
				}
			}
			
			/**
			 * Put function to place a GlobalWord into the hash table
			 * @param word Word to be put in the hash table
			 * Time Complexity: O(1)
			 * Space Complexity: O(V)
			 */
			public void put(GlobalWord word) {
				
				if (word == null) {
					return;
				}
				int power = 0;
				int code = word.hash(word, power, this);
				
				if (currentSize / globalTable.length >= 0.33) {
					globalTable = word.resize(this);
				}
				
				while (globalTable[code] != null) {
					if (word.equals(globalTable[code])) {
						return;
					}
					
					power++;
					code = word.hash(word, power, this);
				}
				
				globalTable[code] = word;
			}
			

			
			
			
		}
		
		
		/**
		 * Building of the inverted index. Used to build the mapRaf.raf, dict.raf, and post.raf
		 * @param inputDirectory Directory of pre-processed tokens
		 * @param outputDirectory Directory to store generated .temp files 
		 * @throws IOException Handles any IO errors that may occur
		 * @throws FileNotFoundException Handles any file and directory issues
		 * Time Complexity: O(Dlg(D^2*d^3))
		 * Space Complexity: O(V^2*log(D))
		 */
		public void buildInvertedIndex(String inputDirectory, String outputDirectory) throws IOException, FileNotFoundException {
		
				GlobalHashTable globalHashTable = new GlobalHashTable();
				//HashMap<String, GlobalWord> index = new HashMap<String, GlobalWord>();
				HashMap<String, Integer> docHashTable;

				BufferedReader reader;
				BufferedWriter writer;
				File[] files = new File(inputDirectory).listFiles();
				RandomAccessFile mapRaf = new RandomAccessFile("./raf/mapRaf.raf", "rw");
				BufferedWriter bw = new BufferedWriter(new FileWriter("./raf/mapRaf.txt"));
				int termId = 0;
				
				for (int i = 0; i < files.length; i++) {

					int totalFreq = 0;
					docHashTable = new HashMap<String, Integer>();
					reader = new BufferedReader(new FileReader(inputDirectory + i + "p.html.out"));
					if (i <= files.length / 2 )
						writer = new BufferedWriter(new FileWriter(outputDirectory + "/doc" + i + ".temp"));
					else 
						writer = new BufferedWriter(new FileWriter(outputDirectory + "1/doc" + i + ".temp"));
					String input = reader.readLine();
					
					while (input != null) {

						if (docHashTable.get(input) == null) {
							docHashTable.put(input, 1);
							totalFreq++;

						} else {
							docHashTable.put(input, docHashTable.get(input) + 1);
							totalFreq++;
						}
						

						
						input = reader.readLine();
					}

					Set<String> set = docHashTable.keySet();
					Object[] terms = set.toArray();
					
					for (int y = 0; y < terms.length; y++) {
						
						GlobalWord current = new GlobalWord((String)terms[y]);
						if (globalHashTable.get(current) == null) {
							globalHashTable.put(current);
							globalHashTable.get(current).setTermId(termId);
							termId++;
						} else {
							globalHashTable.get(current).count++;
						}	
						
						if (globalHashTable.get(current).link.contains(i) == false) {
							globalHashTable.get(current).link.add(i);
							globalHashTable.get(current).numDocs++;
						}
						
	
					}
					
					if (terms.length > 0) {
						Arrays.sort(terms, 0, terms.length - 1);
					} else {
						continue;
					}
					
					

					for (int x = 0; x < terms.length; x++) {
						
						GlobalWord current = new GlobalWord((String)terms[x]);
						Collections.sort(globalHashTable.get(current).link);
							writer.write(String.format("%s,%08d,%.8f", current, i, (float)(docHashTable.get((String)terms[x]) / (float)totalFreq)));
							writer.newLine();
						
					}
				
					mapRaf.seek(i*22);
					String name = files[i].getName();
					if (files[i].getName().length() > 19) {
						name = name.substring(0, 19);
					}
					//s = 19
					if (name.substring(0, name.indexOf("p")).length() < 10) {
						name = String.format("%0" + (10-name.substring(0, name.indexOf("p")).length()) + "d%s", 0, name);
					}
					
					mapRaf.writeUTF(name);
					bw.write(name);
					bw.newLine();
					
					writer.close();
					reader.close();

				//end of big for loop
				}
				bw.close();
				mapRaf.close();
				
		files = new File(outputDirectory + "/").listFiles();
                File[] files2 = new File(outputDirectory + "1/").listFiles();
                mergeFiles(files);
                mergeFiles(files2);
                
                files = fileExistsArr(files);
                files2 = fileExistsArr(files2);
                File[] merged = new File[files.length + files2.length];
                
                for (int i = 0; i < files.length + files2.length; i++) {
                	for (int y = 0; y < files.length; y++) {
                		merged[i] = files[y];
                		i++;
                	}
                	
                	for (int y = 0; y < files2.length; y++) {
                		merged[i] = files2[y];
                		i++;
                	}
                }
                
               
                writePostingRaf(merged, globalHashTable);
                writeDictRaf(globalHashTable);

			//end of buildInvertedIndex
		}


		public void mergeFiles(File[] files) throws IOException {
	                int counter = 0;
	                BufferedReader rightReader = null;
	                BufferedReader leftReader = null;
	                BufferedWriter bw = null;
	                while (files.length > 500) {

                        if (counter >= 500) {
                                counter = 0;
                                files = fileExistsArr(files);
                                System.out.println(files.length);
			}

                                        int numLinesFile1 = -1;
                                        int numLinesFile2 = -1;

            					if (counter + 1 < files.length && files[counter] != null && files[counter + 1] != null && files[counter].isFile() && files[counter + 1].isFile()) {
                                                numLinesFile1 = (int)Files.lines(Paths.get(files[counter].getPath())).count();
                                                numLinesFile2 = (int)Files.lines(Paths.get(files[counter + 1].getPath())).count();
                                        } else {
                                                counter++;
                                                continue;
                                        }

                                        merge(files[counter].getAbsolutePath(), files[counter + 1].getAbsolutePath(), numLinesFile1, numLinesFile2, files, counter, leftReader, rightReader, bw);
                                        counter += 2;

	                }

		}
	
			/**
			 * Counts the amount of existing files left
			 * @param arr File[] to count
			 * @return Number of existing files
			 * Time Complexity: O(D)
			 * Space Complexity: O(D)
			 */
			public int countExists(File[] arr) {
				int count = 0;
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] != null && arr[i].isFile())
						count++;
				}
				return count;
			}
			
			/**
			 * Creates a new File[] to accommodate for deleted files
			 * @param arr File[] to be processed for nonexisting files
			 * @return New File[] that has all files that exists
			 * Time Complexity: O(D)
			 * Space Complexity: O(D^2)
			 */
			public File[] fileExistsArr(File[] arr) {
				int count = countExists(arr);
				File[] temp = new File[count];
				int i = 0;
				int x = 0;
				while (x < count) {
					if (arr[i] == null || !arr[i].isFile()) {
						i++;
						continue;
				} else {
						temp[x] = arr[i];
						x++;
						i++;
					}
				}
				
				return temp;
			}
			
			/**
			 * Helper method to calculate Inverse Document Frequency
			 * @param totalDocs Total number of documents in directory
			 * @param count Total occurrences of a specified word
			 * @return IDF value
			 * Time Complexity: O(1)
			 * Space Complexity: O(1)
			 */
			public float idf(int totalDocs, int count) {
				return (float)Math.log((float)totalDocs / count);
			}

			/**
			 * Merge method to merge together files to fit into about the 1000 file range that Linux can open.
			 * @param file1 First file to be merged
			 * @param file2 Second file to be merged
			 * @param numLinesFile1 Number of lines in file one
			 * @param numLinesFile2 Number of lines in file two
			 * @param files Listing of all the files in the outputDirectory
			 * @param counter Used to delete a file about merging one
			 * @throws IOException Handles any IO errors that may occur during merging
			 * Time Complexity: O(d^3)
			 * Space Complexity: O(D*d^2)
			 */
		private void merge(String file1, String file2, int numLinesFile1, int numLinesFile2, File[] files, int counter, BufferedReader leftReader, BufferedReader rightReader, BufferedWriter bw) throws IOException {
				if (numLinesFile1 == -1 || numLinesFile2 == -1) {
					return;
				}
				
				leftReader  = new BufferedReader(new FileReader(file1));
				rightReader = new BufferedReader(new FileReader(file2));
                                bw = new BufferedWriter(new FileWriter(file1));
			try {				
				String[] leftArr 	= new String[numLinesFile1 + 1];
				String[] rightArr 	= new String[numLinesFile2 + 1];
				
				for (int i = 0; i < numLinesFile1; i++) {
					String input = leftReader.readLine();
					if (input != null) {
						leftArr[i] = input;
					}
				}
				
				leftArr[numLinesFile1] = "-1";
				
				for (int i = 0; i < numLinesFile2; i++) {
					String input = rightReader.readLine();
					if (input != null) {
						rightArr[i] = input;
					}
				}
				
				rightArr[numLinesFile2] = "-1";
				
				int i = 0, j = 0;
				
				for (int k = 0; k < numLinesFile2 + numLinesFile1; k++) {
					
					if (i == numLinesFile1 && leftArr[i].equals("-1")) {
						while (j < numLinesFile2 && rightArr[j] != null && !rightArr[j].equals("-1")) {
							bw.write(rightArr[j]);
							bw.newLine();
							j++;
						}
						break;
					}
					
					if (j == numLinesFile2 && rightArr[j].equals("-1")) {
						while (i < numLinesFile2 && leftArr[i] != null && !leftArr[i].equals("-1")) {
							bw.write(rightArr[i]);
							bw.newLine();
							i++;
						}
						break;
					}
					
					if (i < numLinesFile1 && leftArr[i] != null && leftArr[i].compareTo(rightArr[j]) <= 0) {
						bw.write(leftArr[i]);
						bw.newLine();
						i++;
						
					} else {
						if (j < numLinesFile2 && rightArr[j] != null) {
							bw.write(rightArr[j]);
							j++;
							bw.newLine();
						}
					}
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				rightReader.close();
				leftReader.close();
				bw.close();
			}
				files[counter + 1].delete();


			}
			/**
			 * Method to write the global hash table into the dict.raf file
			 * @param globalTable Global hash table to be written to file
			 * @throws IOException Handles any IO errors that the RandomAccessFile may generate
			 * Time Complexity: O(V)
			 * Space Complexity: O(V)
			 */
			public void writeDictRaf(GlobalHashTable ght) throws IOException {
                RandomAccessFile dictRaf = new RandomAccessFile("./raf/dict.raf", "rw");
                BufferedWriter bw = new BufferedWriter(new FileWriter("./raf/dict.txt"));
                //Size = 22
                for (int i = 0; i < ght.globalTable.length; i++) {

                	GlobalWord current = ght.get(ght.globalTable[i]);
                	if (current != null && current.word.equals("data")) {
                		System.out.println("data");
                	}
                	if (current == null) {
                		dictRaf.seek(i * 29);
                		dictRaf.writeUTF(String.format("%03d%s%05d%06d%08d", 0, "blank", -1, -1, -1));
                		bw.write(String.format("%03d%s%05d%06d%08d", 0, "blank", -1, -1, -1));
                		bw.newLine();
                	} else if (8-current.word.length() > 0){
                		dictRaf.seek(i * 29);
                		dictRaf.writeUTF(String.format("%0" + (8-current.word.length()) + "d%s%05d%06d%08d", 0, current.word, current.termId, current.numDocs, current.start));
                		bw.write(String.format("%0" + (8-current.word.length()) + "d%s%05d%06d%08d", 0, current.word, current.termId, current.numDocs, current.start));
                		bw.newLine();
                	} else {
                		dictRaf.seek(i * 29);
                		dictRaf.writeUTF(String.format("%s%05d%06d%08d", current.word, current.termId, current.numDocs, current.start));
                		bw.write(String.format("%s%05d%06d%08d", current.word, current.termId, current.numDocs, current.start));
                		bw.newLine();
                	}
                }
                bw.close();
                dictRaf.close();
			}
			
			/**
			 * Function to write the postings list to the post.raf file
			 * @param index HashMap of String, GlobalWord for easy retrieval
			 * @param files File[] of all files in directory
			 * @param globalHashTable Global hash table
			 * @throws IOException Handles any IO errors during writing to the post.raf file
			 * Time Complexity: O(D^3)
			 * Space Complexity: O(V^2D^3)
			 */
			public void writePostingRaf(File[] files, GlobalHashTable globalHashTable) throws IOException {
                RandomAccessFile postRaf = new RandomAccessFile("./raf/post.raf", "rw");
                int recordCount = 0;
                BufferedWriter bw = new BufferedWriter(new FileWriter("./raf/post.txt"));
                
            	BufferedReader[] readers = new BufferedReader[files.length];
                
            	for (int i = 0; i < files.length; i++) {
            		if (files[i].isFile()) 
            			readers[i] = new BufferedReader(new FileReader(files[i]));
                }
            	
            	PriorityQueue<String> queue = new PriorityQueue<String>(new FormatQueue());
            	boolean complete = false;
            	
            	for (int i = 0; i < readers.length; i++) {
            		if (readers[i] != null) {
            			String input = readers[i].readLine();
            			if (input != null)
            				queue.add(input + "|" + i);
            		}
            	}
            	String previous = queue.peek();
            	while (complete == false) {	                
                	if (queue.size() == 0) {
                		complete = true;
                		break;
                	}
                    
	                String extracted = queue.poll();
	                int top = Integer.parseInt(extracted.substring(extracted.indexOf("|") + 1));
	                String[] temp = extracted.split(",");
	                extracted = temp[0];
	                GlobalWord gWord = new GlobalWord(extracted);
	                gWord = globalHashTable.get(gWord);
	                if (globalHashTable.get(gWord) == null) {
	                	String current = readers[top].readLine() + "|" + top;
	                	if (current.equals(previous)) {
	                		previous = current;
	                		current = readers[top].readLine() + "|" + top;
	                		if (current.substring(0, current.indexOf("|")).equals("null")) {
	                			readers[top] = null;
	                			continue;
	                		}
	                		while (previous.equals(current)) {
	                			current = readers[top].readLine() + "|" + top;
	                		}
	                	}
	                	previous = current;
	                	queue.add(current);
	                	continue;
	                }

	                globalHashTable.get(gWord).setStart(recordCount);
	                
	                
	                float idf = idf(files.length, globalHashTable.get(gWord).numDocs);
	                float rtfidf = ((float)globalHashTable.get(globalHashTable.get(gWord)).count / files.length) * idf;
	                	
	                Iterator<Integer> iter = globalHashTable.get(gWord).link.iterator();
	                if (extracted.equals("http")) {
	                	System.out.println();
	                }
                	//long position = 0;
	            	int counter = 0;
	                while (globalHashTable.get(gWord) != null && iter.hasNext()) {
	                	int document = iter.next();
	                	int x = (gWord.start + counter) * (27 + 2);
	                	postRaf.seek((gWord.start + counter) * (27 + 2));
	                	
		                if (extracted.equals("http")) {
		                	System.out.println(postRaf.getFilePointer());

		                }
		                
		                
	                	//position = postRaf.getFilePointer();
	                	String rtfidfStr = rtfidf + "";
	                	if (rtfidfStr.length() < 17) {
		                	postRaf.writeUTF(String.format("%010d%0" + (17-rtfidfStr.length()) + "d%.17f", document, 0, rtfidf));
		                	bw.write(String.format(String.format("%010d%0" + (17-rtfidfStr.length()) + "d%.17f", document, 0, rtfidf)));
	                	} else {
		                	postRaf.writeUTF(String.format("%010d%.17f", document, rtfidf));
		                	bw.write(String.format("%010d%.17f", document, rtfidf));
		                	
	                	}
	                	bw.newLine();
	                	counter++;
	                	iter.remove();
	                }
	                
	                if (extracted.equals("http")) {
	                	System.out.println();
	                }
	                
		            recordCount++;
		            
		            queue.add(readers[top].readLine() + "|" + top);
                	queue.remove(extracted + "|" + top);
                }
                
                postRaf.close(); 
                bw.close();
			}
			
			public static class FormatQueue implements Comparator<String> {

				@Override
				public int compare(String o1, String o2) {
					String[] temp1 = o1.split(",");
					String[] temp2 = o2.split(",");
					return temp1[0].compareTo(temp2[0]);
				}
				
			}

		//end of class
		}
		
