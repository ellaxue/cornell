package logicalOperator;

import java.io.IOException;

import project.OperationVisitor;

/**
 * A logical operator class for distinct query
 * @author Chengcheng Ji (cj368), Pei Xu (px29) and Ella Xue (ex32) 
 */
public class LogicalDulplicateEliminationOperator extends TreeNode {
	public LogicalDulplicateEliminationOperator(TreeNode leftChild) {
		this.leftChild = leftChild;
	}
	
	@Override
	public void accept(OperationVisitor visitor) throws IOException {
		visitor.visit(this);
	}
	
}
