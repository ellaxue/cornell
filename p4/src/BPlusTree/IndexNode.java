package BPlusTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
/**
 * This class stores information about a indexnode
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 *
 */
public class IndexNode extends Node {

	protected ArrayList<Node> children;
	
	/**
	 * Constructor
	 * @param newChildren the nodes on the one level below
	 */
	public IndexNode(List<Node> newChildren){
		children = new ArrayList<Node>(newChildren);
		keys = new ArrayList<Integer>();
		for(int i = 1; i < children.size(); i++){
			Node curNode = children.get(i);
			while(!curNode.isLeafNode){
				//looking for the key at the left most subtree leaf node
				curNode = ((IndexNode)curNode).children.get(0);
			}
			keys.add(curNode.keys.get(0));
		}
		numOfKeys = keys.size();
	  	numOfDataEntry = children.size();
	}
	
	@Override
	public String toString(){ 
		String result="";
		for(Integer i:keys){
			result+=i+", ";
		}
		return result;
	}

	/**
	 * 
	 * @return the keys inorder
	 */
	public String getKeysInOrder() {
		//Make a copy of the key list to be sorted in order
		ArrayList<Integer> keys = new ArrayList<Integer>();
		keys.addAll(this.keys);
		
		Collections.sort(keys);
		StringBuilder sb = new StringBuilder();
		for(Integer i: keys){
			sb.append(i).append(" ");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return the children in this node in order
	 */
	public String getChildrenInOrder() {
		//Make a copy of the children list to be sorted in order by address
		ArrayList<Node> children = new ArrayList<Node>();
		children.addAll(this.children);
		
		//sort copied children list in order
		children.sort(new Comparator<Node>(){

			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				if(o1.address < o2.address) return -1;
				if(o1.address > o2.address) return 1;
				return 0;
			}
			
		});
		
		StringBuilder sb = new StringBuilder();
		for(Node node: children){
			sb.append(node.address).append(" ");
		}
		return sb.toString();
	}
}
