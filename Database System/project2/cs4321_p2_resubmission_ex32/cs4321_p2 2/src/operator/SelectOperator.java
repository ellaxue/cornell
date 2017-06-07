package operator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import net.sf.jsqlparser.statement.select.*;
import project2.ExpressionEvaluator;
import project2.FromScanner;
import project2.ParserExample;
import project2.QueryInterpreter;
import project2.Tuple;
import project2.QueryPlanBuilder.Node;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
/**
 *  SelectOperator.java
 *  This class checks if a tuple matches certain condition, if not, 
 *  the tuple is not chosen.
 *  Created on: 03/06/2017
 *  Author: Ella Xue
 */
public class SelectOperator extends Operator {
	private ArrayList<Expression> selectConditionList;
	ArrayList<Tuple> tupleList;
	private FromScanner fromScanner;
	private FromItem fromItem;
	private Expression selectCondition;
	
	/**
	 * Constructor
	 * @param fromItem contains table name to be operated on
	 * @param selectConditions the selection condition to find the qualified tuples
	 */
	public SelectOperator(FromItem fromItem, HashMap<String, ArrayList<Expression>> selectConditions) {
		this.fromItem = fromItem;
		this.tupleList = new ArrayList<Tuple>();
		this.fromScanner = new FromScanner();
		this.fromItem.accept(fromScanner);
		
		// get the selection condition
		if(selectConditions != null){
			//check if this table has a alias, if so need to use alias to find select condition
			if(fromItem.getAlias() != null){
				selectConditionList = selectConditions.get(fromItem.getAlias());
			}
			else{
				selectConditionList = selectConditions.get(fromItem.toString());
			}
			
			if(selectConditionList != null){
				selectCondition = selectConditionList.get(0);
				//connect all select condition into conjunction selection condition
				// A <=3 , A > 1  ==>  A <= 3 AND A > 1
				for(int i = 1; i < selectConditionList.size(); i++){
					selectCondition = new AndExpression(selectCondition, selectConditionList.get(i));
				}
			}
		}
	}

	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node, QueryInterpreter interpreter) throws FileNotFoundException, IOException {
		
		Tuple tuple = null;
		//iterate through the whole table, find all tuples match the condition
		do{			
			tuple = fromScanner.scanOperator.getNextTuple(node,null);
			if(tuple == null) return null;
			if(selectCondition != null){
				ExpressionEvaluator eval = new ExpressionEvaluator(tuple);
				selectCondition.accept(eval);
				if(!eval.result()){
					tuple = null;
				}
			}
		}
		while(tuple == null);
		return tuple;
	}
	
	/**
	 * Tells the operator to reset its state and start returning its output 
	 * again from the beginning;	
	 * @throws FileNotFoundException 
	 */
	public void reset(Node<Operator> node) throws FileNotFoundException {
		fromScanner.scanOperator.reset(node, null);
	}
	
	/**
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException {
		Tuple tuple = getNextTuple(node,interpreter);
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		while(tuple != null){
			tupleList.add(tuple);
			writer.print(tuple.toString()+"\n");
			tuple = getNextTuple(node,interpreter);
		}
		writer.close();
	}

	public FromItem getFromItem() {
		return fromItem;
	}
}
