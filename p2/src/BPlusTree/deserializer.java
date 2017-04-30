package BPlusTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import project.QueryPlan;
import project.catalog;

public class deserializer {
	private String filename;	        // the index file
	private catalog cl = catalog.getInstance();
	private FileChannel fc;					
	private String fileDirectory;
	private FileInputStream fin;
	private ByteBuffer buffer=ByteBuffer.allocate(QueryPlan.pageSize);				// The buffer page.
	private int rootAdr;				// root address
	private int leafSize;				// number of leaf pages
	private int treeOrder;				// order of the tree.
	private Integer lowKey;					// low key of the (included) search range
	private Integer highKey;				// high key of the (included) search range.  lowKey<= search range<= highKey
	private int curNodeAdr;

	private ArrayList<Record> ridList;  // a list of RID in search range
	
	private int times = 0;    //the times to get next rid
	
	public deserializer(Integer lowKey,Integer highKey,Boolean clustered,String indexFileName) throws IOException{
		filename = indexFileName;
		this.lowKey = lowKey;
		this.highKey = highKey;

		fileDirectory = cl.getTableLocation().get(filename);

		System.out.println("read path" + fileDirectory);
		try {
			fin = new FileInputStream(fileDirectory);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fc = fin.getChannel();
		try {
			fc.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		rootAdr = buffer.getInt(0);//read head page
		leafSize = buffer.getInt(4);
		treeOrder = buffer.getInt(8);

		//for lowKey
		if(lowKey == null) lowKey = Integer.MIN_VALUE ;
		if(highKey == null) highKey = Integer.MAX_VALUE ;
		if(lowKey>highKey) ridList = null;
		else{
			ridList = new ArrayList<Record>();
			
			curNodeAdr = readIndexPage(rootAdr,lowKey);
			while(curNodeAdr>leafSize){//search through the index pages
				curNodeAdr = readIndexPage(curNodeAdr,lowKey);
			}
			int leftAdr = curNodeAdr;

		//for highKey
			curNodeAdr = readIndexPage(rootAdr,highKey);
			while(curNodeAdr>leafSize){//search through the index pages
				curNodeAdr = readIndexPage(curNodeAdr,highKey);
			}
			int rightAdr = curNodeAdr;
			
			if(leftAdr == rightAdr){
				readLeafPage(leftAdr,lowKey,highKey);
			}else{
				readLeafPage(leftAdr, rightAdr, lowKey, highKey);
			}
		}
	}
	
	public void readLeafPage(int leftAdr, int rightAdr, Integer lowKey, Integer highKey) throws IOException{
		readLeafPage(leftAdr, lowKey, Integer.MAX_VALUE);
		leftAdr++;
		while(leftAdr<rightAdr){
			readLeafPage(leftAdr, Integer.MAX_VALUE, Integer.MAX_VALUE);
			leftAdr++;
		}
		readLeafPage(leftAdr, Integer.MAX_VALUE, highKey);
	}
	
	public void readLeafPage(int nodeAdr,Integer lowKey, Integer highKey) throws IOException{
		buffer.clear();
		fc.position((long)4096*nodeAdr);
		fc.read(buffer);
		buffer.flip();
		int keyNum = buffer.getInt(4);
		ArrayList<Integer> keys = new ArrayList<>();
		ArrayList<Integer> positions = new ArrayList<>();
		ArrayList<Integer> counts = new ArrayList<>();
		int count = -1;// number of the same key value
		int lastPosition = 8;
		for(int i = 0; i<keyNum; i++){
			int curPosition = lastPosition+(count+1)*8;
			positions.add(curPosition);//store each key's location
			keys.add(buffer.getInt(curPosition));
			lastPosition = curPosition;
			count = buffer.getInt(curPosition+4);
			counts.add(count);//store the number of each key
		}
		int position = Collections.binarySearch(keys, lowKey);
		if(position<0) position = -position-1;
		int keyVal = lowKey;
		while(keyVal<=highKey && position<keyNum){
			int p = positions.get(position);
			int c = counts.get(position);
			for(int i = 0; i<c; i++){
				int pid = buffer.getInt(p+(i+1)*8);
				int tid = buffer.getInt(p+(i+1)*8+4);
				Record rd = new Record(pid,tid);
				ridList.add(rd);
			}
			position++;
			if(position<keyNum){
				keyVal = buffer.getInt(position);
			}
		}
	}
	
	public int readIndexPage(int NodeAdr,Integer key) throws IOException{
		buffer.clear();
		fc.position((long)4096*NodeAdr);
		fc.read(buffer);
		buffer.flip();
		int keyNum = buffer.getInt(4);
		ArrayList<Integer> keys = new ArrayList<>();
		ArrayList<Integer> addresses = new ArrayList<>();
		for(int i = 0; i<keyNum; i++){
			keys.add(buffer.getInt(8+i*4));
		}
		for(int i = 0; i<keyNum+1; i++){
			addresses.add(buffer.getInt(8+4*keyNum+i*4));
		}
		int position = Collections.binarySearch(keys, key);
		if(position>=0){
			return addresses.get(position);
		}else{
			return addresses.get(-position-1);
		}
	}
	
	public Record getLastRecord(){
		return ridList.get(ridList.size()-1);
	}
	public Record getNextRecord(){
		int temp = times;
		times++;
		return ridList.get(temp);
	}
	public void resetGetNextRecord(){
		this.times = 0;
	}
}
