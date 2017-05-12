package physicalOperator;
import project.*;
import queryPlanBuilder.PhysicalPlanBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import IO.BinaryReader;
import IO.BinaryWriter;
import IO.DirectReader;
import IO.DirectWriter;
import IO.TupleReader;
import IO.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
/**
 * Scan operator to scan the information of base table
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class ScanOperator extends Operator {
	private TupleReader reader;
	private String tableOriginalName;
	
	public ScanOperator(String tablename) throws Exception {
		catalog cl = catalog.getInstance();
		if(cl.UseAlias())this.tableOriginalName= cl.getAlias().get(tablename);
		else this.tableOriginalName = tablename;
		reader= new BinaryReader(tablename);
	}

	/**
	 * Method to read the next line from the file
	 * 
	 * @return (Tuple)  the next tuple
	 */
	@Override
	public Tuple getNextTuple() throws Exception {
	  return reader.readNext();
	}

	/**
	 * Method to reset 
	 */
	@Override
	public void reset() throws Exception {
		reader.close();
		reader.reset();
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
    		writer.writeNext(tu);
    		if (QueryPlan.debuggingMode){writerReadable.writeNext(tu);}
    	}
    	writer.close();
    	if (QueryPlan.debuggingMode){writerReadable.close();}
		QueryPlan.nextQuery();
	}
	
	public void close() throws Exception{
		reader.close();
	}
	@Override
	public void setLeftChild(Operator child) {}

	@Override
	public void setRightChild(Operator child) {}

	@Override
	public Operator getLeftChild() {return null;}

	@Override
	public Operator getRightChild() {return null;}

	@Override
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
//		System.out.println("scan PhysicalPlanBuilder.level " + PhysicalPlanBuilder.level + " sb " + sb.toString());
		sb.append("TableScan").append("[").append(tableOriginalName).append("]");
		return sb.append("\n").toString();
	}
	
	@Override
	public void addChildren(Operator operator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<Operator> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

}
