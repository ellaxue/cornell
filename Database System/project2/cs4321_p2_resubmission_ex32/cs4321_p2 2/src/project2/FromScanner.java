package project2;
import java.io.File;
import java.util.HashMap;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import operator.Operator;
import operator.ScanOperator;
/**
 *  FromScanner.java
 *  This class construct a scanOperator class that scans every tuple 
 *  it the current table and push it to the parent class.
 *  Created on: 03/06/2017
 *  Author: Ella Xue (ex32)
 */
public class FromScanner implements FromItemVisitor{
	File inputDir;
	HashMap<String, HashMap<String, Integer>> schema;
	Table table;
	String outputDir;
	public ScanOperator scanOperator;
	
	 /**
	  *  Construct a scanOperator that read in data file
	  *  @param the data file contain the table
	  */
	@Override
	public void visit(Table tableName) {
		scanOperator = new ScanOperator(new File(ParserExample.inputDir+ "/db/data/"+tableName.getName()));
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubJoin arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void println(String sentence){
		System.out.println(sentence);
	}
	
				
}
