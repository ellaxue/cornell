package BPlusTree;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleWriter;
import project.QueryPlan;

/**
 * 
 * This class builds a BPlusTree based on chosen relation index,
 * and serialize the BPlusTree to a file
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 */
public class BPlusTree<K extends Comparable<K>, T> {
	
	public Node root;
	public static int order;
	private String tableName;
	private String columnName;
	private Boolean isCluster;
	public static int numOfLeafNode;
	private int address = 1;
	private TreeMap<Integer, ArrayList<Record>>recordMap;
	
	public BPlusTree(String index_info){
		numOfLeafNode = 0;
		recordMap = new TreeMap<Integer, ArrayList<Record>>();
		String[] info = index_info.split(" ");
		this.tableName = info[0];
		this.columnName = info[1];
		this.isCluster = info[2].equals("1")? true : false;
		this.order = Integer.parseInt(info[3]);
	}
	
	/**
	 * This method builds a BPlusTree and serialize it to a file
	 * @param file the file of the index tree is written to
	 * @throws Exception exception
	 */
	public void buildTree(String file) throws Exception{
		TupleWriter directWriter = null;
		if(QueryPlan.debuggingMode){directWriter = new DirectWriter(file+"_humanreadable");}
		TupleWriter binaryWriter = new BinaryWriter(file);
		((BinaryWriter)binaryWriter).writeEmptyPage();
	
		LeafNode leafNode = new LeafNode();	
		//filling the first leafNode if recordMap contains rid entry set
		ArrayList<Node> builtNodeList = new ArrayList<Node>();
		addNodeToList(builtNodeList,leafNode);
		
		int lastTwoNodeElementCounter = 0;
		
		//build leafnode level
		
		while(!recordMap.isEmpty()){
			Entry<Integer, ArrayList<Record>> entry = recordMap.pollFirstEntry();
			leafNode.addToRecordList(entry.getKey(), entry.getValue());
			
			if(lastTwoNodeElementCounter != 0){
				if(--lastTwoNodeElementCounter == 0 && !recordMap.isEmpty()){
					writeLeafNodeToFile(directWriter,binaryWriter,leafNode);
					leafNode = new LeafNode();
					addNodeToList(builtNodeList,leafNode);
				}
			}
			if(leafNode.isFull()){
				writeLeafNodeToFile(directWriter,binaryWriter,leafNode);
				leafNode = new LeafNode();
				addNodeToList(builtNodeList,leafNode);
				int size = recordMap.size();
				if(size > 2*order && size < 3*order){
					lastTwoNodeElementCounter = size / 2;
				}
			}
		}
		//might be a node that is not full, and need to write to a file as a whole page
		if(leafNode.numOfKeys > 0){writeLeafNodeToFile(directWriter,binaryWriter,leafNode);}
		
		ArrayList<Node> children = new ArrayList<Node>(); 
		ArrayList<Node> builtNodeListSwap = new ArrayList<Node>();
		lastTwoNodeElementCounter = 0;
		//build indexnode one level above leafnode
		while(true){
			Node curNode = builtNodeList.remove(0);
			children.add(curNode);
			if(lastTwoNodeElementCounter != 0){
				if(--lastTwoNodeElementCounter == 0 && !builtNodeList.isEmpty()){
					createNewNodeAndWriteCurNodeToFile(children,directWriter,binaryWriter,builtNodeListSwap);
					children = new ArrayList<Node>(); 
				}
			}
			if(children.size() == 2*order+1){
				createNewNodeAndWriteCurNodeToFile(children,directWriter,binaryWriter,builtNodeListSwap);
				children = new ArrayList<Node>(); 
				int size = builtNodeList.size();
				if(size > 2*order+1 && size < 3*order+2){
					lastTwoNodeElementCounter = size / 2;
				}
			}
			//current level's nodes have all been added to one level above indexNode as children
			if(builtNodeList.size() == 0){
				//in case of total node count less than 2d. Current level's nodes have not been added
				//to one level above's node as children;create a new indexNode to store those children
				if(!children.isEmpty()){
					createNewNodeAndWriteCurNodeToFile(children,directWriter,binaryWriter,builtNodeListSwap);
					children = new ArrayList<Node>(); 
				
				}				
				builtNodeList.addAll(builtNodeListSwap);
				int size = builtNodeList.size();
				if(size > 2*order+1 && size < 3*order+2){
					lastTwoNodeElementCounter = size / 2;
				}
				//only one node left, make it a tree root node
				if(builtNodeListSwap.size() == 1){
					root = builtNodeListSwap.get(0);
					break;
				}
				else{builtNodeListSwap.clear();}
			}			
		}
		
		if(QueryPlan.debuggingMode)directWriter.writeHeader("root address: " + root.address+ ", number of leafnode: " + numOfLeafNode + ", with order: " +order);
		binaryWriter.writeHeader(root.address+ " " + numOfLeafNode + " " +order);
		if(QueryPlan.debuggingMode)directWriter.close();
		binaryWriter.close();
		
	}
	
