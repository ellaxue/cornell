package project;

import java.util.ArrayList;


public class Buffer extends ArrayList<Tuple> {
	private static final long serialVersionUID = 1L;
	private int pageNumber;
	private int tupleCapacity; // max tuple numbers
	private static final int pageSize=4096;
	
	public Buffer(int pageNumber,int tupleColumns) {
		this.pageNumber=pageNumber;
		tupleCapacity=pageSize*pageNumber/(tupleColumns*4);
	}
	
	public boolean isFull() {
		if (this.size()>=tupleCapacity) return true;
		else return false;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
}
