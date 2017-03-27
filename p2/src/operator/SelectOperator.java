package operator;
import project.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.jsqlparser.expression.Expression;
/**
 * Select operator to execute the select operation.
 * It grabs the next tuple from its child and returns
 * tuple that passes the selection condition.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class SelectOperator extends Operator {
    private ScanOperator child;
    private Expression ex;

	
	public SelectOperator(ScanOperator child, Expression ex) {
      this.child=child;
      this.ex=ex;
	}
	/**
	 * Method to obtain the next tuple from its child and check
	 * 
	 * @return (Tuple) the tuple matches the selection condition
	 */
	@Override
	public Tuple getNextTuple() throws IOException{		
	Tuple tu;
	while((tu= child.getNextTuple())!=null) {
		conditionEvaluator eva= new conditionEvaluator(tu,ex);
		if (eva.getResult()){return tu;}
	}	
	return null;
	}

	/**
	 * Method to reset
	 */
	@Override
	public void reset() throws IOException{
    child.reset();		
	}

	/**
	 * Method to dump the results
	 */
	@Override
	public void dump() throws IOException {
		FileWriter output= new FileWriter(catalog.getInstance().getOutputdir()+File.separator+"query"+QueryPlan.getCount(),false);
		BufferedWriter br= new BufferedWriter(output);
		Tuple tu;
	    while ((tu=this.getNextTuple())!=null) {
	    br.write(tu.getComplete());  
	    br.newLine();
	    }
	    br.close();	
	    QueryPlan.nextQuery();
	};

}
