package operator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import project2.*;
import project2.QueryPlanBuilder.Node;
/**
 *  ScanOperator.java
 *  This class scans all date saved in a data file
 *  Created on: 03/05/2017
 *      Author: Ella Xue
 */
public class ScanOperator extends Operator{
	private File file;
	BufferedReader bufferReader;
	HashMap<String, List<Tuple>> tables;
	String outputDir;
	
	/** Constructor
	 * @param file the data file to be read in
	 */
	public ScanOperator(File file){
		this.file = file;
		try {
			bufferReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tells the operator to reset its state and start returning its output 
	 * again from the beginning;	
	 * @throws FileNotFoundException 
	 */
	public void reset(Node<Operator> node, Operator operator) throws FileNotFoundException {
		bufferReader = new BufferedReader(new FileReader(file));
	}
	
	/**
	 * Repeatedly to get the next tuple of the operator's output.
	 * @return next Tuple if exists
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public Tuple getNextTuple(Node<Operator> node,QueryInterpreter interpreter) throws IOException {
		Table table = (Table)((SelectOperator)node.getOperator()).getFromItem();
		if(file == null) return null;
		String line = bufferReader.readLine();
		if(line != null){
			String data[] = line.split(",");
			Tuple tuple = new Tuple(data);
			String tableName = table.getAlias();
			if(tableName == null){
				tableName = table.getName();
			}
			tuple.setTable(tableName);
			return tuple;
		}
		return null;
	}
	
	/**
	 * This method repeatedly calls getNextTuple() until the next tuple 
	 * is null (no more output) and writes each tuple to a suitable PrintStream.
	 * @throws IOException 
	 */
	public void dump(Node<Operator> node, QueryInterpreter interpreter) throws IOException {
		Table table = (Table)interpreter.getFromItem();
		file = new File(ParserExample.inputDir+ "/db/data/"+table.getName());
		PrintWriter writer = new PrintWriter(ParserExample.outputDir+"/query"+(ParserExample.fileCounter++));
		bufferReader = new BufferedReader(new FileReader(file));
		Tuple tuple = getNextTuple(node,interpreter);
		while(tuple != null){
			writer.print(tuple.toString()+"\n");
			tuple = getNextTuple(node, interpreter);
		}
		writer.close();
	}
}

