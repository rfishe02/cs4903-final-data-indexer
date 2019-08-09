package context;

public class UARecord {
	
	private String word;
	private long count;
	private float ppmi;
	private int position;
	
	public UARecord(String word, long count, float ppmi, int position) {
		this.word = word;
		this.count = count;
		this.ppmi = ppmi;
		this.position = position;
	}
	
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
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int hashCode() {
		return this.word.hashCode();
	}
	
	public boolean equals(Object obj) {
		UARecord o = (UARecord)obj;
		
		return this.word.equals(o.getWord());
	}

}
