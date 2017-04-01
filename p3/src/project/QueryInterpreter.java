package project;
import java.io.*;
import java.util.*;

import logicalOperator.TreeNode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.*;

/**
 *  QueryInterpreter.java
 *  This class interpreter SQL query
 *  Created on: 03/06/2017
 *      Author: Ella Xue (ex32)
 */
public class QueryInterpreter {
	private PlainSelect plainSelect = null;
	private Expression whereCondition = null;
	private List<Join> joinList = null;
	private Statement statement = null;
	private List<OrderByElement> orderByElements = null;
	private List<SelectItem> selectItemList = null;
	private Distinct distinct = null;
	private FromItem fromItem = null;
	private Table firstTable = null;
	private TreeNode queryPlan;
	ArrayList<Tuple> tupleList;
	
	/**
	 *  Constructor
	 *  @param statement SQL query statement
	 */
	public QueryInterpreter(Statement statement,catalog cl){
		this.statement = statement;
		System.out.println("SQL : " + statement);
		if(statement instanceof Select){
			plainSelect = (PlainSelect)((Select)statement).getSelectBody();
			whereCondition = plainSelect.getWhere();
			
			orderByElements = plainSelect.getOrderByElements();
			selectItemList = plainSelect.getSelectItems();
			distinct = plainSelect.getDistinct();
			fromItem = plainSelect.getFromItem();
			
			/*---------------- Edited from main(store alias information)------------------------*/
			firstTable = (Table)fromItem; //same as fromItem, cast to Table
			if (firstTable.getAlias() != null) {
				cl.setUseAlias(true);
			}
			cl.storeAlias(firstTable.getAlias(), firstTable.getName());
			
			joinList = plainSelect.getJoins(); 
			//store all join tables' alias information
			if (joinList != null) {
				for (Join ta : joinList) {
					Table table = (Table) ta.getRightItem();
					if (table.getAlias() != null) {
						cl.storeAlias(table.getAlias(), table.getName());
					}
				}
			}
			/*---------------- Edited from main------------------------*/
			
			tupleList = null;
		}
	}
	
	/** get query plan from query plan builder
	 * @param queryPlan
	 */
	public void setQueryPlan(TreeNode queryPlan) {
		this.queryPlan = queryPlan;
	}

	/**
	 * @param root print the constructed query plan tree
	 */
	public void printQueryPlan(TreeNode root){
		if (root == null) return;
		
		printQueryPlan(root.getLeftChild());		
		printQueryPlan(root.getRightChild());
		System.out.println("logical operator " + root.getClass());
	}

	
	public Table getFirstTable(){
		return this.firstTable;
	}
	/**
	 * @return getFromItem tables to be work on
	 */
	public FromItem getFromItem(){
		return fromItem;
	}

	/**
	 * @return getPlainSelect SQL select query
	 */
	public PlainSelect getPlainSelect() {
		return plainSelect;
	}
	/**
	 * @return getWhereCondition contains join and select condition of tables
	 */
	public Expression getWhereCondition() {
		return whereCondition;
	}
	/**
	 * @return getJoinList join tables to be joined
	 */
	public List<Join> getJoinList() {
		return joinList;
	}
	/**
	 * @return getStatement of the query
	 */
	public Statement getStatement() {
		return statement;
	}
	/**
	 * @return getOrderByElements that to be ordered by
	 */
	public List<OrderByElement> getOrderByElements() {
		return orderByElements;
	}
	/**
	 * @return getSelectItemList for projection
	 */
	public List<SelectItem> getSelectItemList() {
		return selectItemList;
	}

	/**
	 * @return distinct key word
	 */
	public Distinct getDistinct() {
		return distinct;
	}
}
