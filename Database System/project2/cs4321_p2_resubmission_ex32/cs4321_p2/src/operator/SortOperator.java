package operator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import project2.QueryPlanBuilder.Node;
import project2.ParserExample;
import project2.QueryInterpreter;
import project2.Tuple;
/**
 * SortOperator.java
 *
 *  Created on: 03/11/2017
 *      Author: Ella Xue (ex32)
 */
public class SortOperator extends Operator {
	ArrayList<Tuple> tupleList;
	List<OrderByElement> orderByElementList;
	private ArrayList<Integer> orderIndex;
	HashMap<String, Column[]> schema =  ParserExample.schema;
	ArrayList<String> columnNameList;
	Column[] columnNames;
	int index = 0;
	
	/**
	 * Constructor
	 * @param orderByElementList tuples ordered by the given column names
	 */
	public SortOperator(List<OrderByElement> orderByElementList){
		this.orderByElementList = orderByElementList;
		tupleList = null;
	}

	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException{
		if(tupleList == null) {
			getTuplesAndSort(node,interpreter );
		}
		Tuple tuple = null;
		if(index < tupleList.size()){
			tuple = tupleList.get(index++);
			return tuple;
		}

		return null;
	}

	/**
	 * Tells the operator to reset its state and start returning its output 
	 * again from the beginning;	
	 * @throws FileNotFoundException 
	 */
	public void reset(Node<Operator> node, Operator operator) throws FileNotFoundException{
		node.getLeftChild().getOperator().reset(node.getLeftChild());
	}

	/**
	 *  Set up the order information for the tables to be sorted.
	 *  Map the table index with column name stored inside schema
	 */
	private void initOrderInfo(){
		if(tupleList == null || tupleList.size() == 0) return;
		String tableName = tupleList.get(0).getTableName();
		//all columns names of current tuples that need to be sorted
		columnNames = schema.get(tableName);
		orderIndex = new ArrayList<Integer>();
		columnNameList = new ArrayList<String>();

		int countSavedColumnName = 0;

		if(orderByElementList != null){
			for(OrderByElement orderElement: orderByElementList){
				String columnName[] = (orderElement+"").split("\\.");

				int columnIndex = 0;
				//look for the index of the orderElementItem(columnName) and store in orderIndex list
				for(int i = 0 ; i < columnNames.length; i++){
					if(columnNames[i].getColumnName().equals(columnName[1])){
						columnIndex = i;
					}
				}
				//store the index of the column need to be sorted
				orderIndex.add(columnIndex);
				columnNameList.add(columnName[1]);
				countSavedColumnName++;
			}
		}	
		int columnNameLength = columnNames.length;

		//find all column name's index that we need to search based on orderByElement order 
		for(int i = 0; i < columnNameLength; i++){
			if(!containsElement(columnNames[i], countSavedColumnName)){
				columnNameList.add(columnNames[i].getColumnName());
				orderIndex.add(i);
				countSavedColumnName++;
			}
		}
	}

	/**
	 *  Extracting out all the tuples from child operator and then sort the tuples by 
	 *  given table names
	 * @param node
	 * @param interpreter
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void getTuplesAndSort(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException{
		Tuple tuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(),interpreter);
		tupleList = new ArrayList<Tuple>();
		while(tuple != null){
			tupleList.add(tuple);
			tuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(),interpreter);
		}
		sortTupleList();
	}

	/**
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException {
		getNextTuple(node,interpreter);
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		if(tupleList != null){
			for(Tuple tuple: tupleList){
				writer.print(tuple.toString()+"\n");
			}
		}
		writer.close();
	}

	void sortTupleList(){
		if(tupleList == null) return;
		initOrderInfo();
		Collections.sort(tupleList, new tupleComparator());
	}
	
	/*
	 *  need to check the case self-join table ?
	 */
	/**
	 * @param colName the name to be checked if stored inside the order table
	 * that contains index of the column to be ordered by
	 * @param length the length of the current order table that contains index of 
	 * the column to be ordered by
	 * @return true contains the element, false if not
	 */
	public boolean containsElement(Column colName, int length){
		for(int i = 0; i < length; i++){
			if(columnNameList.get(i).equals(colName.getColumnName())){
				return true;
			}
		}
		return false;
	}

	/**
	 *  Compare two tuples with given ordered by column names and order by ascending order
	 */
	public class tupleComparator implements Comparator<Tuple>{
		@Override
		public int compare(Tuple t1, Tuple t2) {
			int index = 0;
			while(index < orderIndex.size()){
				int t1Int = Integer.parseInt(t1.getData()[orderIndex.get(index)]);
				int t2Int = Integer.parseInt(t2.getData()[orderIndex.get(index)]);
				if (t1Int - t2Int > 0){return 1;}
				else if(t1Int - t2Int < 0){return -1;}
				else{index++;}
			}
			return t1.getData()[orderIndex.get(index-1)].compareTo(t2.getData()[orderIndex.get(index-1)]);
		}
	}
}
