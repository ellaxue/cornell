package physicalOperator;

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
/**
 * A BPlus Tree index scan operator class for scanning a table
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class IndexScanOperator extends Operator{
	Integer lowKey;
	Integer highKey;
	Boolean clustered;
	String indexFileName;   //tree index generated file
	deserializer dTree;
	TupleReader reader;
	Record rid;             // current Rid
	int count;
	public IndexScanOperator(String tableName, Integer lowKey,Integer highKey,Boolean clustered,String indexFileName) throws Exception {
		reader= new BinaryReader(tableName);
		this.count = 0;
		this.lowKey = lowKey;
		this.highKey = highKey;
		this.clustered = clustered;
		this.indexFileName = indexFileName;
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

}
