
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

    /*
    args = new String[2];
    args[0] = "C:\\Users\\fishe\\Documents\\GitHub\\4903-final-data\\input\\clean";
    args[1] = "C:\\Users\\fishe\\Documents\\GitHub\\4903-final-data\\output\\clean";*/

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
        if ( (int)s.charAt(i) > 47 && (int)s.charAt(i) < 58 || (int)s.charAt(i) > 96 && (int)s.charAt(i) < 123 ) {
            out += s.charAt(i);
        } else if( (int)s.charAt(i) > 64 && (int)s.charAt(i) < 91 ) {
            out += (char)((int)s.charAt(i) + 32);
        }
    } // Only choose ASCII characters.

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
      allDoc = new HashMap<>(10000000);

      File[] files = inDir.listFiles();
      File[] zDir;

      for(File f : files) {

        zDir = f.listFiles();
        if(zDir != null) {

          for(File z : zDir) {

            br = new BufferedReader(new FileReader(z));
            set = new HashSet<String>(100000);

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

        } else {

          br = new BufferedReader(new FileReader(f));
          set = new HashSet<String>(100000);

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

      }

    } catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static void cleanFiles(File inDir, File outDir) {
    HashSet<String> vocab = new HashSet<String>(3000000);
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
      HashSet<String> stopwords = getStopWords();
      File[] files = inDir.listFiles();
      File[] zDir;
      File tmp;

      for(File f : files) {

        zDir = f.listFiles();
        if(zDir != null) {

          for(File z : zDir) {

            tmp = new File(outDir.getPath()+"/"+f.getName());
            tmp.mkdirs();

            br = new BufferedReader(new FileReader(z));
            bw = new BufferedWriter(new FileWriter(tmp.getPath()+"/"+z.getName()));

            thisDoc = new HashMap<>(100000);
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

              if(!stopwords.contains(temp)) {

                if( idf > 3 && allDoc.get(temp) > 30 ) {

                  bw.write(temp+"\n");

                  if(!vocab.contains(temp)) {
                    vocab.add(temp);
                  }

                }

              }

            } // end while loop

            br.close();
            bw.close();

          }

        } else {

          br = new BufferedReader(new FileReader(f));
          bw = new BufferedWriter(new FileWriter(outDir.getPath()+"/"+f.getName()));

          thisDoc = new HashMap<>(100000);
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

            if(!stopwords.contains(temp)) {

              if( idf > 2 && thisDoc.get(temp) > 1 && allDoc.get(temp) > 20 ) {

                bw.write(temp+"\n");

                if(!vocab.contains(temp)) {
                  vocab.add(temp);
                }

              }

            }

          } // end while loop

          br.close();
          bw.close();

        }

      } // end for loop

      writeVocab(vocab);
      System.out.println(vocab.size());

    } catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

  }

  public static HashSet<String> getStopWords() throws IOException {

    HashSet<String> stop = new HashSet<String>(300);
    BufferedReader br = new BufferedReader( new FileReader("stopwords.txt") );
    String read;

    while((read = br.readLine())!=null) {
      stop.add(read);
    }

    return stop;

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
