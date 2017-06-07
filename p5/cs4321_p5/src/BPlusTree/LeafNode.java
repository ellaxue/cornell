package BPlusTree;

import java.util.ArrayList;

/**
 * This class stores information about a leafndoe
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class LeafNode extends Node {
	protected ArrayList<ArrayList<Record>> values;
	
	/**
	 * Constructor
	 */
	public LeafNode(){
		isLeafNode = true;
		keys = new ArrayList<Integer>();
		values = new ArrayList<ArrayList<Record>>();
	}
	
	/**
	 * Create a new node of key and rid entry set list pair
	 * @param key the node key
	 * @param value rid list <pageid, tupleid>
	 */
	public void addToRecordList(Integer key, ArrayList<Record> value) {
		values.add(value);
		keys.add(key);
		numOfKeys++;
		numOfDataEntry++;
	}	
	
	/**
	 * 
	 * @return the entry set stored in this leaf node
	 */
	public String getEntrySet(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < keys.size(); i++){
			sb.append(keys.get(i)).append(" ").append(values.get(i).size());
			for(Record r:values.get(i)){
				sb.append(r);
			}
			sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * prints out the entry set with debugging info
	 * @return
	 */
	public String getEntrySetDebug(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < keys.size(); i++){
			sb.append("the key:" ).append(keys.get(i)).append(", num of rid in this key: ").append(values.get(i).size());
			for(Record r:values.get(i)){
				sb.append(r.toStringDebug());
			}
			sb.append("\n ");
		}
		return sb.toString();
	}

	@Override
	public String toString(){ 
		String result="";
		for(int i = 0; i < keys.size(); i++){
			result+= "(" + keys.get(i) +"," + values.get(i) +") ,";
		}
		return result;
	}
}
