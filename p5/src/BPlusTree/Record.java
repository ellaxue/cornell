package BPlusTree;

/**
 * This class stores a rid entry set's info
 *@author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class Record {
	private int pageId;
	private int tupleId;
	
	/**
	 * Constructor
	 * @param pageId pageid
	 * @param tupleId tupleid
	 */
	public Record(int pageId, int tupleId){
		this.pageId = pageId;
		this.tupleId = tupleId;
	}
	
	public int getPageId() {
		return pageId;
	}
	
	public int getTupleid() {
		return tupleId;
	}
	
	@Override
	public String toString(){
		return " " +pageId+" "+tupleId;
	}
	
	/**
	 * for debugging pretty print
	 * @return
	 */
	public String toStringDebug(){
		return " <" +pageId+", "+tupleId+">";
	}
	
}
