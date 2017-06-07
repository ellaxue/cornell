package operator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import project2.FromScanner;
import project2.ParserExample;
import project2.QueryInterpreter;
import project2.QueryPlanBuilder;
import project2.Tuple;
import project2.QueryPlanBuilder.Node;

/**
 * This class working as deleting duplicated tuples from the current table
 * Created on: 03/11/2017
 * @author ellaxue
 */
public class DulplicateEliminationOperator extends Operator {
	
	ArrayList<Tuple> tupleList;
	public DulplicateEliminationOperator(){
		tupleList = new ArrayList<Tuple>();
	}

	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node,QueryInterpreter interpreter) throws FileNotFoundException, IOException{
		Tuple tuple = node.getLeftChild().getOperator().getNextTuple(node.getLeftChild(), interpreter);
		
		return tuple;
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
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node, QueryInterpreter interpreter) throws IOException{		
		
		Tuple tuple = getNextTuple(node,interpreter);
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		while(tuple != null){
			tupleList.add(tuple);
			tuple = getNextTuple(node,interpreter);
		}
		
		//if empty table, just return
		if(tupleList == null || tupleList.size() == 0) return;
		
		//get the first tuple in the tuple list for comparison with others
		String data[] = tupleList.get(0).getData();
		//since they are in order only check if i and i-1 tuple are the same  
		//if so don't add it to the new tuple list
		for(int i = 1;i < tupleList.size(); i++){
			String curTuple[] = tupleList.get(i).getData();
			// if two tuples are not the same, add to the output
			if(!Arrays.equals(data,curTuple)){
				writer.print(tupleList.get(i-1)+"\n");
			}
			data = curTuple;
		}
		
		//add the last tuple to the new tuple list
		writer.print(tupleList.get(tupleList.size()-1 )+"\n");
		writer.close();
	}
}
