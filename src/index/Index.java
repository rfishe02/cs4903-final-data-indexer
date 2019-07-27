package index;
import java.io.*;

public class Index {

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			args = new String[2];
			args[0] = "output/clean";
			args[1] = "output/index";
		}

		try {

			BufferedInputStream bis;

			File input = new File(args[0]);
			File output = new File(args[1]);
			File[] files = input.listFiles();

			for (File f : files) {

				bis = new BufferedInputStream(new FileInputStream(f));
			
				// Do something with the bis, ie: build indexes.
				
				bis.close();

			}

		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

	}

}
