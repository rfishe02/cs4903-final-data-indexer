
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.io.*;

public class QueryTest {

  public static void main(String[] args) throws IOException {

    /*
    String[] query = {"one","two","three","four","five"};
    PriorityQueue<Term> pq = new PriorityQueue<>( new TermComparator() );

    int i = 0;
    for(String s : query) {
      pq.add(new Term(s,i));
      i++;
    }

    while(!pq.isEmpty()) {
      System.out.println(pq.remove().count);
    }*/

    File rafDir = new File("C:\\Users\\fishe\\Documents\\GitHub\\4903-final-data\\output\\index");

    //File inDir, File rafDir, String[] query

    String[] query = {"cat"};

    Query q = new Query(rafDir,"stats.raf");
    //String[] res = q.runQuery( new File("C:\\Users\\fishe\\Documents\\GitHub\\4903-final-data\\output\\clean"), rafDir, query );

    /*
    for(String s : res) {

      System.out.println(res);

    }*/


    PriorityQueue<Term> pq = q.getQueue(rafDir,query);
    /*
    while(!pq.isEmpty()) {
      System.out.println(pq.remove().count);
    }*/

    q.getIntersect(rafDir,pq);

  }

  static class TermComparator implements Comparator<Term> {
    public int compare(Term o1, Term o2) {
      if(o1.count > o2.count) {
        return 1;
      } else if(o1.count < o2.count) {
        return -1;
      } else {
        return 0;
      }
    }
  }

}
