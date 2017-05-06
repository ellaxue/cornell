package physicalOperator;
import project.*;
import queryPlanBuilder.PhysicalPlanBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
/**
 * Select operator to execute the select operation.
 * It grabs the next tuple from its child and returns
 * tuple that passes the selection condition.
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class SelectOperator extends Operator {
    private Operator child;
    private Expression ex;
    private String tableName;
	
	public SelectOperator(Operator child, Expression ex, String tableName) {
      this.child=child;
      this.ex=ex;
      this.tableName = tableName;
	}
	/**
	 * Method to obtain the next tuple from its child and check
	 * 
	 * @return (Tuple) the tuple matches the selection condition
	 */
	@Override
	public Tuple getNextTuple() throws Exception{		
	Tuple tu;
	while((tu= child.getNextTuple())!=null) {
		
		if(ex == null) {return tu;}
		else{
			conditionEvaluator eva= new conditionEvaluator(tu,ex);
			if (eva.getResult()){return tu;}
		}
	}	
	return null;
	}

	/**
	 * Method to reset
	 */
	@Override
	public void reset() throws Exception{
    child.reset();		
	}

	/**
	 * Method to dump the results
	 */
	@Override
	public void dump() throws Exception {
	    Tuple tu;
        TupleWriter writer= new BinaryWriter();
        TupleWriter writerReadable = null;
        if (QueryPlan.debuggingMode) {writerReadable = new DirectWriter();}
        
    	while ((tu=this.getNextTuple())!=null) {
//    		System.out.println("tuple " + tu.getComplete());
    		writer.writeNext(tu);
    		if (QueryPlan.debuggingMode){writerReadable.writeNext(tu);}
    	}
    	writer.close();
    	if (QueryPlan.debuggingMode){writerReadable.close();}
		QueryPlan.nextQuery();
	}
	@Override
	public void setLeftChild(Operator child) {
		this.child = (Operator)child;
	}
	@Override
	public void setRightChild(Operator child) {
		
	}
	@Override
	public Operator getLeftChild() {
		return this.child;
	}
	@Override
	public Operator getRightChild() {
		return null;
	};
	public Expression getExpression(){
		return this.ex;
	}
	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < PhysicalPlanBuilder.level; i++){
			sb.append("-");
		}		
		if(ex == null){
			sb.append("Leaf[").append(tableName).append("]\n");
		}
		else{
			sb.append("Select[").append(ex).append("]\n");
			
			for(int i = 0; i < PhysicalPlanBuilder.level+1; i++){
				sb.append("-");
			}	
			sb.append("Leaf[").append(tableName).append("]\n");
		}	
		return sb.toString();
	}
	@Override
	public void addChildren(Operator operator) {
		// TODO Auto-generated method stub
		
	}
}
