package physicalOperator;

import java.io.IOException;
import java.util.ArrayList;

import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
import project.Buffer;
import project.QueryPlan;
import project.SchemaPair;
import project.Tuple;
import project.conditionEvaluator;
/**
 * BNLJoin operator to execute the BNLJoin operation
 * 
 * @author Chengcheng Ji (cj368), Pei Xu (px29)  and Ella Xue (ex32) 
 */
public class BNLJOperator extends Operator{
	public int BNLJ_Page_Bytes = 4096;
	private Buffer buf;
	private int pageSize;
	private Operator rightChild;
	private Operator leftChild;
	private boolean first_enter = true;
	private int Outer_attr;
	private Expression ex;
	private int count;
	private boolean addedToSet = false;
	
	
	public BNLJOperator(Operator leftChild, Operator rightChild, Expression ex, int pageSize) throws IOException {
		
		//The leftChild is the Outer Child and the rightChild is the Inner Child
		//System.out.println("leftChild:::::::::::::::::::::: "+leftChild.toString());
		//Inner_attr= rightChild.getNextTuple().getTuple().length;
		this.pageSize = pageSize;
		//reset after getting attributes number
		//leftChild.reset();
		//rightChild.reset();

		this.rightChild = rightChild;
		this.leftChild = leftChild;

		this.ex = ex;
		this.count = 0;
	}
	
	
	/** 
	 * Method to obtain a tuple from the outer and a tuple from the inner and glue them
	   together
	   Outer tuples are stored in block
	 * 
	 * @return (Tuple) the tuple matches the join condition(If the join is a cross product, all pairs of
	   tuples are returned)
	 */
	@Override
	public Tuple getNextTuple() throws IOException {
		Tuple temp;
		if(first_enter == true&&(temp=leftChild.getNextTuple())!=null){//Initiate buffer
			this.Outer_attr = temp.getTuple().length;
			leftChild.reset();
			this.first_enter = false;
			int cst = BNLJ_Page_Bytes/(4*Outer_attr);
			this.buf = new Buffer(pageSize, cst);
		}
		
		if(addedToSet == false) {//Initiate new block
			addLeftTableToBuffer();
			addedToSet = true;
			}
		Tuple left;
		String[] lt;
		Tuple right;
		String[] rt;
		if(buf.size()==0) return null;
		while (count<buf.size()) {//get tuple from the outer
			left = buf.get(count);
			ArrayList<SchemaPair> def_schema = new ArrayList<SchemaPair>(left.getSchemaList());
			lt = left.getTuple();
			StringBuffer sb = new StringBuffer();
			for (String s : lt) {
				sb.append(s).append(",");
			}
			
			while ((right = rightChild.getNextTuple()) != null) {//get tuple from the inner
				StringBuffer sb_next = new StringBuffer(sb);
				ArrayList<SchemaPair> def_schema_next = new ArrayList<SchemaPair>(def_schema);
				for (SchemaPair s : new ArrayList<SchemaPair>(right.getSchemaList())) {
					def_schema_next.add(s);
				}
				rt = right.getTuple();
				for (String s : rt) {
					sb_next.append(s).append(",");
				}
				Tuple tu = new Tuple(sb_next.toString().split(","), def_schema_next);
				if (ex != null) {
					conditionEvaluator eva = new conditionEvaluator(tu, ex);
					if (eva.getResult()) {
						return tu;
					}
				} else
					return tu;
			}
			rightChild.reset();
			count ++;
		}
		if(count==buf.size()){
			addedToSet = false;
			buf.clear();
			count = 0;
		}
		return null;
	}

	
	/**
	 * Method to reset by reset all its fields
	 */
	@Override
	public void reset() throws IOException {
		rightChild.reset();
		leftChild.reset();
		count = 0;
		//int cst = BNLJ_Page_Bytes/(4*Outer_attr);
		//buf = new Buffer(pageSize, cst);
		buf.clear();
		first_enter = true;
	}

	
	/**
	 * Method to reset by reset all its fields
	 */
	@Override
	public void reset(int index) throws IOException {
		// TODO Auto-generated method stub	
	}

	
	/**
	 * Method to dump the results of the join operator
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

	/**
	 * Method to read and store outer tuples in blocks
	 */
	private void addLeftTableToBuffer() throws IOException{
		Tuple left;
		
		while ((left = leftChild.getNextTuple()) != null && (!buf.isFull())) {
			this.buf.add(left);
		}// get and store tuple from the outer
	}
	
	@Override
	public void setLeftChild(Operator child) throws IOException {
		this.leftChild = child;
	}

	@Override
	public void setRightChild(Operator child) {
		this.rightChild = child;
	}

	@Override
	public Operator getLeftChild() {
		return this.leftChild;
	}

	@Override
	public Operator getRightChild() {
		return this.rightChild;
	}
	
	@Override
	public Expression getExpression(){
		return this.ex;
	}

}
