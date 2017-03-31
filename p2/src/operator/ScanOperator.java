package operator;
import project.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Scan operator to scan the information of base table
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */
public class ScanOperator extends Operator {
	TupleReader reader;

	public ScanOperator(String tablename) throws IOException {
		reader=new BinaryReader(tablename);
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
    	while ((tu=this.getNextTuple())!=null) {
    		writer.writeNext(tu);
    	}
    	writer.close();
		QueryPlan.nextQuery();
	}

}
