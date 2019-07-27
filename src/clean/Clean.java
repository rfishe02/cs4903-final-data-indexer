package clean;

import java.io.*;

public class Clean {

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			args = new String[2];
			args[0] = "output/tokenize";
			args[1] = "output/clean";
		}

		try {

			BufferedInputStream bis;
			BufferedWriter bw;

			File input = new File(args[0]);
			File output = new File(args[1]);
			File[] files = input.listFiles();

			for (File f : files) {

				bis = new BufferedInputStream(new FileInputStream(f));
				bw = new BufferedWriter(new FileWriter(output.getPath() + "/" + f.getName()));

				process(bw,bis); // Perform some actions on the file. We could calculate frequencies, etc.

				bw.close();
				bis.close();

			}

		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

	}

	public static void process(BufferedWriter bw, BufferedInputStream bis) throws IOException {

		StringBuilder sb = new StringBuilder();
		
		// We could use a Buffered Reader here.
		
		int i;
		while( ( i = bis.read() ) != -1 ){    
			
			if(i != 10 && i != 13) {
				sb.append((char)i); // Append to str, until we reach a newline or cr (Would only work for our tokenized files). 
			
				// We could optionally handle characters here.
				// Note: It reads one byte at a time, but we can't expact all chars to be a byte.
			
			} else {
				
				bw.write(sb.toString()+"\n");
				sb.setLength(0); // This may be faster than making a new instance.
				
			} 
			
		}    

	}

}
