package context;

import java.util.List;
import java.util.Map;

public class UAHolder {
	
	private int rowPos;
	private Map<Integer, Integer> counts;
	
	public UAHolder(int rowPos, Map<Integer, Integer> counts) {
		this.rowPos = rowPos;
		this.counts = counts;
	}
	
	public int getRowPos() {
		return rowPos;
	}
	public void setRowPos(int rowPos) {
		this.rowPos = rowPos;
	}
	public Map<Integer, Integer> getCounts() {
		return counts;
	}
	public void setCounts(Map<Integer, Integer> counts) {
		this.counts = counts;
	}
	
	public int hashCode() {
		return rowPos;
	}
	
	public boolean equal(Object obj) {
		UAHolder o = (UAHolder)obj;
		if (this.rowPos == o.getRowPos()) {
			return true;
		}
		return false;
	}

}
