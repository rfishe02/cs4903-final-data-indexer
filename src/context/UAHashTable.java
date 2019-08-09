package context;

public class UAHashTable {
	
	private static final int INITIAL_SIZE = 27;
	private int hashMapSize;
	private UARecord[] table;
	
	public UAHashTable(int size) {
		this.table = new UARecord[size];
	}
	
	public UAHashTable() {
		this(INITIAL_SIZE);
	}
	
	public UARecord[] getTable() {
		return this.table;
	}
	
	public void insert(UARecord r) {
		int h = Math.abs(r.hashCode());
		int i = 0;
		
		while (i < table.length && table[(h + i) % table.length] != null) {
			i++;
		}
		
		if (i >= table.length) {
			resize();
			insert(r);
			return;
		}
		
		table[(h + i) % table.length] = r;
		hashMapSize++;
	}
	
	public UARecord search(String term) {
		int h = Math.abs(term.hashCode());
		int i = 0;
		
		while (i < table.length && table[(h + i) % table.length] != null) {
			if (table[(h + i) % table.length].getWord().equals(term)) {
				return table[(h + i) % table.length];
			}
			i++;
		}
		
		return null;
	}
	
	public int size() {
		return hashMapSize;
	}
	
	public int fullSize() {
		return table.length;
	}
	
	private void resize() {
		UARecord[] A = table;
		table = new UARecord[findPrime()];
		
		for (int i = 0; i < A.length; i++) {
			if (A[i] != null) {
				insert(A[i]);
			}
		}
	}
	
	private int findPrime() {
		int start = (int)(table.length * 1.5);
		boolean prime = true;
		int i;
		
		if (start % 2 == 0) {
			start++;
		}
		
		while (true) {
			prime = true;
			for (i = 2; i * i <= start; i++) {
				if (start % i == 0) {
					prime = false;
					break;
				}
			}
			if (prime) {
				return start;
			} else {
				start += 2;
			}
		}
	}

}
