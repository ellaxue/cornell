package project;

import java.io.IOException;

import logicalOperator.*;

/**
 * Visitor pattern interface
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32)
 */
public interface OperationVisitor {
	public void visit(LogicalSelectOperator node) throws IOException;
	
	public void visit(LogicalScanOperator node) throws IOException;
	
	public void visit(LogicalProjectOperator node) throws IOException;
	
	public void visit(LogicalJoinOperator node) throws IOException;

	public void visit(LogicalSortOperator node) throws IOException;
	
	public void visit(LogicalDulplicateEliminationOperator node) throws IOException;;
}
