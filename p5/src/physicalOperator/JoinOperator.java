package physicalOperator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import ChooseSelectionImp.Element;
import IO.BinaryWriter;
import IO.DirectWriter;
import IO.TupleWriter;
import logicalOperator.LogicalSelectOperator;
import logicalOperator.TreeNode;
import net.sf.jsqlparser.expression.Expression;
import project.QueryInterpreter;
import project.QueryPlan;
import project.SchemaPair;
import project.Tuple;
import project.catalog;
import project.conditionEvaluator;
import queryPlanBuilder.PhysicalPlanBuilder;

/**
 * Join operator to execute the join operation
 * 
 * @author Chengcheng Ji (cj368) and Pei Xu (px29)
 */

public class JoinOperator extends Operator {
	private ArrayList<Tuple> leftset;
	private Operator leftChild;
	private Operator rightChild;
	private Expression ex;
	private int count;
	private boolean addedToSet = false;
	private int printcount = 0;
	private ArrayList<Operator> children;

	private ArrayList<Element> unionFindJoinExpList;
	private ArrayList<Expression> residualJoinExpression;
	public JoinOperator(Operator leftChild, Operator rightChild, Expression ex) throws Exception {
		
		this.leftset = new ArrayList<>();
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.ex = ex;
		this.count = 0;
		this.children = new ArrayList<Operator>();
	}

	public JoinOperator(ArrayList<Expression> residualJoinExpression, ArrayList<Element> unionFindJoinExpression,
			Expression expressoin) {
		// TODO Auto-generated constructor stub
		this.leftset = new ArrayList<>();
		this.count = 0;
		this.children = new ArrayList<Operator>();
		this.unionFindJoinExpList = unionFindJoinExpression;
		this.residualJoinExpression= residualJoinExpression;
	}

	/** 
	 * Method to obtain a tuple from the outer and a tuple from the inner and glue them
	   together
	 * 
	 * @return (Tuple) the tuple matches the join condition(If the join is a cross product, all pairs of
	   tuples are returned)
	 */
	@Override
	public Tuple getNextTuple() throws Exception {
		if(addedToSet == false) {addLeftTableToSet();addedToSet = true;}
		Tuple left;
		String[] lt;
		Tuple right;
		String[] rt;
		while (count<leftset.size()) {//get tuple from the outer
			left = leftset.get(count);
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
		if (leftChild instanceof ExternalSortOperator){
			((ExternalSortOperator)leftChild).cleanFinalSortedFile();
		}
		if (rightChild instanceof ExternalSortOperator){
			((ExternalSortOperator)rightChild).cleanFinalSortedFile();
		}
		return null;
	}

	/**
	 * Method to reset by reset all its fields
	 */
	@Override
	public void reset() throws Exception  {
		leftChild.reset();
		rightChild.reset();
		count = 0;
		leftset = new ArrayList<Tuple>();
	}

	/**
	 * Method to dump the results of the join operator
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
	
	private void addLeftTableToSet() throws Exception{
		Tuple left;
		while ((left = leftChild.getNextTuple()) != null) {
			this.leftset.add(left);
		}// get and store tuple from the outer
	}
	
	@Override
	public void setLeftChild(Operator child) throws Exception {
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

	@Override
	public void reset(int index) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addChildren(Operator operator){
		System.out.println("join operator adding child ==========>" + operator.getClass());
		children.add(operator);
	}
	
	public void setResidualJoinExpression(ArrayList<Expression> expList){
		this.residualJoinExpression = expList;
	}
	
	/**
	 * @return the unionFindJoinExpList
	 */
	public ArrayList<Element> getUnionFindJoinExpList() {
		return unionFindJoinExpList;
	}

	/**
	 * @param unionFindJoinExpList the unionFindJoinExpList to set
	 */
	public void setUnionFindJoinExpList(ArrayList<Element> unionFindJoinExpList) {
		this.unionFindJoinExpList = unionFindJoinExpList;
	}

	/**
	 * @return the residualJoinExpression
	 */
	public ArrayList<Expression> getResidualJoinExpression() {
		return residualJoinExpression;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < PhysicalPlanBuilder.level; i++){
			sb.append("-");
		}
		sb.append("Join").append(residualJoinExpression).append("\n");
		
		for(Element e:unionFindJoinExpList){
			sb.append(e);
		}
		
		PhysicalPlanBuilder.level++;
		for(Operator op: children){
			sb.append(op);
		}
		return sb.toString();
	}
}
