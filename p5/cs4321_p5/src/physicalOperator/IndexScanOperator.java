package physicalOperator;

import java.io.File;
import java.util.ArrayList;

import BPlusTree.IndexInfo;
import BPlusTree.Record;
import BPlusTree.deserializer;
import IO.BinaryReader;
import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleReader;
import IO.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
import project.QueryPlan;
import project.Tuple;
import project.catalog;
import queryPlanBuilder.PhysicalPlanBuilder;
/**
 * A BPlus Tree index scan operator class for scanning a table
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class IndexScanOperator extends Operator{
	private Integer lowKey;
	private Integer highKey;
	private Boolean clustered;
	private String indexFileName;   //tree index generated file
	private deserializer dTree;
	private TupleReader reader;
	private Record rid;             // current Rid
	private int count;
	private String tableName;
	private String originalTableName;
	private String indexCol;
	private catalog cl = catalog.getInstance();
	
	//TODO: might not need this instructor if the following one is correct
	public IndexScanOperator(String tableName, Integer lowKey,Integer highKey,Boolean clustered,String indexFileName, String indexCol) throws Exception {
		this.tableName = tableName;
		reader= new BinaryReader(tableName);
		this.indexCol = indexCol;
		this.count = 0;
		this.lowKey = lowKey;
		this.highKey = highKey;
		this.clustered = clustered;
		this.indexFileName = indexFileName;
		this.dTree = new deserializer(lowKey, highKey, clustered, indexFileName);
		this.rid = dTree.getNextRecord();
	}
	
	public IndexScanOperator(String tableName, IndexInfo index) throws Exception {
		// original table name disregard if alias is used for index file scan
		this.originalTableName = index.getTableName();
		//System.out.println("table name:" + tableName);
		this.reader= new BinaryReader(tableName);//could be alias name, if alias used
		this.indexCol = index.getIndexCol();
		this.count = 0;
		this.lowKey = index.getLowKey();
		this.highKey = index.getHighKey();
		this.clustered = index.getClustered();
		this.indexFileName= cl.getIndexDir()+File.separator+originalTableName+"."+indexCol;
		this.dTree = new deserializer(lowKey, highKey, clustered, indexFileName);
		this.rid = dTree.getNextRecord();
	}

	/**
	 * Method to read the next tuple from the file
	 * 
	 * @return (Tuple)  the next tuple
	 */
	@Override
	public Tuple getNextTuple() throws Exception {
		if(clustered){
//			Record rd = dTree.getLastRecord();
			int pid = rid.getPageId();
			int tid = rid.getTupleid();
			if(this.count<dTree.totalRid()){
				this.count++;
				return reader.readNext(pid,tid, false);	
			}else return null;
		}
		else{
			int pid = rid.getPageId();
			int tid = rid.getTupleid();
			if(this.count<dTree.totalRid()){
			//	System.out.println("count = "+count+" total rid = "+dTree.totalRid());
				if(this.count<dTree.totalRid()-1) this.rid = dTree.getNextRecord();
				this.count++;
				return reader.readNext(pid,tid);
			}else return null;
		}
	}
	
	/**
	 * Method to reset 
	 */
	@Override
	public void reset() throws Exception {
		reader.close();
		reader.reset();	
	}
	
	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub	
	}
	
	/**
	 * Method to dump the results 
	 */
	@Override
	public void dump() throws Exception {
		// TODO Auto-generated method stub
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
	/**
	 * Method to close reader 
	 */
	public void close() throws Exception{
		reader.close();
	}
	@Override
	public void setLeftChild(Operator child) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setRightChild(Operator child) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Operator getLeftChild() {
		return null;
	}
	@Override
	public Operator getRightChild() {
		return null;
	}
	@Override
	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Method to translate to string 
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("IndexScan").append("[").
		append(originalTableName+",").append(indexCol+",").append(lowKey+",").append(highKey+"]");
		
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
