package project;

import logicalOperator.*;

/**
 * Visitor pattern interface
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 */
public interface OperationVisitor {
	public void visit(LogicalSelectOperator node) throws Exception;
	
	public void visit(LogicalScanOperator node) throws Exception;
	
	public void visit(LogicalProjectOperator node) throws Exception;
	
	public void visit(LogicalJoinOperator node) throws Exception;

	public void visit(LogicalSortOperator node) throws Exception;
	
	public void visit(LogicalDulplicateEliminationOperator node) throws Exception;;
}
