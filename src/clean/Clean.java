
import java.util.HashMap;
import java.util.HashSet;
import java.io.*;
import java.util.Queue;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;

public class Clean {

  static HashMap<String,Integer> allDoc;
  static HashMap<String,Integer> thisDoc;

  static int docs = 0;

  public static void main(String[] args) {
    File input = new File(args[0]);
    File output = new File(args[1]);

    calcDocFreq(input);
    cleanFiles(input,output);
  }

  public static String formatString(String s) {

    String out = "";
    int len;

    len = Math.min(s.length(),8);

    for(int i = 0; i < len; i++) {
        if((int)s.charAt(i) > 127) {
            out += "?";
        } else if ( (int)s.charAt(i) > 47 && (int)s.charAt(i) < 58 || (int)s.charAt(i) > 96 && (int)s.charAt(i) < 123 ) {
            out += s.charAt(i);
        } else if( (int)s.charAt(i) > 64 && (int)s.charAt(i) < 91 ) {
            out += (char)((int)s.charAt(i) + 32);
        }
    }

    if(out.length() > 7) {
      out = out.substring(0,8);
    }

    return out;

  }

  public static void countTerm(HashMap<String,Integer> map, String s) {
    if(map.containsKey(s)) {
      map.put(s,map.get(s)+1);
    } else {
      map.put(s,1);
    }
  }

  public static void calcDocFreq(File inDir) {
    HashSet<String> set;
    BufferedReader br;
    String read;

    String[] spl;

    try {
      File[] files = inDir.listFiles();
      allDoc = new HashMap<>(3000000);

      for(File f : files) {
        br = new BufferedReader(new FileReader(f));
        set = new HashSet<String>(10000);

        while((read=br.readLine())!=null) {

          spl = read.split("([\\s-&])+");

          for(String s : spl) {
            s = formatString(s);

            if( !set.contains(s) ){
              countTerm(allDoc,s);
              set.add(s);
            }

          }

        }
        docs++;
        br.close();
      }

    } catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static void cleanFiles(File inDir, File outDir) {
    HashSet<String> vocab = new HashSet<String>(100000);
    HashMap<String,Integer> thisDoc;
    Queue<String> terms;
    BufferedReader br;
    BufferedWriter bw;
    String temp;
    double idf;
    double tfIDF;

    int docSize;

    String[] spl = null;

    try {
      File[] files = inDir.listFiles();

      for(File f : files) {
        br = new BufferedReader(new FileReader(f));
        bw = new BufferedWriter(new FileWriter(outDir.getPath()+"/"+f.getName()));

        thisDoc = new HashMap<>(10000);
        terms = new LinkedList<>();

        docSize = 0;

        while((temp=br.readLine())!=null) {

          spl = temp.split("([\\s-&])+");

          for(String s : spl) {
            s = formatString(s);
            countTerm(thisDoc,s);

            terms.add(s);
            docSize++;
          }

        }

        while(!terms.isEmpty()) {
          temp = terms.remove();

          idf = Math.log( (double)docs / allDoc.get(temp) );
          tfIDF = ( (double)thisDoc.get(temp) ) * idf;

          if( idf > 1.3 && thisDoc.get(temp) > 1 && allDoc.get(temp) > 20 ) {

            //bw.write(  String.format(  "%20s tfidf: %-12.4f idf: %-12.4f all: %-8d this: %-8d rtf: %-12.4f \n", temp, tfIDF, idf, allDoc.get(temp), thisDoc.get(temp), ( (double)thisDoc.get(temp)/docSize )  )  );
            bw.write(temp+"\n");

            if(!vocab.contains(temp)) {
              vocab.add(temp);
            }
          }

        } // end while loop

        br.close();
        bw.close();
      } // end for loop

    } catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    System.out.println(vocab.size());

  }

  public static void writeVocab(HashSet<String> vocab) throws IOException {
    Iterator<String> it = vocab.iterator();
    BufferedWriter bw = new BufferedWriter(new FileWriter("vocab.txt"));

    while (it.hasNext()) {
      bw.write(it.next()+"\n");
    }
    bw.close();
  }

}
