package project;

import java.io.IOException;

import logicalOperator.*;
import net.sf.jsqlparser.statement.select.AllColumns;
import physicalOperator.*;

/**
 * This class recursively builds a physical query plan with a tree structure
 * based on the passed in logical query plan tree
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class PhysicalPlanBuilder implements OperationVisitor{
	private Operator rootOperator = null;
	private Operator curOperator = null;
	private catalog cl;
	QueryInterpreter queryInterpreter;
	
	/**
	 * Constructor
	 * @param cl the catalog store table information and tables' alias 
	 * @param queryInterpreter query interpreter
	 */
	PhysicalPlanBuilder(catalog cl,QueryInterpreter queryInterpreter)
	{
		this.cl = cl;
		this.queryInterpreter = queryInterpreter;
	}
	
	/**
	 * @return the root of the physical query plan tree
	 */
	public Operator result(){
		return this.rootOperator;
	}
	
	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	public void visit(LogicalSelectOperator node) throws IOException {
		String tableName = getTableName(node);
		SelectOperator selectOperator = new SelectOperator(new ScanOperator(tableName),node.getExpressoin());
		if(rootOperator == null){
			rootOperator = selectOperator;
		}
		else if(curOperator instanceof JoinOperator){
			if(((JoinOperator)curOperator).getLeftChild() == null){
				((JoinOperator)curOperator).setLeftChild(selectOperator);
			}
			else{
				((JoinOperator)curOperator).setRightChild(selectOperator);
			}
		}
		else{
			curOperator.setLeftChild(selectOperator);
		}
	}
	
	/**
	 * This method return a table's name in a string format
	 * @param node the logical operator that contain table information
	 * @return the table's name
	 */
	private String getTableName(TreeNode node) {
		if(cl.UseAlias()){return node.getTable().getAlias();}
		return node.getTable().getName();
	}

	public void visit(LogicalScanOperator node){}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalJoinOperator node) throws IOException {
		
		JoinOperator joinOperator = new JoinOperator(null, null,node.getExpressoin());
		if(rootOperator == null){
			rootOperator = joinOperator; 
		}
		else{	
			curOperator.setLeftChild(joinOperator);
		}
		
		curOperator = joinOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
		//reset the current operator to this joinOperator for attaching the right child
		curOperator = joinOperator;
		if(node.getRightChild() != null) {node.getRightChild().accept(this);}
	}
	
	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalProjectOperator node) throws IOException {

		ProjectOperator projectOperator = new ProjectOperator(null,QueryPlan.schema_pair);
		if(rootOperator == null){
			rootOperator = projectOperator;
		}
		else{
			curOperator.setLeftChild(projectOperator);
		}
		curOperator = projectOperator;
		if(node.getLeftChild() != null ) {node.getLeftChild().accept(this);}
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalSortOperator node) throws IOException {
		// call different constructor depends on if query contains orderBy
		SortOperator sortOperator;
		if (QueryPlan.schema_pair_order != null && QueryPlan.schema_pair_order.size() > 0) {
			sortOperator = new SortOperator(null,QueryPlan.schema_pair_order); 
		}
		else{
			sortOperator = new SortOperator(null,QueryPlan.schema_pair);
		}
		if(rootOperator == null){
			rootOperator = sortOperator;
		}
		else{
			curOperator.setLeftChild(sortOperator);
		}
		curOperator = sortOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
	}

	/**
	 * this visit method recursively creates new children for the current operator
	 * to form a query operator tree
	 */
	@Override
	public void visit(LogicalDulplicateEliminationOperator node) throws IOException {
		//call different constructor depends on if projection is needed  
		DuplicateEliminationOperator distinctOperator;
		if(queryInterpreter.getSelectItemList().get(0) instanceof AllColumns){
			distinctOperator = new DuplicateEliminationOperator(null);
		}
		else{
			distinctOperator = new DuplicateEliminationOperator(null,QueryPlan.schema_pair);
		}
		if(rootOperator == null){
			rootOperator = distinctOperator;
		}
		else{
			curOperator.setLeftChild(distinctOperator);
		}
		curOperator = distinctOperator;
		if(node.getLeftChild() != null) {node.getLeftChild().accept(this);}
	}
	
	/**
	 * prints out the built physical plan tree for debugging purpose in postfix order
	 * @param op the root operator
	 */
	public void printPhysicalPlanTree(Operator op){
		if (op == null) return;
		printPhysicalPlanTree(op.getLeftChild());
		printPhysicalPlanTree(op.getRightChild());
		System.out.println("physical operator " + op.getClass());
	}
}
