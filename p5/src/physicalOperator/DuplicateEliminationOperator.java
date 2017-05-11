package physicalOperator;
import java.util.ArrayList;
import java.util.Locale.FilteringMode;

import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleWriter;
import net.sf.jsqlparser.expression.Expression;
import project.QueryInterpreter;
import project.QueryPlan;
import project.SchemaPair;
import project.Tuple;
import queryPlanBuilder.PhysicalPlanBuilder;

/**
 * operator to support DISTINCT constraint.
 * 
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 */

public class DuplicateEliminationOperator extends Operator {
	private Operator child;
	ArrayList<SchemaPair> schema_pair;
	private boolean flag;
	private Tuple firstTuple;
	private boolean firstTime=true;

	/** 
	 * Assuming the tuple from child is sorted already. Simply compare
	 * the adjacent tuple and remove the duplicate tuple according to 
	 * the columns in the select clause.
	 * @param child of the operator (which must be a sort operator here)
	 * @param schema_pair the list containing <tablename,columnnmae> pair which is in the select clause
	 * @throws Exception
	 */
	public DuplicateEliminationOperator(Operator child, ArrayList<SchemaPair> schema_pair) throws Exception {
		this.child = child;
		this.schema_pair = schema_pair;
		flag = true;
	}

	/** 
	 * Constructor to deal with "select distinct *" 
	 * @param child of the operator (which must be a sort operator here)
	 * @throws Exception
	 */
	public DuplicateEliminationOperator(Operator child) throws Exception {
		this.child = child;
		flag = false;
	}
	/**
	 * Method to obtain the next tuple 
	 * 
	 * @return (Tuple) the tuple that distinct among all the tuples with respect to
	 *  the required schemas
	 */
	@Override
	public Tuple getNextTuple() throws Exception {
		if(this.flag == true){return ReturnWithOrderBy();}
		else{ return ReturnWithoutOrderBy();}
	}

	/**
	 * Method to reset after projection by reset all its fields
	 */
	@Override
	public void reset() throws Exception {
		child.reset();
	}

	/**
	 * Method to dump the results of the DISTINCT
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

	/**
	 * store distinct tuples with order by query
	 * @throws Exception IOexception
	 */
	private Tuple ReturnWithOrderBy() throws Exception{
		Tuple tu=null;
		if(firstTime) {
			firstTuple = child.getNextTuple();
			firstTime=false;
			return firstTuple;
		}
		if (firstTuple==null) return null;
		while (true) {
			tu=child.getNextTuple();
			if (tu==null) return null;
			boolean repeat = false;
			for (SchemaPair pair : schema_pair) {
				for (SchemaPair p : firstTuple.getSchemaList()) {
					if (pair.equalsTo(p)) {
						pair = p;
					}
				}
				int indext1 = firstTuple.getSchemaList().indexOf(pair);
				Long valuet1 = Long.parseLong(firstTuple.getTuple()[indext1]);
				for (SchemaPair p : tu.getSchemaList()) {
					if (pair.equalsTo(p)) {
						pair = p;
					}
				}
				int indext2 = tu.getSchemaList().indexOf(pair);
				Long valuet2 = Long.parseLong(tu.getTuple()[indext2]);
				if (!valuet1.equals(valuet2)) {//If encounter a distinct value, break and return the tuple
					repeat = false;
					break;
				}else repeat = true;
			}


			if (!repeat) {firstTuple=tu;
			return tu;}
		}
	}


	/**
	 * store distinct tuples witout orderby query
	 * @throws Exception IOexception
	 */
	private Tuple ReturnWithoutOrderBy() throws Exception{
		Tuple tu=null;
		if(firstTime) {
			firstTuple = child.getNextTuple();
			firstTime=false;
			return firstTuple;
		}
		System.out.println(firstTuple.getComplete());
		if (firstTuple==null) return null;
		while (true) {
			tu=child.getNextTuple();
			if (tu==null) return null;
			boolean repeat = false;
			if (tu != firstTuple) {
				for (SchemaPair pair : firstTuple.getSchemaList()) {
					int indext1 = firstTuple.getSchemaList().indexOf(pair);
					Long valuet1 = Long.parseLong(firstTuple.getTuple()[indext1]);
					for (SchemaPair p : tu.getSchemaList()) {
						if (pair.equalsTo(p)) {
							pair = p;
						}
					}
					int indext2 = tu.getSchemaList().indexOf(pair);
					Long valuet2 = Long.parseLong(tu.getTuple()[indext2]);
					if (!(valuet1.equals( valuet2))) {//If encounter a distinct value, break and return the tuple
						repeat = false;
						break;
					}else repeat = true;
				}
			}
			if (!repeat) {firstTuple=tu; return tu;}
			
		}
	}


	/**
	 * set current operator's child
	 */
	@Override
	public void setLeftChild(Operator child){
		this.child = child;
	}

	@Override
	public void setRightChild(Operator child) {}

	@Override
	public Operator getLeftChild() {return this.child;}

	@Override
	public Operator getRightChild() {return null;}

	@Override
	public Expression getExpression() {return null;}

	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub

	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("DupElim");
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
