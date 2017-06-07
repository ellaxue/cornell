package operator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import project2.ExpressionEvaluator;
import project2.FromScanner;
import project2.ParserExample;
import project2.QueryInterpreter;
import project2.QueryPlanBuilder.Node;
import project2.Tuple;
/**
 *  JoinOperator.java
 *  This class joins two tables with matching condition on each pairing tuples
 *  Created on: 03/06/2017
 *  Author: Ella Xue
 */
public class JoinOperator extends Operator{
	ArrayList<Tuple> tupleList;
	FromScanner fromScanner;
	HashMap<String, Expression> joinConditionMap;
	ArrayList<Tuple> joinTupleList;
	FromItem fromItem;
	HashMap<String, Column[]> schema = ParserExample.schema;
	Expression joinCondition;
	int leftTupleListIndex = 0;
	ArrayList<Tuple> leftTupleList;
	/** Constructor 
	 * @param joinConditionMap the join condition of two tables
	 * @param fromItem the right child's table name to be joined
	 */
	public JoinOperator(HashMap<String, Expression> joinConditionMap, FromItem fromItem){
		tupleList = null;
		joinTupleList = new ArrayList<Tuple>();
		this.joinConditionMap = joinConditionMap;
		this.fromItem = fromItem;
		joinCondition = null;
	}
	
	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node, QueryInterpreter interpreter) throws IOException {
		
		Tuple leftTuple = null;
		//get all the tuples from the left table
		if (leftTupleList == null){
			leftTupleList = new ArrayList<Tuple>();
			leftTuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(), interpreter);
			while(leftTuple != null){
				leftTupleList.add(leftTuple);
				leftTuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(), interpreter);
			}
			printTupleList(leftTupleList);
		}

		//get join condition for these two tables
		if(joinCondition == null){
			joinCondition = getJoinCondition(node);
		}
		
		// leftTupleListIndex < leftTupleList.size() means there is still at one tuple to be joined on the left table
		if(leftTupleListIndex < leftTupleList.size()){
			//get the left tuple
			leftTuple = leftTupleList.get(leftTupleListIndex);
		}else{
			return null;
		}
		
		Tuple rightTuple = null;
		do{
			rightTuple = node.getRightChild().getOperator().getNextTuple(node.getRightChild(), interpreter);
			if(rightTuple == null){
				//right table has exhausted all tuples, go to the next left tuple
				//reset the readerBuffer
				node.getRightChild().getOperator().reset(node);
				//check if left tuples are all visited and paired with every right tuple
				if( ++leftTupleListIndex == leftTupleList.size()){return null;}
				//current left tuple has matched up with all right tuple
				//update the left tuple to the next one
				leftTuple = leftTupleList.get(leftTupleListIndex);
				rightTuple = node.getRightChild().getOperator().getNextTuple(node.getRightChild(), interpreter);
			}
			// join tables baed on join condition
			if(joinCondition != null){
				ExpressionEvaluator eval = new ExpressionEvaluator(leftTuple, rightTuple,leftTuple.getTableName(), rightTuple.getTableName());
				joinCondition.accept(eval);
				if(eval.result() != true){rightTuple = null;}
			}
		}
		while(rightTuple == null);
		
		String leftTableName = 	leftTuple.getTableName();
		String rigthTableName = rightTuple.getTableName();
		//joined table name
		String tableName=leftTableName+rigthTableName;
		
		//add a new combined table schema. 
		addSchema(tableName,leftTableName,rigthTableName);
		// the two tuple matches, return the tuple to the parent
		return combineJoinTuples(leftTuple, rightTuple);
	}
	
	/** This method stored a new table schema
	 * @param tableName new table name
	 * @param leftTableName left table's name 
	 * @param rigthTableName right table's name
	 */
	public void addSchema(String tableName, String leftTableName, String rigthTableName){
		if(!schema.containsKey(tableName)){
			Column[] newColumns = combineTableColumnName(schema.get(leftTableName), schema.get(rigthTableName));
			schema.put(tableName, newColumns);
		}
	}
	
	/**
	 *  Start reading from the first tuple for the table  
	 */
	public void reset(Node<Operator> node) throws FileNotFoundException {
		node.getLeftChild().getOperator().reset(node.getLeftChild());
	}
	
	public void dump(Node<Operator> node,QueryInterpreter interpreter) throws IOException{
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		Tuple tuple = getNextTuple(node,interpreter);
		while(tuple != null){
			writer.print(tuple.toString()+"\n");
			tuple = getNextTuple(node,interpreter);
		}
		writer.close();
	}
	
	/**
	 * obtained a join condition of left child table and right child table
	 * @param node current node with join operator stored inside
	 * @return to get the join condition of two tables to be joined
	 */
	private Expression getJoinCondition(Node<Operator> node){
		Expression joinCond = null;
		if(joinConditionMap != null){
			joinCond = joinConditionMap.get(getJoinTableName(node));
			if (joinCond == null){
				joinCond = joinConditionMap.get(getJoinTableName2(node));
			}
		}
		return joinCond;
	}
	/**
	 * @param leftTuple
	 * @param rightTuple
	 * @return
	 */
	private Tuple combineJoinTuples(Tuple leftTuple, Tuple rightTuple){
		String joinName = leftTuple.getTableName()+rightTuple.getTableName();
		String data[] = joinTwoTuple(leftTuple, rightTuple);
		Tuple joinTuple = new Tuple(data);
		joinTuple.setTable(joinName);
		return joinTuple;
	}
	
	
	/**
	 * Search left side of the tree node to get child operator and it's table to be joined
	 * Search right side of the tree node to get child operator and it's table to be joined
	 * @param node current node with join operator stored inside
	 * @return get the name of two join table and combine them
	 */
	public String getJoinTableName(Node<Operator> node){
		String leftChildTableName = "";
		if(node.getLeftChild().getOperator() instanceof SelectOperator){
			leftChildTableName = ((SelectOperator)node.getLeftChild().getOperator()).getFromItem().getAlias();
			if( leftChildTableName == null){
				leftChildTableName = ((SelectOperator)node.getLeftChild().getOperator()).getFromItem().toString();
			}
		}
		else if(node.getLeftChild().getOperator() instanceof JoinOperator){
			leftChildTableName = ((SelectOperator)node.getLeftChild().getRightChild().getOperator()).getFromItem().getAlias(); 
			if( leftChildTableName == null){
				leftChildTableName = ((SelectOperator)node.getLeftChild().getRightChild().getOperator()).getFromItem().toString();
			}
		}
		String rightChildTableName = ((SelectOperator)node.getRightChild().getOperator()).getFromItem().getAlias();
		if (rightChildTableName == null){
			rightChildTableName = ((SelectOperator)node.getRightChild().getOperator()).getFromItem().toString();
		}
		return leftChildTableName+rightChildTableName;
	}
	
	
	/**
	 * Search left side of the tree node to get child operator and it's table to be joined
	 * Search right side of the tree node to get child operator and it's table to be joined
	 * @param node current node with join operator stored inside
	 * @return get the name of two join table and combine them
	 */
	private String getJoinTableName2(Node<Operator> node) {
		String leftChildTableName = "";
		if(node.getLeftChild().getOperator() instanceof SelectOperator){
			leftChildTableName = ((SelectOperator)node.getLeftChild().getOperator()).getFromItem().getAlias();
			if( leftChildTableName == null){
				leftChildTableName = ((SelectOperator)node.getLeftChild().getOperator()).getFromItem().toString();
			}
		}
		else if(node.getLeftChild().getOperator() instanceof JoinOperator){
			leftChildTableName = ((SelectOperator)node.getLeftChild().getLeftChild().getOperator()).getFromItem().getAlias();
			if( leftChildTableName == null){
				leftChildTableName =  ((SelectOperator)node.getLeftChild().getLeftChild().getOperator()).getFromItem().toString();
			}
		}
		String rightChildTableName = ((SelectOperator)node.getRightChild().getOperator()).getFromItem().getAlias();
		if (rightChildTableName == null){
			rightChildTableName = ((SelectOperator)node.getRightChild().getOperator()).getFromItem().toString();
		}
		return leftChildTableName+rightChildTableName;
	}
	

	/**
	 * @param A first table column names 
	 * @param B second table column names
	 * @return combine new table column names of A and B
	 */
	private Column[] combineTableColumnName(Column[] A, Column[] B) {
		Column newColumns[] = new Column[A.length+B.length];
		Table combineTable = new Table();
		String combineTableName  = "";
		if(A != null && A.length > 0){
			combineTableName += A[0].getTable().getName();
		}
		if(B != null && B.length > 0){
			combineTableName += B[0].getTable().getName();
		}
		combineTable.setName(combineTableName);
		int index = 0;
		for(int i = 0; i < A.length; i++){
			Column column = new Column();
			column.setColumnName(A[i].getColumnName());
			column.setTable(A[i].getTable());
			newColumns[index++] = column;
		}
		for(int i = 0; i < B.length; i++){
			Column column = new Column();
			column.setColumnName(B[i].getColumnName());
			column.setTable(B[i].getTable());
			newColumns[index++] = column;
		}
		return newColumns;
	}
	

	/**
	 * Join two tuples that satisfied the condition to be one tuple
	 * @param A first tuple to be joined
	 * @param B second tuple to be joined
	 * @return joined tuple of A and B
	 */
	public String[] joinTwoTuple(Tuple A, Tuple B){
		String newTuple[] = new String[A.getData().length + B.getData().length];
		int index = 0;
		for(int i = 0; i < A.getData().length; i++){
			newTuple[index++] = A.getData()[i];
		}
		for(int i = 0; i < B.getData().length; i++){
			newTuple[index++] = B.getData()[i];
		}
		return newTuple;
	}

	/** Print out current array list elements for debugging purpose
	 * @param list
	 */
	public void printTupleList(ArrayList<Tuple> list){
		for(Tuple rightTuple: list){
			System.out.println(rightTuple.toString());
		}
	}
}
