package project2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import operator.*;
/**
 * QueryPlanBuilder.java
 * Building a Query Plan to execute queries in a left depth order
 *  Created on: 03/06/2017
 *  uthor: Ella Xue (ex32)
 */
public class QueryPlanBuilder {
	QueryInterpreter queryInterpreter;
	Node<Operator> root;
	Node<Operator> dummyNode;
	HashMap<String, Expression> joinCondition;
	HashMap<String, ArrayList<Expression>> selectCondition;
	
	/**
	 * @author ellaxue
	 *
	 * A node class will be used to construct query plan tree,
	 * each node stores its left and/or right child of operator if exists
	 */
	@SuppressWarnings("hiding")
	public class Node<Operator>{
		private Node<Operator> leftChild;
		private Node<Operator> rightChild;
		private Operator operator;
		Node(){
			leftChild = null;
			rightChild = null;
			operator = null;
		}

		/**
		 * @return current node saved operator 
		 */
		public Operator getOperator() {
			return operator;
		}
		
		/**
		 * @return check if current node's has a child operator
		 */
		public boolean hasLeftChild(){
			return leftChild != null;
		}
		
		/**
		 * @return check if current node's has a child operator
		 */
		public boolean hasRightChild(){
			return rightChild != null;
		}
		
		/**
		 * @return current node's child operator
		 */
		public Node<Operator> getLeftChild(){
			return leftChild;
		}
		
		/**
		 * @return current node's child operator
		 */
		public Node<Operator> getRightChild(){
			return rightChild;
		}
	}
	
	/**
	 * @return the root operator of the query plan tree
	 */
	public Node<Operator> getRoot() {
		return root;
	}
	
	/**
	 * Constructor
	 * @param queryInter get statement elements from query interpreter
	 */
	public QueryPlanBuilder(QueryInterpreter queryInter){
		queryInterpreter = queryInter;
		root = null;
		dummyNode = new Node<Operator>();
	}
	
	/**
	 * add left child node to the current node
	 * @param cur current working node
	 * @param newOperator new node to be added with this operator
	 */
	public void setLeftChild(Node<Operator> cur, Operator newOperator){
		cur.leftChild = new Node<Operator>();
		cur.leftChild.operator = newOperator;
	}
	
	/**
	 * add right child node to the current node
	 * @param cur current working node
	 * @param newOperator new node to be added with this operator
	 */
	public void setRightChild(Node<Operator> cur, Operator newOperator){
		cur.rightChild = new Node<Operator>();
		cur.rightChild.operator = newOperator;
	}
	
	
	/**
	 * Construct a query plan with tree structure
	 * @return the root of query plan tree
	 */
	public Node<Operator> getQueryPlan(){
		Node<Operator> cur = dummyNode;
		
		List<OrderByElement> orderElementList = queryInterpreter.getOrderByElements();
		//create distinct operator in the query plan tree and create an order operator
		//as distinct operator works better on sorted tuples
		if(queryInterpreter.getDistinct() != null){
			setLeftChild(cur,new DulplicateEliminationOperator());
			cur = cur.leftChild;
				setLeftChild(cur, new SortOperator(orderElementList));
				cur = cur.leftChild;
		}
		
		//add sort operator if orderElementList has element inside
		if(orderElementList != null && queryInterpreter.getDistinct() == null){
			setLeftChild(cur, new SortOperator(orderElementList));
			cur = cur.leftChild;
		}
		
		//Add projection operator if select item is not '*'
		List<SelectItem> itemList = queryInterpreter.getSelectItemList();
		
		if(!(itemList.get(0) instanceof AllColumns)){
			setLeftChild(cur, new ProjectionOperator(itemList));
			cur = cur.leftChild;
		}
		
		//Get join conditions from where clause
		List<Join> joinList = queryInterpreter.getJoinList();
		if(queryInterpreter.getWhereCondition() != null){
			WhereExpressionEvaluator eval = new WhereExpressionEvaluator();
			queryInterpreter.getWhereCondition().accept(eval);
			joinCondition = eval.getJoinCondition();
			selectCondition = eval.getSelectCondition();

		}
	
		// add join operator if based on the number of join elements in the list
		if(joinList != null){
			Collections.reverse(joinList);
			for(Join join: joinList){
				setLeftChild(cur, new JoinOperator(joinCondition, join.getRightItem()));
				cur = cur.leftChild;
				setRightChild(cur, new SelectOperator(join.getRightItem(),selectCondition));
			} 
		}
		
		//the last one is always the selection operator for the child node
		setLeftChild(cur, new SelectOperator(queryInterpreter.getFromItem(),selectCondition));
		root = dummyNode.leftChild;
		return root;
	}
}
