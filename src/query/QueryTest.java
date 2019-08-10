
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;


public class QueryTest {

  public static void main(String[] args) {

    String[] query = {"one","two","three","four","five"};
    PriorityQueue<Term> pq = new PriorityQueue<>( new TermComparator() );

    int i = 0;
    for(String s : query) {
      pq.add(new Term(s,i));
      i++;
    }

    while(!pq.isEmpty()) {
      System.out.println(pq.remove().count);
    }

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
