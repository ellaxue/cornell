package project2;
import java.io.*;
import java.util.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.*;
import operator.*;
import project2.QueryPlanBuilder.Node;
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
	private Node<Operator> queryPlan;
	ArrayList<Tuple> tupleList;
	
	/**
	 *  Constructor
	 *  @param statement SQL query statement
	 */
	public QueryInterpreter(Statement statement){
		this.statement = statement;
		System.out.println("SQL : " + statement);
		if(statement instanceof Select){
			plainSelect = (PlainSelect)((Select)statement).getSelectBody();
			whereCondition = plainSelect.getWhere();
			joinList = plainSelect.getJoins();
			orderByElements = plainSelect.getOrderByElements();
			selectItemList = plainSelect.getSelectItems();
			distinct = plainSelect.getDistinct();
			fromItem = plainSelect.getFromItem();
			tupleList = null;
		}
	}
	
	/** get query plan from query plan builder
	 * @param queryPlan
	 */
	public void setQueryPlan(Node<Operator> queryPlan) {
		this.queryPlan = queryPlan;
	}
	
	/**
	 * execute the query plan from root
	 * @param root the root operator
	 * @throws IOException 
	 */
	public void executeQuery(Node<Operator> root) throws IOException {
			root.getOperator().dump(root,this);

	}

	/**
	 * @param root print the constructed query plan tree
	 */
	public void printQueryPlan(Node<Operator> root){
		if (root == null) return;
		printQueryPlan(root.getLeftChild());
		printQueryPlan(root.getRightChild());
		System.out.println("plan " + root.getOperator().getClass());
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
