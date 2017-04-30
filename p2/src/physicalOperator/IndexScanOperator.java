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

public class IndexScanOperator extends Operator{
	Integer lowKey;
	Integer highKey;
	String index;
	Boolean clustered;
	String indexFileName;
	deserializer dTree;
	TupleReader reader;
	Record rid;
	public IndexScanOperator(String tableName, String index, Integer lowKey,Integer highKey,Boolean clustered,String indexFileName) throws Exception {
		reader= new BinaryReader(tableName);
		this.lowKey = lowKey;
		this.highKey = highKey;
		this.index = index;
		this.clustered = clustered;
		this.indexFileName = indexFileName;
		this.dTree = new deserializer(lowKey, highKey, clustered, indexFileName);
		this.rid = dTree.getNextRecord();
	}
	@Override
	public Tuple getNextTuple() throws Exception {
		if(clustered){
			Record rd = dTree.getLastRecord();
			int pid = rid.getPageId();
			int tid = rid.getTupleid();
			if(pid<rd.getPageId()||(pid==rd.getPageId()&&tid<=rd.getTupleid())){
				return reader.readNext(pid,tid, false);
			}else return null;
		}
		else{
			Record temp = rid;
			if(temp!=null){
				this.rid = dTree.getNextRecord();
				return reader.readNext(temp.getPageId(),temp.getTupleid(),true);
			}else return null;
		}
	}
	@Override
	public void reset() throws Exception {
		reader.reset();
		
	}
	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub
		
	}
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