	/**
	 * This method checks if recordmap contains rid entry set, if it does, add this leaf node
	 * to one level above's node as a child
	 * @param builtNodeList store leaf node to this list
	 * @param leafNode contains node key and rid entry 
	 */
	private void addNodeToList(ArrayList<Node> builtNodeList, LeafNode leafNode) {
		if (!recordMap.isEmpty()){ 
			builtNodeList.add(leafNode);
			numOfLeafNode++;
			leafNode.setAddress(address++);
		}
	}
	
	/**
	 * This method write out the leafnode that is fully filled with rid entry set
	 * @param directWriter humanReadable writer
	 * @param binaryWriter binary writer
	 * @param leafNode  contains node key and rid entry 
	 * @throws Exception the exception
	 */
	private void writeLeafNodeToFile(TupleWriter directWriter, TupleWriter binaryWriter, LeafNode leafNode) throws Exception {
		if(QueryPlan.debuggingMode){directWriter.writeNext("0 means leaf node,  number of data entry in this leaf node "
	+ leafNode.getNumOfDataEntry()+", data in the entry set: {\n "+leafNode.getEntrySetDebug() + "}");}
		binaryWriter.writeNext("0 " + leafNode.getNumOfDataEntry()+" "+leafNode.getEntrySet());
	}

	/**
	 *  Current index node is full, need to write it to a file, and create a new index node for next round  
	 * @param children contains current level's nodes
	 * @param directWriter humanReadable writer
	 * @param binaryWriter binary writer
	 * @param builtNodeListSwap a second list to store newly created indexnodes
	 * @throws Exception the expcetion
	 */
	private void createNewNodeAndWriteCurNodeToFile(ArrayList<Node> children, TupleWriter directWriter,
			TupleWriter binaryWriter, ArrayList<Node> builtNodeListSwap) throws Exception {
		IndexNode newIndexNode = new IndexNode(children);
		builtNodeListSwap.add(newIndexNode);
		if(QueryPlan.debuggingMode)directWriter.writeNext("1 means index node, number of keys in this index node: " + newIndexNode.numOfKeys + ", keys in order:< " +newIndexNode.getKeysInOrder()+"> children address in order:< "+newIndexNode.getChildrenInOrder()+">");
		binaryWriter.writeNext("1 " + newIndexNode.numOfKeys+" "+newIndexNode.getKeysInOrder()+newIndexNode.getChildrenInOrder());
		newIndexNode.setAddress(address++);	
		
	}
	
	/**
	 * add rid entry set to a hashmap
	 * @param key node key
	 * @param record <pageid, tupleid>
	 */
	public void addToRecordMap(Integer key, Record record){
		if(recordMap.containsKey(key)){
			recordMap.get(key).add(record);
		}
		else{
			ArrayList<Record> recordList = new ArrayList<Record>();
			recordList.add(record);
			recordMap.put(key, recordList);
		}
	}
	
	/**
	 * @return the order
	 */
	public static int getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public static void setOrder(int order) {
		BPlusTree.order = order;
	}
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return the isCluster
	 */
	public Boolean getIsCluster() {
		return isCluster;
	}
	/**
	 * @param isCluster the isCluster to set
	 */
	public void setIsCluster(Boolean isCluster) {
		this.isCluster = isCluster;
	}
}
