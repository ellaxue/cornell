package physicalOperator;
import project.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;
/**
 * Scan operator to scan the information of base table
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class ScanOperator extends Operator {
	TupleReader reader;

	public ScanOperator(String tablename) throws IOException {
		reader=QueryPlan.debuggingMode? new DirectReader(tablename):new BinaryReader(tablename);
	}

	/**
	 * Method to read the next line from the file
	 * 
	 * @return (Tuple)  the next tuple
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
	  return reader.readNext();
	}

	/**
	 * Method to reset 
	 */
	@Override
	public void reset() throws IOException {
		reader.reset();
	}

	/**
	 * Method to dump the results 
	 */
	@Override
	public void dump() throws IOException {
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
	public void reset(int index) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
