package BPlusTree;

import java.util.ArrayList;

/**
 * Parent class of leaf node and index node for node's basic information
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class Node {
	protected boolean isLeafNode;
	protected ArrayList<Integer> keys;
  	protected int numOfKeys = 0;
  	protected int numOfDataEntry = 0;
  	protected int address = 0;
	public boolean isFull() {
		return keys.size() == 2 * BPlusTree.order;
	}
	
	/**
	 * sets this node's address in the file
	 * @param address
	 */
	public void setAddress(int address){
		this.address = address;
	}
	
	/**
	 * 
	 * @return the number of data entry in this node
	 */
	public int getNumOfDataEntry(){
		return this.numOfDataEntry;
	}	
}
