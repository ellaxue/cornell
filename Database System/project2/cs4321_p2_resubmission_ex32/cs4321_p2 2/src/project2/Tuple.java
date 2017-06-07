package project2;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
/**
 *  Tuple.java
 *  This class stores tuple data
 *  Created on: 03/06/2017
 *  Author: Ella Xue (ex32)
 */
public class Tuple {
	private String[] tuple;
	private String table;
	public Tuple(String data[]){
		tuple = data;
	}
	
	/**
	 *  Constructor
	 */
	public Tuple(){}
	
	/**
	 *  Print the tuples in a better format
	 */
	@Override
	public String toString(){
		String result = "";
		int size = tuple.length;
		if (size == 0) return result;
		for(int i = 0; i < size - 1; i++){
			result += tuple[i] + ",";
		}
		result += tuple[size - 1];
		return result;
	}
	
	/**
	 * @return the datas stored in this tuple
	 */
	public String[] getData(){
		return tuple;
	}
	
	/**
	 * 
	 * @param table the table name this tuple referring to
	 */
	public void setTable(String table){
		this.table = table;
	}
	
	/**
	 * 
	 * @return the table name this tuple referring to
	 */
	public String getTableName(){
		return table;
	}
}
