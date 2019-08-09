package context;

public class UARecord {
	
	private String word;
	private long count;
	private float ppmi;
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public float getPpmi() {
		return ppmi;
	}
	
	public void setPpmi(float ppmi) {
		this.ppmi = ppmi;
	}
	
	public int hashCode() {
		return this.word.hashCode();
	}
	
	public boolean equals(Object obj) {
		UARecord o = (UARecord)obj;
		
		return this.word.equals(o.getWord());
	}

}
