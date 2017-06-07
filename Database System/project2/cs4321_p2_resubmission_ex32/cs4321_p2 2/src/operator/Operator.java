package operator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import project2.*;
import project2.QueryPlanBuilder.Node;
/**
 * Operator.java
 *
 *  Created on: 03/06/2017
 *      Author: Ella Xue (ex32)
 */
public abstract class Operator{

	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	ScanOperator scanOperator;
	SelectOperator selectOperator;
	JoinOperator joinOperator;
	FromScanner fromScanner;
	String outputDir;
	public Operator(){
		
	}
	
	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException{
		return null;
	}

	/**
	 * Tells the operator to reset its state and start returning its output 
	 * again from the beginning;	
	 * @throws FileNotFoundException 
	 */
	public void reset(Node<Operator> node) throws FileNotFoundException{
		
	}

	/**
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node, QueryInterpreter interpreter) throws IOException{		
	}
}